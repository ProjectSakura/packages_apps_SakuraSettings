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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.android.settings.R
import com.android.settings.preferences.KeyboxDataPreference
import com.android.settings.preferences.BasePreferenceFragment

class Spoof : BasePreferenceFragment(R.xml.spoof) {

    private var keyboxDataPreference: KeyboxDataPreference? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data!!.data
                keyboxDataPreference?.handleFileSelected(uri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        keyboxDataPreference =
            findPreference("keybox_data_setting") as? KeyboxDataPreference
        keyboxDataPreference?.setFilePickerLauncher(filePickerLauncher)
    }
}
