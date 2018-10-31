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
 *
 */

package com.xenonota.dialogs;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xenonota.R;
import com.xenonota.utils.OTAUtils;

import java.io.File;

public class Downloader {
    private Context context;

    private long downloadID = 0;
    private String filepath = "";
    private DownloadManager manager;

    private AlertDialog progressDialog;
    private TextView progressTextView;
    private TextView sizeTextView;
    private ProgressBar progressBar;

    private DownloaderCallback callback;

    public interface DownloaderCallback {
        void onDownloadError(String reason);
        void onDownloadSuccess(String filepath);
        void onDownloadCancelled();
    }

    public Downloader(Context context, DownloaderCallback callback) {
        this.context = context;
        this.callback = callback;
        this.manager = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
    }

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            String reasonText;

            switch(message.what){
                case DownloadManager.STATUS_FAILED:
                    switch(message.arg1){
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            reasonText = "ERROR_CANNOT_RESUME";
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            reasonText = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            reasonText = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            reasonText = "ERROR_FILE_ERROR";
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            reasonText = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            reasonText = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            reasonText = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        default:
                            reasonText = "ERROR_UNKNOWN";
                            break;
                    }
                    callback.onDownloadError(reasonText);
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    OTAUtils.logError("Downloading Failed: Reason :" + reasonText);
                    break;
                case DownloadManager.STATUS_PAUSED:
                    switch(message.arg1){
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            reasonText = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case DownloadManager.PAUSED_UNKNOWN:
                            reasonText = "PAUSED_UNKNOWN";
                            break;
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            reasonText = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:
                            reasonText = "PAUSED_WAITING_TO_RETRY";
                            break;
                        default:
                            reasonText = "Unknown";
                    }
                    OTAUtils.logInfo("Downloading Paused: Reason :" + reasonText);
                    progressDialog.setTitle("Download Paused");
                    break;
                case DownloadManager.STATUS_PENDING:
                    OTAUtils.logInfo("Download Pending");
                    progressDialog.setTitle("Download Pending");
                    progressBar.setIndeterminate(true);
                    break;
                case DownloadManager.STATUS_RUNNING:
                    progressDialog.setTitle("Downloading");
                    double bytes_downloaded = message.arg1;
                    double bytes_total = message.arg2;
                    int progress = (int)((bytes_downloaded / bytes_total) * 100);
                    if (progressBar.isIndeterminate()) progressBar.setIndeterminate(false);
                    progressBar.setProgress(progress);
                    progressTextView.setText(context.getString(R.string.prog, progress));
                    sizeTextView.setText(context.getString(R.string.progress_size, OTAUtils.getSizeString(context, (int)bytes_downloaded), OTAUtils.getSizeString(context, (int)bytes_total)));
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    OTAUtils.logInfo("Download Finished.");
                    callback.onDownloadSuccess(filepath);
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    break;
            }
            return true;
        }
    });

    public void Start(String url, String filepath, String downloadType) {
        this.filepath = filepath;

        OTAUtils.logInfo("Starting Download URL:" + url + " Filename: " + filepath);

        View progressDialog_layout = View.inflate(context, R.layout.dialog_downloader, null);
        progressTextView =  progressDialog_layout.findViewById(R.id.progress_TextView);
        sizeTextView =  progressDialog_layout.findViewById(R.id.sizeTextView);
        progressBar =  progressDialog_layout.findViewById(R.id.progressBar);
        Button btn_Cancel =  progressDialog_layout.findViewById(R.id.btn_cancel);

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDownload();
            }
        });

        File file = new File(filepath);

        if (file.exists()) {
            if (!file.delete()) OTAUtils.logError("Unable to delete existing file");
        }

        Uri uri;
        try{
            uri = Uri.parse(url);
        }
        catch(Exception ex){
            OTAUtils.logError(ex);
            callback.onDownloadError("INVALID_URL");
            return;
        }

        DownloadManager.Request r = new DownloadManager.Request(uri);
        r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        r.setAllowedOverRoaming(false);
        r.setTitle("XenonOTA");
        r.setDescription("Downloading " + downloadType);
        r.setMimeType("application/zip");
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filepath);
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadID = manager.enqueue(r);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    try {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadID);
                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();

                        Message msg = handler.obtainMessage();

                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        msg.what = cursor.getInt(columnIndex);
                        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = cursor.getInt(columnReason);

                        switch(msg.what){
                            case DownloadManager.STATUS_SUCCESSFUL:
                            case DownloadManager.STATUS_FAILED:
                                downloading = false;
                                msg.arg1 = reason;
                                break;
                            case DownloadManager.STATUS_PAUSED:
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                                downloading = true;
                                msg.arg1 = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                msg.arg2 = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                break;
                        }

                        handler.sendMessage(msg);
                        cursor.close();
                    } catch (CursorIndexOutOfBoundsException ex) {
                        downloading = false;
                    }
                }
            }
        }).start();

        AlertDialog.Builder progressDialog_builder = new AlertDialog.Builder(context);
        progressDialog_builder.setTitle(R.string.downloading);
        progressDialog_builder.setView(progressDialog_layout);
        progressDialog_builder.setCancelable(false);
        progressDialog = progressDialog_builder.create();
        progressDialog.show();
    }

    private void cancelDownload(){
        manager.remove(downloadID);
        progressDialog.dismiss();
        callback.onDownloadCancelled();
    }
}
