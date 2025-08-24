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
package com.superior.lab.fragments;

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.preferences.KeyboxDataPreference
import com.android.settings.preferences.BasePreferenceFragment
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class Spoof : BasePreferenceFragment(R.xml.spoof) {

    private var keyboxDataPreference: KeyboxDataPreference? = null

    private lateinit var mGmsSpoof: Preference
    private lateinit var mPifJsonFilePreference: Preference
    private lateinit var mUpdateJsonButton: Preference

    private val handler = Handler(Looper.getMainLooper())

    private val keyboxFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                keyboxDataPreference?.handleFileSelected(uri)
            }
        }

    private val pifJsonFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
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

        mGmsSpoof.setOnPreferenceChangeListener { _, _ ->
            true
        }

        mPifJsonFilePreference.setOnPreferenceClickListener {
            openPifJsonFileSelector()
            true
        }

        mUpdateJsonButton.setOnPreferenceClickListener {
            updatePropertiesFromUrl(PIF_JSON_URL)
            true
        }

        findPreference<Preference>("show_pif_properties")?.setOnPreferenceClickListener {
            showPropertiesDialog()
            true
        }
    }

    private fun openPifJsonFileSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        pifJsonFilePickerLauncher.launch(intent)
    }

    private fun showPropertiesDialog() {
        var jsonString = Settings.System.getString(requireContext().contentResolver, "pif_props_data")
        if (TextUtils.isEmpty(jsonString)) {
            Log.e(TAG, "No spoofing data found in Settings")
            jsonString = getString(R.string.error_loading_properties)
        } else {
            try {
                val json = JSONObject(jsonString!!)
                jsonString = json.toString(4).replace("\\/", "/")
            } catch (e: JSONException) {
                Log.e(TAG, "Malformed JSON in pif_props_data", e)
                jsonString = getString(R.string.error_loading_properties)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.show_pif_properties_title)
            .setMessage(jsonString)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun updatePropertiesFromUrl(urlString: String) {
        Thread {
            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                try {
                    val json = urlConnection.inputStream.readBytes().toString(StandardCharsets.UTF_8)
                    Log.d(TAG, "Downloaded JSON data: $json")
                    val jsonObject = JSONObject(json)
                    val spoofedModel = jsonObject.optString("MODEL", "Unknown model")
                    Settings.System.putString(requireActivity().contentResolver, "pif_props_data", jsonObject.toString())
                    handler.post {
                        val toastMessage = getString(R.string.toast_spoofing_success, spoofedModel)
                        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show()
                    }
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading JSON or setting properties", e)
                handler.post {
                    Toast.makeText(requireContext(), R.string.toast_spoofing_failure, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun loadPifJson(uri: Uri) {
        Log.d(TAG, "Loading PIF JSON from URI: $uri")
        try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.readBytes().toString(StandardCharsets.UTF_8)
                Log.d(TAG, "PIF JSON data: $json")
                val jsonObject = JSONObject(json)
                Settings.System.putString(requireActivity().contentResolver, "pif_props_data", jsonObject.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading PIF JSON or setting properties", e)
        }
    }

    companion object {
        private const val TAG = "Spoof"
        private const val SYS_GMS_SPOOF = "persist.sys.pixelprops.gms"
        private const val KEY_PIF_JSON_FILE_PREFERENCE = "pif_json_file_preference"
        private const val KEY_UPDATE_JSON_BUTTON = "update_pif_json"
        private const val PIF_JSON_URL =
            "https://raw.githubusercontent.com/SuperiorOS/PlayIntegrityFix/refs/heads/sixteen-los/pif.json"
    }
}
