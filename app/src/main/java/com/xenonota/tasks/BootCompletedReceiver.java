/*
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
 *
 * Created by Devil7DK for XenonHD
 */

package com.xenonota.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.xenonota.configs.AppConfig;
import com.xenonota.utils.OTAUtils;
import com.xenonota.xml.OTADevice;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        try{
            Thread.sleep(30000); //Give some time to the system to warm up
            OTAUtils.logInfo("Checking whether auto update scheduled onBoot...");
            int UpdateInterval = AppConfig.getUpdateIntervalIndex(context.getApplicationContext());
            if(UpdateInterval == 4){
                OTAUtils.logInfo("Auto update scheduled onBoot. Checking for updates...");
                CheckUpdateTask otaChecker = CheckUpdateTask.getInstance(true, new CheckUpdateTask.UpdateCheckerCallback() {
                    @Override
                    public void processOTACheckResult(OTADevice device, boolean updateAvailable) {
                        OTAUtils.logInfo("OTACheckResult OnBoot - " + (updateAvailable ? "Update available" : "Update Not Available"));
                    }

                    @Override
                    public Context getContext() {
                        return context;
                    }
                });
                if (!otaChecker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    otaChecker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                }
            }
        }catch(Exception ex){
            OTAUtils.logError(ex);
        }
    }
}