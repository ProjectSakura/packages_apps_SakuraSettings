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

import com.android.settings.R;

public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";
    private CustomSeekBarPreference mQsPanelAlpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sakura_settings_quicksettings);
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        mQsPanelAlpha = (CustomSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_BG_ALPHA, 221);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);
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

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Resources res = null;

        Settings.System.putIntForUser(resolver,
           Settings.System.QS_TILE_TITLE_VISIBILITY, 1, UserHandle.USER_CURRENT);
   }
}
