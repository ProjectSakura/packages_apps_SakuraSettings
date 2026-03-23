package com.sakura.settings.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settingslib.widget.LayoutPreference;

public class MonolithicDashboardPreference extends LayoutPreference {

    private PreferenceManager mManager;

    public MonolithicDashboardPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Native LayoutPreference avoids AOSP ItemDecorations and grouped backgrounds!
        // No need for RecyclerView padding hacks or background stripping.

        // Hardcode clicks for all 10 explicit standalone cards
        setupClick(holder.itemView, "card_statusbar", "statusbar_category");
        setupClick(holder.itemView, "card_quicksettings", "quicksettings_category");
        setupClick(holder.itemView, "card_buttons", "buttonsettings_category");
        setupClick(holder.itemView, "card_gestures", "gestures_category");
        setupClick(holder.itemView, "card_lockscreen", "lockscreen_category");
        setupClick(holder.itemView, "card_themes", "themes");
        setupClick(holder.itemView, "card_notifications", "notifications_category");
        setupClick(holder.itemView, "card_misc", "misc_category");
        setupClick(holder.itemView, "card_dev_info", "dev_info");
        setupClick(holder.itemView, "card_donators_info", "donators_info");
    }

    private void setupClick(View root, String viewIdStr, String targetKey) {
        int resId = root.getResources().getIdentifier(viewIdStr, "id", root.getContext().getPackageName());
        if (resId != 0) {
            View target = root.findViewById(resId);
            if (target != null) {
                target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getPreferenceManager() != null && getPreferenceManager().getPreferenceScreen() != null) {
                            Preference realPref = getPreferenceManager().getPreferenceScreen().findPreference(targetKey);
                            if (realPref != null && getPreferenceManager().getOnPreferenceTreeClickListener() != null) {
                                getPreferenceManager().getOnPreferenceTreeClickListener().onPreferenceTreeClick(realPref);
                            }
                        }
                    }
                });
            }
        }
    }
}
