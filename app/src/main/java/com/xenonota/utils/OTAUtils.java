/*
 * Copyright (C) 2018 Chandra Poerwanto
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

package com.xenonota.utils;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.xenonota.R;
import com.xenonota.configs.OTAConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public final class OTAUtils {

    private static final String TAG = "XenonOTA";
    private static final boolean DEBUG = true;

    private OTAUtils() {
    }

    public static void logError(Exception e) {
        if (DEBUG) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void logError(String e) {
        if (DEBUG) {
            Log.e(TAG, e);
        }
    }

    public static void logInfo(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void toast(int messageId, Context context) {
        if (context != null) {
            Toast.makeText(context, context.getResources().getString(messageId),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String getDeviceName(Context context) {
        String propName = OTAConfig.getInstance(context).getDeviceSource();
        return OTAUtils.getProp(propName);
    }

    public static String getProp(String propName) {
        Process p = null;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line=br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
            return result;
    }

    public static String runCommand(String command) {
        try {
            StringBuilder output = new StringBuilder();
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            p.waitFor();
            return output.toString();
        } catch (InterruptedException | IOException e) {
            logError(e);
        }
        return "";
    }

    public static InputStream downloadURL(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        logInfo("downloadStatus: " + conn.getResponseCode());
        return conn.getInputStream();
    }

    public static void rebootRecovery(Context context){
        ((PowerManager) context.getApplicationContext().getSystemService(Activity.POWER_SERVICE)).reboot("recovery-update");
    }

    @NonNull
    public static String getSizeString(@NonNull final Context context, long bytes) {
        if (bytes < 0) bytes = 0;
        double kb = (double) bytes / (double) 1000;
        double mb = kb / (double) 1000;
        final DecimalFormat decimalFormat = new DecimalFormat(".##");
        if (mb >= 1) {
            return context.getString(R.string.size_mb, decimalFormat.format(mb));
        } else if (kb >= 1) {
            return context.getString(R.string.size_kb, decimalFormat.format(kb));
        } else {
            return context.getString(R.string.size_bytes, bytes);
        }
    }
}
