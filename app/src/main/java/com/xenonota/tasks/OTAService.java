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

package com.xenonota.tasks;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;

import com.xenonota.utils.OTAUtils;
import com.xenonota.xml.OTADevice;

public class OTAService extends JobService implements CheckUpdateTask.UpdateCheckerCallback {

    public boolean onStartJob(final JobParameters jobParameters) {

        try {
            CheckUpdateTask otaChecker = CheckUpdateTask.getInstance(true,this);
            if (!otaChecker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                otaChecker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            }
        } catch (Exception ex) {
            OTAUtils.logError(ex);
        }

        return false;
    }

    @Override
    public void processOTACheckResult(OTADevice device, boolean updateAvailable) {
        OTAUtils.logInfo("OTACheckResult JobService - " + (updateAvailable ? "Update available" : "Update Not Available"));
    }

    @Override
    public Context getContext() {
        return  getApplicationContext();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return false;

    }
}
