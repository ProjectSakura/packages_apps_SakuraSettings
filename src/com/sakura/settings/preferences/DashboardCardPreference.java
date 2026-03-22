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
        
        // Remove the default preference background so the root container doesn't draw a square ripple
        itemView.setBackground(null);
        
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
