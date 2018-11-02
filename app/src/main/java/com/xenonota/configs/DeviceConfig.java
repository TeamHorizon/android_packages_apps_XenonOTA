package com.xenonota.configs;

import android.os.Build;

import java.util.Properties;

public class DeviceConfig extends Properties {

    public static String getRelease() {
        String Release = android.os.Build.VERSION.RELEASE;
        if(Release.length() >= 3)
            return Release.substring(0,3);
        else
            return Release + ".0";
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getCPU() {
        String CPU = Build.SUPPORTED_ABIS[0];
        if(CPU.contains("armeabi")){
            return "arm";
        }else if(CPU.contains("arm64")){
            return "arm64";
        }else if(CPU.contains("x86_64")){
            return "x86_64";
        }else{
            return "x86";
        }
    }

    public static int getSDK() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String getOSVersion() {
        return "Android " + getRelease() + " (API "+ getSDK() + ")";
    }

}
