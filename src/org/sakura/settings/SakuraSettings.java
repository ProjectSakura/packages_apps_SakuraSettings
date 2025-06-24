/*
 * Copyright (C) 2019-2025 Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sakura.settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class SakuraSettings extends DashboardFragment {

    private static final String TAG = "SakuraSettings";

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.sakura_settings;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SAKURA_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.sakura_settings);
}
