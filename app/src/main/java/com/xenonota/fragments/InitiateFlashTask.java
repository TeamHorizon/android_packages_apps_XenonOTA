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

package com.xenonota.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.dialogs.WaitDialogHandler;
import com.xenonota.utils.ORSUtils;
import com.xenonota.utils.OTAUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class InitiateFlashTask extends AsyncTask<Void, Void, String> {

    private static InitiateFlashTask mInstance = null;
    private final Handler mHandler = new WaitDialogHandler();
    private boolean mIsBackgroundThread;

    private Fragment_OTA frag;
    private boolean flash_gapps;
    private boolean flash_magisk;

    private static String ota_zip;
    private static String ota_md5;
    private static String gapps_path;
    private static String magisk_path;

    private InitiateFlashTask(boolean isBackgroundThread) {
        this.mIsBackgroundThread = isBackgroundThread;
    }

    public static InitiateFlashTask getInstance(boolean isBackgroundThread, Fragment_OTA frag, boolean flash_gapps, boolean flash_magisk) {
        if (mInstance == null) {
            mInstance = new InitiateFlashTask(isBackgroundThread);
        }
        if (frag == null || frag.getContext() == null) return null;
        mInstance.flash_gapps = flash_gapps;
        mInstance.flash_magisk = flash_magisk;
        mInstance.frag = frag;

        ota_zip = AppConfig.getOtaZipPath(frag.getContext().getApplicationContext());
        ota_md5 = AppConfig.getOtaZipChecksum(frag.getContext().getApplicationContext());
        gapps_path = AppConfig.getGappsZipPath(frag.getContext().getApplicationContext());
        magisk_path = AppConfig.getMagiskZipPath(frag.getContext().getApplicationContext());
        return mInstance;
    }


    @Override
    protected String doInBackground(Void... params) {
        showWaitDialog();

        String fileChecksum = "";

        try {
            String command = "md5sum \'" + ota_zip + "\'\n";
            try {
                String line;
                Process process = Runtime.getRuntime().exec("sh");
                OutputStream stdin = process.getOutputStream();
                InputStream stdout = process.getInputStream();

                stdin.write(command.getBytes());
                stdin.flush();

                stdin.close();
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    fileChecksum = line.split(" ")[0];
                }
                br.close();

                process.waitFor();
                process.destroy();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            OTAUtils.logInfo("OTA Checksum from server\t: " + ota_md5);
            OTAUtils.logInfo("OTA Checksum of file in local\t: " + fileChecksum);
        }
        catch (Exception e) {e.printStackTrace();}

        return fileChecksum;
    }

    @Override
    protected void onPostExecute(String md5) {
        super.onPostExecute(md5);

        if (frag == null || frag.getContext() == null) return;

        if(ota_md5.equals(md5)){
            ORSUtils.clear();
            ORSUtils.InstallZip(ota_zip);
            if(flash_gapps){
                if( !("".equals(gapps_path.trim())) && (new File(gapps_path)).exists()){
                    ORSUtils.InstallZip(gapps_path);
                }
            }
            if(flash_magisk){
                if(!("".equals(magisk_path.trim())) && (new File(magisk_path)).exists()){
                    ORSUtils.InstallZip(magisk_path);
                }
            }
            OTAUtils.rebootRecovery(frag.getContext());
            hideWaitDialog();
            mInstance = null;
        }else{
            hideWaitDialog();
            AlertDialog.Builder dBuilder = new AlertDialog.Builder(frag.getContext());
            dBuilder.setTitle(R.string.failed);
            dBuilder.setMessage(R.string.verification_failed);
            dBuilder.setCancelable(true);
            dBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dDialog = dBuilder.create();
            dDialog.show();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

    private void showWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_SHOW_DIALOG);
            msg.obj = frag.getContext();
            msg.arg1 = R.string.verifying_package;
            mHandler.sendMessage(msg);
        }
    }

    private void hideWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_CLOSE_DIALOG);
            mHandler.sendMessage(msg);
        }
    }
}
