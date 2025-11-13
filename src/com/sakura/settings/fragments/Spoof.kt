/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sakura.settings.fragments;

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemProperties
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.preferences.KeyboxDataPreference
import com.android.settings.preferences.BasePreferenceFragment
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class Spoof : BasePreferenceFragment(R.xml.spoof) {

    private var keyboxDataPreference: KeyboxDataPreference? = null

    private lateinit var mGmsSpoof: Preference
    private lateinit var mPifJsonFilePreference: Preference
    private lateinit var mUpdateJsonButton: Preference

    private val handler = Handler(Looper.getMainLooper())

    private val keyboxFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                keyboxDataPreference?.handleFileSelected(result.data?.data)
            }
        }

    private val pifJsonFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    loadPifJson(uri)
                    Toast.makeText(requireContext(), R.string.toast_import_success, Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyboxDataPreference = findPreference("keybox_data_setting") as? KeyboxDataPreference
        keyboxDataPreference?.setFilePickerLauncher(keyboxFilePickerLauncher)

        mGmsSpoof = findPreference(SYS_GMS_SPOOF)!!
        mPifJsonFilePreference = findPreference(KEY_PIF_JSON_FILE_PREFERENCE)!!
        mUpdateJsonButton = findPreference(KEY_UPDATE_JSON_BUTTON)!!

        mGmsSpoof.setOnPreferenceChangeListener { _, _ -> true }

        mPifJsonFilePreference.setOnPreferenceClickListener { openPifJsonFileSelector(); true }
        mUpdateJsonButton.setOnPreferenceClickListener { updatePropertiesFromUrl(PIF_JSON_URL); true }

        findPreference<Preference>("show_pif_properties")?.setOnPreferenceClickListener { showPropertiesDialog(); true }
    }

    private fun openPifJsonFileSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        pifJsonFilePickerLauncher.launch(intent)
    }

    private fun showPropertiesDialog() {
        val keys = SystemProperties.get("persist.sys.propshooks_keys", "")
        if (TextUtils.isEmpty(keys)) {
            Toast.makeText(requireContext(), R.string.error_loading_properties, Toast.LENGTH_SHORT).show()
            return
        }

        val commonKeys = mapOf(
            "MF" to "MANUFACTURER",
            "MD" to "MODEL",
            "FP" to "FINGERPRINT",
            "PR" to "PRODUCT",
            "DV" to "DEVICE",
            "SP" to "SECURITY_PATCH",
            "ISDK" to "DEVICE_INITIAL_SDK_INT"
        )

        val map = keys.split(",").associate { key ->
            val fullKey = commonKeys.getOrDefault(key, key)
            fullKey to SystemProperties.get("persist.sys.propshooks_$key", "")
        }

        val displayJson = JSONObject(map).toString(4).replace("\\/", "/")

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.show_pif_properties_title)
            .setMessage(displayJson)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun updatePropertiesFromUrl(urlString: String) {
        Thread {
            try {
                val json = java.net.URL(urlString).readBytes().toString(StandardCharsets.UTF_8)
                applyPifJson(JSONObject(json))
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading or applying JSON", e)
                handler.post { Toast.makeText(requireContext(), R.string.toast_spoofing_failure, Toast.LENGTH_LONG).show() }
            }
        }.start()
    }

    private fun loadPifJson(uri: Uri) {
        try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                applyPifJson(JSONObject(inputStream.readBytes().toString(StandardCharsets.UTF_8)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading PIF JSON", e)
        }
    }

    private fun applyPifJson(jsonObject: JSONObject) {
        val keys = mutableListOf<String>()
        for (entry in jsonObject.keys()) {
            val key = entry
            val value = jsonObject.getString(key)
            val pifKey = when (key) {
                "MANUFACTURER" -> "MF"
                "MODEL" -> "MD"
                "FINGERPRINT" -> "FP"
                "PRODUCT" -> "PR"
                "DEVICE" -> "DV"
                "SECURITY_PATCH" -> "SP"
                "DEVICE_INITIAL_SDK_INT" -> "ISDK"
                else -> key
            }
            SystemProperties.set("persist.sys.propshooks_$pifKey", value)
            keys.add(pifKey)
        }

        val keysCsv = keys.joinToString(",")
        SystemProperties.set("persist.sys.propshooks_keys", keysCsv)

        val hash = MessageDigest.getInstance("SHA-256").digest(
            keys.sorted().joinToString("") { k -> k + SystemProperties.get("persist.sys.propshooks_$k", "") }.toByteArray()
        ).joinToString("") { "%02x".format(it) }

        SystemProperties.set("persist.sys.propshooks_data_hash", hash)

        handler.post {
            val spoofedModel = jsonObject.optString("MODEL", "Unknown model")
            Toast.makeText(requireContext(), getString(R.string.toast_spoofing_success, spoofedModel), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "Spoof"
        private const val SYS_GMS_SPOOF = "persist.sys.pixelprops.gms"
        private const val KEY_PIF_JSON_FILE_PREFERENCE = "pif_json_file_preference"
        private const val KEY_UPDATE_JSON_BUTTON = "update_pif_json"
        private const val PIF_JSON_URL =
            "https://raw.githubusercontent.com/ProjectSakura/PlayIntegrityFix/refs/heads/16/pif.json"
    }
}
