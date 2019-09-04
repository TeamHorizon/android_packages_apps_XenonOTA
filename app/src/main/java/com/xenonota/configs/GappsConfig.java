package com.xenonota.configs;

import android.content.Context;
import android.preference.PreferenceManager;

public class GappsConfig {

    private static final String GAPPS_VARIANT = "gapps_variant";
    private static final String GAPPS_URL = "gapps_url";
    private static final String GAPPS_LATESTVERSION = "gapps_latest_version";

    public static String getLatestVersion(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(GAPPS_LATESTVERSION, "");
    }

    public static String getURL(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(GAPPS_URL, "");
    }

    public static String getVariant(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(GAPPS_VARIANT, "nano");
    }

    public static void setLatestVersion(String releaseDate, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(GAPPS_LATESTVERSION, releaseDate).apply();
    }

    public static void setURL(String url, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(GAPPS_URL, url).apply();
    }

    public static void setVariant(String variant, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(GAPPS_VARIANT, variant).apply();
    }

}
