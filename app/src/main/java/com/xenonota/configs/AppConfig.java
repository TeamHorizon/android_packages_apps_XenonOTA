/*
 * Copyright (C) 2017 Team Horizon
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

package com.xenonota.configs;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xenonota.R;
import com.xenonota.utils.OTAUtils;

import java.text.DateFormat;
import java.util.Date;

public final class AppConfig {

    private static final String LAST_CHECK = "last_check";
    private static final String LATEST_VERSION = "latest_version";

    private AppConfig() {
    }

    public static String getLatestVersionKey() {
        return LATEST_VERSION;
    }

    public static String getLastCheckKey() {
        return LAST_CHECK;
    }

    private static String buildLastCheckSummary(long time, Context context) {
        String prefix = context.getResources().getString(R.string.last_check_summary);
        if (time > 0) {
            final String date = DateFormat.getDateTimeInstance().format(new Date(time));
            return String.format(prefix, date);
        }
        return String.format(prefix, context.getResources().getString(R.string.last_check_never));
    }

    public static String getLastCheck(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final long time = sharedPreferences.getLong(LAST_CHECK, 0);
        return buildLastCheckSummary(time, context);
    }

    public static String getFullLatestVersion(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(LATEST_VERSION, "");
    }

    public static void persistLatestVersion(String latestVersion, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(LATEST_VERSION, latestVersion).apply();
    }

    public static void persistLastCheck(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(LAST_CHECK, System.currentTimeMillis()).apply();
    }
}
