/*
 * Copyright (C) 2026 Project Sakura
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
package com.sakura.settings.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class DashboardCardPreference extends Preference {

    public DashboardCardPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DashboardCardPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DashboardCardPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DashboardCardPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.sakura_dashboard_card);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View itemView = holder.itemView;
        
        // Aggressively remove AOSP ItemDecorations from the parent RecyclerView to prevent grouped background blobs overriding our cards
        if (itemView.getParent() instanceof androidx.recyclerview.widget.RecyclerView) {
            androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) itemView.getParent();
            while (rv.getItemDecorationCount() > 0) {
                rv.removeItemDecorationAt(0);
            }
        }
        
        // Remove the default preference background so the root container doesn't draw a square ripple
        itemView.setBackgroundColor(0);
        
        // Remove padding from the parent layout container that Preference framework adds
        itemView.setPadding(0, 0, 0, 0);

        // Disable dividers to prevent standard list styling
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
        
        // Find the actual MaterialCardView and bind clicks natively to it for rounded ripples!
        int cardId = itemView.getResources().getIdentifier("dashboard_card", "id", itemView.getContext().getPackageName());
        if (cardId != 0) {
            View cardView = itemView.findViewById(cardId);
            if (cardView != null) {
                cardView.setClickable(true);
                cardView.setFocusable(true);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performClick();
                    }
                });
                
                // Disable traditional intercept so the user sees the Card's ripple, not the itemView's
                itemView.setClickable(false);
                itemView.setFocusable(false);
            }
        }
    }
}
