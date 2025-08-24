package com.android.settings.preferences

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

data class Keybox(
    val algorithm: String,
    val privateKey: String,
    val certificates: List<String>
)

class KeyboxDataPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    companion object {
        private const val TAG = "KeyboxDataPreference"
        private const val SECURE_KEY = "keybox_data"
        private const val MIME_TYPE_XML = "text/xml"

        private const val TAG_NUMBER_OF_KEYBOXES = "NumberOfKeyboxes"
        private const val TAG_KEY = "Key"
        private const val TAG_PRIVATE_KEY = "PrivateKey"
        private const val TAG_CERTIFICATE = "Certificate"
        private const val ATTR_ALGORITHM = "algorithm"
        private const val ATTR_FORMAT = "format"
        private const val FORMAT_PEM = "pem"

        private const val ALG_ECDSA = "ecdsa"
        private const val ALG_RSA = "rsa"

        private const val KEY_EC = "EC"
        private const val KEY_RSA = "RSA"
    }

    private var filePickerLauncher: ActivityResultLauncher<Intent>? = null

    init {
        layoutResource = R.layout.keybox_data_pref
    }

    fun setFilePickerLauncher(launcher: ActivityResultLauncher<Intent>) {
        filePickerLauncher = launcher
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.setOnClickListener {
            filePickerLauncher?.launch(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = MIME_TYPE_XML
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )
        }

        holder.findViewById(R.id.delete_button)?.let { view ->
            (view as? ImageButton)?.setOnClickListener {
                Settings.Secure.putString(context.contentResolver, SECURE_KEY, null)
                showToast(R.string.keybox_data_cleared)
                callChangeListener(null)
            }
        }
    }

    fun handleFileSelected(uri: Uri?) {
        if (uri == null || !isXmlFile(uri)) {
            showToast(R.string.keybox_invalid_file)
            return
        }

        try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                val xmlContent = reader.readText().removePrefix("\uFEFF")
                val keyboxes = parseXml(xmlContent)

                if (keyboxes == null) {
                    showToast(R.string.keybox_invalid_xml)
                    return
                }

                val jsonObject = JSONObject()
                keyboxes.forEach { keybox ->
                    val algorithmPrefix = keybox.algorithm
                    jsonObject.put("${algorithmPrefix}.PRIV", keybox.privateKey)
                    keybox.certificates.forEachIndexed { index, cert ->
                        jsonObject.put("${algorithmPrefix}.CERT_${index + 1}", cert)
                    }
                }

                val jsonString = jsonObject.toString()
                Settings.Secure.putString(context.contentResolver, SECURE_KEY, jsonString)

                showToast(R.string.keybox_file_loaded)
                callChangeListener(jsonString)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read XML file", e)
            showToast(R.string.keybox_file_read_failed)
        }
    }

    private fun parseXml(xml: String): List<Keybox>? {
        val keyboxes = mutableListOf<Keybox>()
        var numberOfKeyboxes: Int? = null

        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
                setInput(StringReader(xml))
            }

            var eventType = parser.eventType
            var currentAlg: String? = null
            var currentPriv: String? = null
            val currentCerts = mutableListOf<String>()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        TAG_NUMBER_OF_KEYBOXES -> {
                            numberOfKeyboxes = parser.nextText().trim().toIntOrNull()
                        }

                        TAG_KEY -> {
                            currentAlg = when (parser.getAttributeValue(null, ATTR_ALGORITHM)?.lowercase()) {
                                ALG_ECDSA -> KEY_EC
                                ALG_RSA -> KEY_RSA
                                else -> null
                            }
                            currentCerts.clear()
                            currentPriv = null
                        }

                        TAG_PRIVATE_KEY -> {
                            if (currentAlg == null || !parser.isPemFormat()) {
                                Log.w(TAG, "Skipping key due to invalid format or algorithm")
                                currentPriv = null
                            } else {
                                currentPriv = parser.nextText().trim()
                            }
                        }

                        TAG_CERTIFICATE -> {
                            if (currentAlg == null || !parser.isPemFormat()) {
                                Log.w(TAG, "Skipping certificate due to invalid format or algorithm")
                            } else {
                                currentCerts.add(parser.nextText().trim())
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> if (parser.name == TAG_KEY && currentAlg != null && currentPriv != null) {
                        keyboxes.add(Keybox(currentAlg, currentPriv, currentCerts.toList()))
                        currentAlg = null
                        currentPriv = null
                        currentCerts.clear()
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML parsing failed", e)
            return null
        }

        return if (keyboxes.isNotEmpty()) keyboxes else null
    }

    private fun isXmlFile(uri: Uri): Boolean {
        val type = context.contentResolver.getType(uri)
        return uri.toString().endsWith(".xml", ignoreCase = true) || type == MIME_TYPE_XML
    }

    private fun XmlPullParser.isPemFormat(): Boolean =
        getAttributeValue(null, ATTR_FORMAT)?.equals(FORMAT_PEM, ignoreCase = true) == true

    private fun showToast(resId: Int) =
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
}
