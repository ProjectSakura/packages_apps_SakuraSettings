/*
 * Copyright (C) 2018 The Superior OS Project
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.sakura.settings.R;
import lineageos.app.LineageContextConstants;

public class LockscreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String FOD_ANIMATION = "fod_anim";

    private PreferenceCategory mFODIconPickerCategory;
    private Preference mFODAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sakura_settings_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasFod = packageManager.hasSystemFeature(LineageContextConstants.Features.FOD);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null && !hasFod) {
            prefSet.removePreference(mFODIconPickerCategory);
        }
        boolean showFODAnimationPicker = mContext.getResources().getBoolean(R.bool.showFODAnimationPicker);
        mFODAnimation = (Preference) findPreference(FOD_ANIMATION);
        if ((mFODIconPickerCategory != null && mFODAnimation != null && !hasFod) ||
                (mFODIconPickerCategory != null && mFODAnimation != null && !showFODAnimationPicker)) {
            mFODIconPickerCategory.removePreference(mFODAnimation);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SAKURA;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
