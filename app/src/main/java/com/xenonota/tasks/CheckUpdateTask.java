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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.xenonota.MainActivity;
import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.configs.OTAConfig;
import com.xenonota.configs.OTAVersion;
import com.xenonota.dialogs.WaitDialogHandler;
import com.xenonota.fragments.Fragment_OTA;
import com.xenonota.utils.OTAUtils;
import com.xenonota.xml.OTADevice;
import com.xenonota.xml.OTAParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class CheckUpdateTask extends AsyncTask<Context, Void, OTADevice> {

    private static CheckUpdateTask mInstance = null;
    private final Handler mHandler = new WaitDialogHandler();
    private Context mContext;
    private boolean mIsBackgroundThread;

    private Fragment_OTA frag;

    private CheckUpdateTask(boolean isBackgroundThread) {
        this.mIsBackgroundThread = isBackgroundThread;
    }

    public static CheckUpdateTask getInstance(boolean isBackgroundThread, Fragment_OTA frag) {
        if (mInstance == null) {
            mInstance = new CheckUpdateTask(isBackgroundThread);
        }
        mInstance.frag = frag;
        return mInstance;
    }

    private static boolean isConnectivityAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    @Override
    protected OTADevice doInBackground(Context... params) {
        mContext = params[0];

        if (!isConnectivityAvailable(mContext)) {
            return null;
        }

        showWaitDialog();

        OTADevice official = null;
        OTADevice experimental = null;
        OTADevice final_ota = null;
        String deviceName = OTAUtils.getDeviceName(mContext);
        OTAUtils.logInfo("deviceName: " + deviceName);
        if (!deviceName.isEmpty()) {
            official = fetchURL(OTAConfig.getInstance(mContext).getOfficialOtaUrl(),deviceName);
            if(AppConfig.getPreferredType(mContext)=="Experimental"){experimental = fetchURL(OTAConfig.getInstance(mContext).getExperimentalOtaUrl(),deviceName);}
            if(official==null && experimental!=null){
                final_ota = experimental;
            }else if(official!=null && experimental==null){
                final_ota = official;
            }else if(official!=null && experimental != null){
                boolean result = OTAVersion.checkVersions(official.getLatestVersion(), experimental.getLatestVersion(),mContext);
                if(result){
                 final_ota = official;
                }else{
                    final_ota = experimental;
                }
            }else{final_ota = null;}
        }

        return final_ota;
    }

    @Override
    protected void onPostExecute(OTADevice device) {
        super.onPostExecute(device);

        if (device == null) {
            showToast(R.string.check_update_failed);
        } else {
            String latestVersion = device.getLatestVersion();
            boolean updateAvailable = OTAVersion.checkServerVersion(latestVersion, mContext);
            if (updateAvailable) {
                showNotification(mContext);
                showToast(R.string.update_available);
            } else {
                showToast(R.string.no_update_available);
            }
            if(frag!=null){frag.processOTACheckResult(device,updateAvailable);}
        }

        hideWaitDialog();

        mInstance = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

    private OTADevice fetchURL(String otaUrl, String deviceName){
        OTADevice device = null;
        try {
            InputStream is = OTAUtils.downloadURL(otaUrl);
            if (is != null) {
                final String releaseType = OTAConfig.getInstance(mContext).getReleaseType();
                device = OTAParser.getInstance().parse(is, deviceName, releaseType);
                is.close();
            }
        } catch (IOException | XmlPullParserException e) {
            OTAUtils.logError(e);
        }
        return device;
    }

    private void showWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_SHOW_DIALOG);
            msg.obj = mContext;
            msg.arg1 = R.string.dialog_message;
            mHandler.sendMessage(msg);
        }
    }

    private void hideWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_CLOSE_DIALOG);
            mHandler.sendMessage(msg);
        }
    }

    private void showToast(int messageId) {
        if (!mIsBackgroundThread) {
            OTAUtils.toast(messageId, mContext);
        }
    }

    private void showNotification(Context context) {
        if (mIsBackgroundThread) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notifyID = 1;
            String id = "xenonota_channel";
            CharSequence name = context.getString(R.string.xenonota_channel);
            String description = context.getString(R.string.xenonota_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            notificationManager.createNotificationChannel(mChannel);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                    .setSmallIcon(R.drawable.ic_notification_xenonota)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_message))
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setChannelId(id);

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pendingIntent);
            notificationManager.notify(notifyID, mBuilder.build());
        }
    }
}
