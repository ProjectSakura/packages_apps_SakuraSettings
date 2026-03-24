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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SakuraDonatorsInfoFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sakura_donators_info);
        if (isOnline()) {
            showGitProfileIcons();
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SAKURA_SETTINGS;
    }

    private void showGitProfileIcons() {
		getGithubIcon(R.string.donator_1_git, findPreference("donator1"));
		getGithubIcon(R.string.donator_2_git, findPreference("donator2"));
		getGithubIcon(R.string.donator_3_git, findPreference("donator3"));
		getGithubIcon(R.string.donator_4_git, findPreference("donator4"));
		getGithubIcon(R.string.donator_5_git, findPreference("donator5"));
		getGithubIcon(R.string.donator_6_git, findPreference("donator6"));
		getGithubIcon(R.string.donator_7_git, findPreference("donator7"));
		getGithubIcon(R.string.donator_8_git, findPreference("donator8"));
		getGithubIcon(R.string.donator_9_git, findPreference("donator9"));
		getGithubIcon(R.string.donator_10_git, findPreference("donator10"));
    }
    
    public void getGithubIcon(int usernameResId, Preference preference) {
        new parseGitIcon().execute(getResources().getString(usernameResId), preference);
    }

    private class parseGitIcon extends AsyncTask<Object, Preference, String> {
        private Drawable image;
        private Preference preference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Object... arg0) {
            String username = arg0[0].toString();
            preference = (Preference) arg0[1];
            try {
                Context context = getContext();
                if (context == null) return null;
                java.io.File cacheDir = context.getCacheDir();
                java.io.File avatarFile = new java.io.File(cacheDir, username + "_github_avatar.png");
                long oneHour = 60 * 60 * 1000;

                if (avatarFile.exists() && (System.currentTimeMillis() - avatarFile.lastModified() < oneHour)) {
                    image = Drawable.createFromPath(avatarFile.getAbsolutePath());
                } else {
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL("https://github.com/" + username + ".png").openConnection();
                    conn.setInstanceFollowRedirects(true);
                    java.io.InputStream is = conn.getInputStream();

                    java.io.FileOutputStream fos = new java.io.FileOutputStream(avatarFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();

                    image = Drawable.createFromPath(avatarFile.getAbsolutePath());
                }
                if (image != null) {
                    image = new BitmapDrawable(getResources(), getCircularImage(image));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (image != null) {
                preference.setIcon(image);
            }
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
