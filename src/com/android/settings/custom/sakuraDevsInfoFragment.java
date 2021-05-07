/*
 * Copyright (C) 2017 Cardinal-AOSP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.custom;

import android.content.Context;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.android.settings.SettingsPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import com.android.settings.R;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.sakura.settings.utils.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class sakuraDevsInfoFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sakura_devs_info);
        if (isOnline()) {
            showGitProfileIcons();
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SAKURA;
    }

    private void showGitProfileIcons() {
		getGithubIcon(R.string.maintainer_shen_git, findPreference("maintainer_shen"));
    getGithubIcon(R.string.maintainer_shen_git, findPreference("maintainer_X00T"));
		getGithubIcon(R.string.maintainer_arun_git, findPreference("maintainer_arun"));
		getGithubIcon(R.string.maintainer_nitish_git, findPreference("maintainer_nitish"));
		getGithubIcon(R.string.maintainer_haridhayal_git, findPreference("maintainer_haridhayal"));
    getGithubIcon(R.string.maintainer_haridhayal_git, findPreference("maintainer_dreamlte"));
    getGithubIcon(R.string.maintainer_haridhayal_git, findPreference("maintainer_dream2lte"));
    getGithubIcon(R.string.maintainer_haridhayal_git, findPreference("maintainer_greatlte"));
		getGithubIcon(R.string.maintainer_armypunk_git, findPreference("maintainer_armypunk"));
		getGithubIcon(R.string.maintainer_amy_git, findPreference("maintainer_amy"));
		getGithubIcon(R.string.maintainer_agus_git, findPreference("maintainer_agus"));
    getGithubIcon(R.string.maintainer_cyberkiddo_git, findPreference("maintainer_cyberkiddo"));
    getGithubIcon(R.string.maintainer_henrique_git, findPreference("maintainer_henrique"));
    getGithubIcon(R.string.maintainer_tanmay_git, findPreference("maintainer_tanmay"));
    getGithubIcon(R.string.maintainer_darksitara_git, findPreference("maintainer_darksitara"));
    getGithubIcon(R.string.maintainer_chatur_git, findPreference("maintainer_chatur"));
    getGithubIcon(R.string.maintainer_chatur_git, findPreference("maintainer_a20"));    
    getGithubIcon(R.string.maintainer_a9ito_git, findPreference("maintainer_a9ito"));
    getGithubIcon(R.string.maintainer_thdas_git, findPreference("maintainer_thdas"));
    getGithubIcon(R.string.maintainer_thdas_git, findPreference("maintainer_m1892"));
    getGithubIcon(R.string.maintainer_abishek_git, findPreference("maintainer_abishek"));
    getGithubIcon(R.string.maintainer_shriiyansh_git, findPreference("maintainer_shriiyansh"));
    getGithubIcon(R.string.maintainer_deepakjr_git, findPreference("maintainer_deepakjr"));
    getGithubIcon(R.string.maintainer_eduardo_git, findPreference("maintainer_eduardo"));
    getGithubIcon(R.string.maintainer_cakestwix_git, findPreference("maintainer_cakestwix"));
    getGithubIcon(R.string.maintainer_cakestwix_git, findPreference("maintainer_jd2019"));
    getGithubIcon(R.string.maintainer_alexbrrbrr_git, findPreference("maintainer_alexbrrbrr"));
    getGithubIcon(R.string.maintainer_spicyfriedbread_git, findPreference("maintainer_spicyfriedbread"));
    }

    public void getGithubIcon(int usernameResId, Preference preference) {
        new parseGitIcon().execute(getResources().getString(usernameResId), preference);
    }

    private class parseGitIcon extends AsyncTask<Object, Preference, String> {
        private String id;
        private Drawable image;
        private Preference preference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Object... arg0) {
            preference = (Preference) arg0[1];
            HttpHandler sh = new HttpHandler();
            String url = "https://api.github.com/users/" + arg0[0].toString();
            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    id = jsonObj.getString("id");
                } catch (final JSONException ignored) {}
            }
            try {
                InputStream is = (InputStream) new URL("https://avatars2.githubusercontent.com/u/" + id + "?v=4").getContent();
                image = Drawable.createFromStream(is, "src name");
                image = new BitmapDrawable(getResources(), getCircularImage(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            preference.setIcon(image);
        }
    }

    protected Bitmap getCircularImage(Drawable drawable) {
        Bitmap bitmap = null;
        Bitmap srcBitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                srcBitmap = bitmapDrawable.getBitmap();
            }
        }
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvasF = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvasF.getWidth(), canvasF.getHeight());
        drawable.draw(canvasF);
        srcBitmap = bitmap;
        int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());
        Bitmap dstBitmap = Bitmap.createBitmap (
                squareBitmapWidth,
                squareBitmapWidth,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (squareBitmapWidth-srcBitmap.getWidth())/2;
        float top = (squareBitmapWidth-srcBitmap.getHeight())/2;
        canvas.drawBitmap(srcBitmap, left, top, paint);
        srcBitmap.recycle();
        return dstBitmap;
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
