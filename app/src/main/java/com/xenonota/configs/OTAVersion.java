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

package com.xenonota.configs;

import android.content.Context;

import com.xenonota.utils.OTAUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Pattern;

public class OTAVersion {

    private static final String UNAME_R = "uname -r";

    private static String getFullLocalVersion(Context context) {
        String source = OTAConfig.getInstance(context).getVersionSource();
        String sourceString;
        if (source.equalsIgnoreCase(UNAME_R)) {
            sourceString = OTAUtils.runCommand(UNAME_R);
        } else {
            sourceString = OTAUtils.getProp(source);
        }
        return sourceString;
    }

    public static boolean checkServerVersion(String serverVersion, String serverBuildTime, Context context) {
        String localVersion = getFullLocalVersion(context);
        String localBuildTime = OTAUtils.getProp("ro.build.date.utc");
        localVersion = extractVersionFrom(localVersion, context);
        serverVersion = extractVersionFrom(serverVersion, context);

        OTAUtils.logInfo("serverVersion: " + serverVersion);
        OTAUtils.logInfo("localVersion: " + localVersion);

        return compareVersion(serverVersion, localVersion, serverBuildTime, localBuildTime, context);
    }

    public static boolean checkVersions(String version1, String version2, String buildTime1, String buildTime2, Context context) {
        version1 = extractVersionFrom(version1, context);
        version2 = extractVersionFrom(version2, context);

        OTAUtils.logInfo("Version 1: " + version1);
        OTAUtils.logInfo("Version 2: " + version2);

        return compareVersion(version1, version2, buildTime1, buildTime2, context);
    }

    private static boolean compareVersion(String version1, String version2, String version1BuildTime, String version2BuildTime, Context context) {
        boolean versionIsNew = false;

        if (version1.isEmpty() || version2.isEmpty() || version1BuildTime.isEmpty() || version2BuildTime.isEmpty()) {
            return false;
        }

        final SimpleDateFormat format = OTAConfig.getInstance(context).getFormat();
        if (format == null) {
            try {
                int version1Number = Integer.parseInt(version1.replaceAll("[\\D]", ""));
                int version2Number = Integer.parseInt(version2.replaceAll("[\\D]", ""));
                versionIsNew = version1Number > version2Number;
            } catch (NumberFormatException e) {
                OTAUtils.logError(e);
            }
        } else {
            try {
                Date version1Date = format.parse(version1);
                Date version2Date = format.parse(version2);
                versionIsNew = version1Date.after(version2Date);

                if (version1Date.compareTo(version2Date) == 0) {
                    OTAUtils.logInfo("Build IDs are Same! Comparing Build Time...");
                    ZonedDateTime buildTime1 = utcToDateTime(version1BuildTime);
                    ZonedDateTime buildTime2 = utcToDateTime(version2BuildTime);
                    OTAUtils.logInfo("Build Time 1 : " + buildTime1.toString());
                    OTAUtils.logInfo("Build Time 2 : " + buildTime2.toString());
                    versionIsNew = buildTime1.isAfter(buildTime2);
                }
            } catch (ParseException e) {
                OTAUtils.logError(e);
            }
        }

        return versionIsNew;
    }

    private static ZonedDateTime utcToDateTime(String utc) {
        long epoch = Long.parseLong(utc);
        Instant instant = Instant.ofEpochSecond(epoch);
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static String extractVersionFrom(String str, Context context) {
        String version = "";

        if (!str.isEmpty()) {
            String delimiter = OTAConfig.getInstance(context).getDelimiter();
            int position = OTAConfig.getInstance(context).getPosition();

            if (delimiter.isEmpty()) {
                version = str;
            } else {
                if (delimiter.equals(".")) {
                    delimiter = Pattern.quote(".");
                }
                String[] tokens = str.split(delimiter);
                if (position > -1 && position < tokens.length) {
                    version = tokens[position];
                }
            }
        }

        return version;
    }
}
