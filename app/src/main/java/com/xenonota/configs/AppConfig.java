/**
 * Copyright (C) 2018 XenonHD
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
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xenonota.R;
import com.xenonota.tasks.OTAService;
import com.xenonota.utils.OTAUtils;

import java.text.DateFormat;
import java.util.Date;

public final class AppConfig {

    private static final String UPDATE_INTERVAL = "update_interval";

    private static final String GAPPS_VARIANT = "gapps_variant";
    private static final String PREFERRED_TYPE = "preferred_type";

    private static final String OTA_ZIP_PATH = "ota_zip_path";
    private static final String OTA_ZIP_CHECKSUM = "ota_zip_md5";

    private static final String GAPPS_ZIP_PATH = "gapps_zip_path";

    private static final String MAGISK_ZIP_PATH = "magisk_zip_path";

    private AppConfig() {
    }

    public static String getGappsVariant(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(GAPPS_VARIANT, "nano");
    }

    public static String getOtaZipPath(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(OTA_ZIP_PATH, "");
    }

    public static String getOtaZipChecksum(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(OTA_ZIP_CHECKSUM, "");
    }

    public static String getGappsZipPath(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(GAPPS_ZIP_PATH, "");
    }

    public static String getMagiskZipPath(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(MAGISK_ZIP_PATH, "");
    }

    public static String getPreferredType(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREFERRED_TYPE, "Experimental");
    }

    public static void persistGappsVariant(String variant, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(GAPPS_VARIANT, variant).apply();
    }

    public static void persistOtaZipPath(String path, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(OTA_ZIP_PATH,path).apply();
    }

    public static void persistOtaZipChecksum(String checksum, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(OTA_ZIP_CHECKSUM,checksum).apply();
    }

    public static void persistGappsZipPath(String path, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(GAPPS_ZIP_PATH,path).apply();
    }


    public static void persistMagiskZipPath(String path, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(MAGISK_ZIP_PATH,path).apply();
    }

    public static void persistPreferredVersion(String preferredType, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREFERRED_TYPE, preferredType).apply();
    }

    public static void persistUpdateIntervalIndex(int intervalIndex, Context context) {
        long intervalValue;
        switch (intervalIndex) {
            case 0:
                intervalValue = 0;
                break;
            case 1:
                intervalValue = AlarmManager.INTERVAL_HOUR;
                break;
            case 2:
                intervalValue = AlarmManager.INTERVAL_HALF_DAY;
                break;
            case 3:
                intervalValue = AlarmManager.INTERVAL_DAY;
                break;
            case 4:
                intervalValue = 0;
                break;
            default:
                intervalValue = AlarmManager.INTERVAL_HALF_DAY;
                break;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(UPDATE_INTERVAL, intervalIndex).apply();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (intervalValue > 0) {
            jobScheduler.cancel(0);
            jobScheduler.schedule(new JobInfo.Builder(0,new ComponentName(context,OTAService.class))
                    .setPeriodic(intervalValue)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build());
            OTAUtils.toast(R.string.autoupdate_enabled, context);
        } else {
            jobScheduler.cancel(0);
            if(intervalIndex == 0){
                OTAUtils.toast(R.string.autoupdate_disabled, context);
            }
        }
    }

    public static int getUpdateIntervalIndex(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(UPDATE_INTERVAL, 0);
    }

}