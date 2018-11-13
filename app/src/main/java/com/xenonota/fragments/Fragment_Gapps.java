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
 * Parts of this code was took from the app "RnOpenGapps" - https://github.com/hjthjthjt/RnOpenGApps
 */

package com.xenonota.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.configs.DeviceConfig;
import com.xenonota.configs.GappsConfig;
import com.xenonota.dialogs.Downloader;
import com.xenonota.tasks.CheckGappsTask;
import com.xenonota.dialogs.OpenFileDialog;

import java.io.File;

public class Fragment_Gapps extends Fragment implements Downloader.DownloaderCallback {

    public static Fragment_Gapps newInstance() {
        return new Fragment_Gapps();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gapps, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_gapps, menu);
        TypedValue typedValue_accent = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue_accent, true);
        @ColorInt int colorAccent = typedValue_accent.data;
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(colorAccent), 0,     spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_gapps_zip:
                ChooseGappsZIP();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadDeviceInfo();
        stateGApps();
        if (getActivity() != null) {
            getActivity().findViewById(R.id.state_latest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickDownload();
                }
            });
        }
        CheckGappsTask.getInstance(this).execute();
        setHasOptionsMenu(true);
    }

    private void ChooseGappsZIP(){
        if (getContext() == null) return;
        OpenFileDialog dialog = new OpenFileDialog(getContext());
        dialog.setFilter("(.*).zip");
        dialog.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
            @Override
            public void OnSelectedFile(String fileName) {
                AppConfig.persistGappsZipPath(fileName,getContext().getApplicationContext());
                Toast.makeText(getContext(), getString(R.string.gapps_set,fileName),Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    public static boolean isInstall(Context context, String packageName){
        try {
            PackageInfo pkginfo = context.getPackageManager().getPackageInfo(packageName.trim(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            if(pkginfo!=null){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void clickDownload(){
        if (getContext() == null) return;
        View dLayout = View.inflate(getContext(),  R.layout.custom_alertdialog, null);
        android.support.v7.app.AlertDialog.Builder dBuilder = new android.support.v7.app.AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
        TextView message = dLayout.findViewById(R.id.custom_message);
        Button cancel = dLayout.findViewById(R.id.custom_negative);
        Button download = dLayout.findViewById(R.id.custom_positive);
        message.setText(getString(R.string.download_confirm_msg, DeviceConfig.getRelease(), DeviceConfig.getCPU(), GappsConfig.getVariant(getContext())));
        cancel.setText(R.string.cancel);
        download.setText(R.string.download);
        dBuilder.setTitle(R.string.confirm);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        final android.support.v7.app.AlertDialog dDialog = dBuilder.create();
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadGApps("https://github.com/opengapps/" + DeviceConfig.getCPU() + "/releases/download/"+ GappsConfig .getLatestVersion(getContext()) + "/open_gapps-" + DeviceConfig.getCPU() + "-" + DeviceConfig.getRelease() + "-" + GappsConfig.getVariant(getContext()) + "-" + GappsConfig.getLatestVersion(getContext()) + ".zip");
                dDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dDialog.dismiss();
            }
        });
        dDialog.show();
    }

    public void stateGApps() {
        if (getActivity() == null) return;
        boolean stateofGApps = isInstall(getActivity().getApplicationContext(), "com.google.android.gms");
        ImageView status_icon = getActivity().findViewById(R.id.status_icon);
        FrameLayout status_container = getActivity().findViewById(R.id.status_container);
        TextView status_text = getActivity().findViewById(R.id.status_text);
        if (stateofGApps) {
            status_icon.setImageResource(R.drawable.ic_check_circle);
            status_container.setBackgroundResource(R.color.card_green);
            status_text.setText(R.string.gapps_ok);
            status_text.setTextColor(this.getResources().getColor(R.color.card_green, null));
        } else {
            status_icon.setImageResource(R.drawable.ic_warning);
            status_container.setBackgroundResource(R.color.warning);
            status_text.setText(R.string.gapps_warning);
            status_text.setTextColor(this.getResources().getColor(R.color.warning, null));
        }
    }

    public void loadDeviceInfo(){
        if (getActivity() == null) return;
        TextView myDeviceModelText = getActivity().findViewById(R.id.my_device_model);
        TextView myDeviceOS = getActivity().findViewById(R.id.my_device_sdk);
        TextView myDeviceCPU = getActivity().findViewById(R.id.my_device_cpu);
        myDeviceModelText.setText(DeviceConfig.getModel());
        myDeviceCPU.setText(DeviceConfig.getCPU());
        myDeviceOS.setText(DeviceConfig.getOSVersion());
    }

    void downloadGApps(String url) {
        if (getContext() == null) return;

        String filename = url.substring(url.lastIndexOf('/') + 1);
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + filename;
        Downloader downloader = new Downloader(getContext(), this);
        downloader.Start(url, filePath, filename, "GApps");
    }

    @Override
    public void onDownloadError(String reason) {
        if (getContext() == null) return;

        android.support.v7.app.AlertDialog.Builder builder;
        builder = new android.support.v7.app.AlertDialog.Builder(getContext(),R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(R.string.download_interrupted_title)
                .setMessage(getString(R.string.download_interrupted_msg, reason))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onDownloadSuccess(String filePath, String type) {
        if (getContext() == null) return;

        AppConfig.persistGappsZipPath(filePath,getContext().getApplicationContext());

        android.support.v7.app.AlertDialog.Builder builder;
        builder = new android.support.v7.app.AlertDialog.Builder(getContext(),R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(R.string.download_complete_title)
                .setMessage(R.string.download_complete_msg_gapps)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onDownloadCancelled() {
        if (getContext() == null) return;

        android.support.v7.app.AlertDialog.Builder builder;
        builder = new android.support.v7.app.AlertDialog.Builder(getContext(),R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(R.string.download_cancelled_title)
                .setMessage(R.string.download_cancelled_msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}