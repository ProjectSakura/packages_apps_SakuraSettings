/*
 * Copyright (C) 2023-2024 The risingOS Android Project
 * Copyright (C) 2024-25 Project Infinity X 
 * Copyright (C) 2026 crDroid Android Project
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
package com.sakura.settings.fragments.lockscreen;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.sakura.ThemeUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.widget.LayoutPreference;

import com.sakura.settings.utils.SystemUtils;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class CustomClockPreview extends SettingsPreferenceFragment {

    private static final String TAG = "CustomClockPreview";
    private static final String PREF_FIRST_TIME = "first_time_clock_face_access";

    private static final String KEY_CLOCK_PREVIEW = "clock_preview";

    private ViewPager viewPager;
    private ClockPagerAdapter pagerAdapter;
    private ExtendedFloatingActionButton applyFab;
    private View highlightGuide;
    private TextView clockNameTextView;

    private int mClockPosition = 0;

    private ThemeUtils mThemeUtils;
    private Handler mHandler = new Handler();

    // IMPORTANT: This array must stay index-for-index identical to CLOCK_LAYOUTS
    // in frameworks/base/packages/SystemUI/src/com/android/systemui/clocks/ClockStyle.kt
    // for every index it covers (0-33). Indices 2 and 3 use the default layout as a
    // placeholder since their real layouts need SlateForOnePlus fonts not present here.
    private static final int[] CLOCK_LAYOUTS = {
            R.layout.keyguard_clock_default,      // 0  Default Clock
            R.layout.keyguard_clock_oos,            // 1  OnePlus Clock
            R.layout.keyguard_clock_default,        // 2  OnePlus Clock 2 (placeholder - needs SlateForOnePlus fonts)
            R.layout.keyguard_clock_default,        // 3  Center Clock (placeholder - verify asset deps first)
            R.layout.keyguard_clock_simple,          // 4  Simple Clock
            R.layout.keyguard_clock_miui,            // 5  MIUI Clock
            R.layout.keyguard_clock_ide,              // 6  IDE Clock
            R.layout.keyguard_clock_moto,            // 7  Moto Clock
            R.layout.keyguard_clock_stylish,          // 8  Stylish Clock
            R.layout.keyguard_clock_stylish2,        // 9  Stylish Clock 2
            R.layout.keyguard_clock_stylish3,        // 10 Stylish Clock 3
            R.layout.keyguard_clock_stylish4,        // 11 Stylish Clock 4
            R.layout.keyguard_clock_stylish5,        // 12 Stylish Clock 5
            R.layout.keyguard_clock_stylish6,        // 13 Stylish Clock 6
            R.layout.keyguard_clock_stylish7,        // 14 Stylish Clock 7
            R.layout.keyguard_clock_stylish8,        // 15 Stylish Clock 8
            R.layout.keyguard_clock_stylish9,        // 16 Stylish Clock 9
            R.layout.keyguard_clock_stylish10,      // 17 Stylish Clock 10
            R.layout.keyguard_clock_word,            // 18 Text Clock
            R.layout.keyguard_clock_life,            // 19 LifeStyle Clock
            R.layout.keyguard_clock_a9,              // 20 Android 9 Vibe
            R.layout.keyguard_clock_nos1,            // 21 NothingOS 1 Clock
            R.layout.keyguard_clock_nos2,            // 22 NothingOS 2 Clock
            R.layout.keyguard_clock_num,              // 23 Stacked Clock
            R.layout.keyguard_clock_accent,          // 24 X Factor
            R.layout.keyguard_clock_analog,          // 25 Simple Analog
            R.layout.keyguard_clock_block,            // 26 Block
            R.layout.keyguard_clock_bubble,          // 27 Bubble
            R.layout.keyguard_clock_label,            // 28 Label Clock
            R.layout.keyguard_clock_ios,              // 29 IOS Clock
            R.layout.keyguard_clock_taden,            // 30 Taden Clock
            R.layout.keyguard_clock_mont,              // 31 Mont Clock
            R.layout.keyguard_clock_encode,          // 32 Encode Clock
            R.layout.keyguard_clock_nos3              // 33 NOS Clock 3
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(getString(R.string.lockscreen_custom_clock_style_title));
        mThemeUtils = new ThemeUtils(getActivity());

        addPreferencesFromResource(R.xml.lockscreen_clock_preview_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClockPreview();
    }

    private void setupClockPreview() {
        LayoutPreference clockPreviewPref = findPreference(KEY_CLOCK_PREVIEW);
        if (clockPreviewPref == null) return;

        clockNameTextView = clockPreviewPref.findViewById(R.id.clock_name);
        viewPager = clockPreviewPref.findViewById(R.id.view_pager);
        applyFab = clockPreviewPref.findViewById(R.id.apply_extended_fab);
        highlightGuide = clockPreviewPref.findViewById(R.id.highlight_guide);

        pagerAdapter = new ClockPagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setClipChildren(true);
        viewPager.setClipToPadding(true);

        mClockPosition = Settings.Secure.getIntForUser(
                getContext().getContentResolver(), Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
        if (mClockPosition < 0 || mClockPosition >= CLOCK_LAYOUTS.length) {
            mClockPosition = 0;
            Settings.Secure.putIntForUser(
                    getContext().getContentResolver(), Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
        }
        viewPager.setCurrentItem(mClockPosition);

        applyFab.setOnClickListener(v -> {
            Context ctx = getContext();
            int currentClock = Settings.Secure.getIntForUser(
                    ctx.getContentResolver(), Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
            if (mClockPosition != currentClock) {
                Settings.Secure.putIntForUser(
                        ctx.getContentResolver(), Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE, mClockPosition, UserHandle.USER_CURRENT);
                updateClockOverlays(mClockPosition);
                SystemUtils.restartSystemUI(ctx);
            }
        });

        if (isFirstTime()) {
            highlightGuide.setVisibility(View.VISIBLE);
            highlightGuide.setOnClickListener(v -> {
                highlightGuide.setVisibility(View.GONE);
                disableHighlight();
            });
        } else {
            highlightGuide.setVisibility(View.GONE);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                mClockPosition = position;
                if (viewPager != null) {
                    viewPager.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                }
                updateClockName(position);
            }
        });

        updateClockName(mClockPosition);
    }

    // IMPORTANT: This array must stay index-for-index identical to CLOCK_LAYOUTS above
    // and to ClockStyle.kt's CLOCK_LAYOUTS in SystemUI for indices 0-33.
    private void updateClockName(int position) {
        String[] clockNames = {
            "Default Clock",
            "OnePlus Clock",
            "OnePlus Clock 2",
            "Center Clock",
            "Simple Clock",
            "MIUI Clock",
            "IDE Clock",
            "Moto Clock",
            "Stylish Clock",
            "Stylish Clock 2",
            "Stylish Clock 3",
            "Stylish Clock 4",
            "Stylish Clock 5",
            "Stylish Clock 6",
            "Stylish Clock 7",
            "Stylish Clock 8",
            "Stylish Clock 9",
            "Stylish Clock 10",
            "Text Clock",
            "LifeStyle Clock",
            "Android 9 Vibe",
            "NothingOS 1 Clock",
            "NothingOS 2 Clock",
            "Stacked Clock",
            "X Factor",
            "Simple Analog",
            "Block",
            "Bubble",
            "Label Clock",
            "IOS Clock",
            "Taden Clock",
            "Mont Clock",
            "Encode Clock",
            "NOS Clock 3",
        };
        if (clockNameTextView != null && position >= 0 && position < clockNames.length) {
            clockNameTextView.setText(clockNames[position]);
        }
    }

    private void updateClockOverlays(int clockStyle) {
        mThemeUtils.setOverlayEnabled(
                "android.theme.customization.hideclock",
                clockStyle != 0 ? "com.android.systemui.clocks.hideclock" : "android",
                "android");
        mThemeUtils.setOverlayEnabled(
                "android.theme.customization.smartspace",
                clockStyle != 0 ? "com.android.systemui.hide.smartspace" : "com.android.systemui",
                "com.android.systemui");
    }

    private boolean isFirstTime() {
        return Settings.System.getIntForUser(
                getContext().getContentResolver(), PREF_FIRST_TIME, 1, UserHandle.USER_CURRENT) != 0;
    }

    private void disableHighlight() {
        Settings.System.putIntForUser(
                getContext().getContentResolver(), PREF_FIRST_TIME, 0, UserHandle.USER_CURRENT);
    }

    private class ClockPagerAdapter extends PagerAdapter {
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View layout = inflater.inflate(CLOCK_LAYOUTS[position], container, false);
            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return CLOCK_LAYOUTS.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateClockName(mClockPosition);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockName(mClockPosition);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SAKURA_SETTINGS;
    }
}
