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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xenonota.R;
import com.xenonota.dialogs.WaitDialogFragment;
import com.xenonota.tasks.CheckUpdateTask;
import com.xenonota.utils.OTAUtils;
import com.xenonota.xml.OTADevice;

import java.io.File;

public class Fragment_OTA extends Fragment implements WaitDialogFragment.OTADialogListener {
    View view;
    TextView currentVersion;
    TextView rom_version;
    TextView build_type;
    TextView device_name;
    TextView maintainer;

    TextView checkUpdate;
    TextView otaStatus;
    CardView changelog_cv;
    LinearLayout ota_controls;
    ImageView otaStatus_img;
    FrameLayout otaStatus_frame;
    CardView otaStatus_cv;
    LinearLayout lv_maintainer;

    Button btnDownload;
    Button btnFlash;
    TextView btnChangelog;

    String url, filename, filePath;

    OTADevice ota_data;
    boolean updateAvailable;

    private CheckUpdateTask mTask;
    private InitiateFlashTask mFlashTask;

    public static Fragment_OTA newInstance() {
        Fragment_OTA fragment = new Fragment_OTA();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ota, container, false);
        assignObjects(view);
        assignEvents();
        getROMDetails();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_ota, menu);
        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recovery:
                ReboottoRecovery();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressCancelled() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private void assignObjects(View view){
        currentVersion = view.findViewById(R.id.current_version);
        rom_version =  view.findViewById(R.id.device_rom);
        build_type =  view.findViewById(R.id.device_build);
        device_name =  view.findViewById(R.id.device_codename);
        maintainer =  view.findViewById(R.id.device_maintainer);
        checkUpdate = view.findViewById(R.id.checkupdates);
        otaStatus = view.findViewById(R.id.ota_status);
        changelog_cv = view.findViewById(R.id.cv_ota_changelog);
        ota_controls = view.findViewById(R.id.ota_controls);
        ota_controls.setVisibility(View.GONE);
        changelog_cv.setVisibility(View.GONE);
        otaStatus_cv = view.findViewById(R.id.cv_ota_state);
        otaStatus_frame = view.findViewById(R.id.frame_ota_state);
        otaStatus_img = view.findViewById(R.id.image_ota_state);
        btnDownload = view.findViewById(R.id.btn_download);
        btnFlash = view.findViewById(R.id.btn_flash);
        btnChangelog = view.findViewById(R.id.title_changelog);
        lv_maintainer = view.findViewById(R.id.maintainer);
    }

    private void assignEvents(){
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForUpdate();
            }
        });
        btnDownload.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadROM();
            }
        });
        btnChangelog.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangelog();
            }
        });
        btnFlash.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashROM();
            }
        });

        final String donateURL = OTAUtils.getProp("ro.xenonhd.donate");
        lv_maintainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!donateURL.isEmpty() && donateURL.startsWith("http")){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(donateURL));
                    PackageManager pm = getContext().getPackageManager();
                    if (browserIntent.resolveActivity(pm) != null) {
                        startActivity(browserIntent);
                    } else {
                        Toast toast = Toast.makeText(getContext(), R.string.toast_message, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });
    }

    private void flashROM(){
        LayoutInflater inflater = getLayoutInflater();
        View dLayout = inflater.inflate(R.layout.dialog_flash, null);
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(getContext());
        dBuilder.setTitle(R.string.flash);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        Button btnStartFlash = dLayout.findViewById(R.id.btn_startFlash);
        final CheckBox cbFlashGapps = dLayout.findViewById(R.id.cb_flash_gapps);
        final CheckBox cbFlashMagisk = dLayout.findViewById(R.id.cb_magisk);
        final Fragment_OTA frag = this;
        btnStartFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashTask = InitiateFlashTask.getInstance(false,frag,cbFlashGapps.isChecked(),cbFlashMagisk.isChecked());
                if (!mFlashTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    mFlashTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
                }
            }
        });
        AlertDialog dDialog = dBuilder.create();
        dDialog.show();
    }

    private void ReboottoRecovery(){
        LayoutInflater inflater = getLayoutInflater();
        View dLayout = inflater.inflate(R.layout.custom_alertdialog, null);
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(getContext());
        TextView message = dLayout.findViewById(R.id.custom_message);
        Button cancel = dLayout.findViewById(R.id.custom_negative);
        Button reboot = dLayout.findViewById(R.id.custom_positive);
        message.setText(R.string.reboot_warning);
        cancel.setText(R.string.cancel);
        reboot.setText(R.string.confirm);
        dBuilder.setTitle(R.string.confirm);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        final AlertDialog dDialog = dBuilder.create();
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTAUtils.rebootRecovery(getContext());
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

    private void showChangelog(){
        LayoutInflater inflater = getLayoutInflater();
        View dLayout = inflater.inflate(R.layout.dialog_changelog, null);
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(getContext());
        TextView changelog = dLayout.findViewById(R.id.txt_changelog);
        Button close = dLayout.findViewById(R.id.btn_close_changelog);
        Button openInBrowser = dLayout.findViewById(R.id.btn_open_in_browser);
        changelog.setMovementMethod(new ScrollingMovementMethod());
        changelog.setText(ota_data.getChangelog());
        dBuilder.setTitle(R.string.changelog_title);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        final AlertDialog dDialog = dBuilder.create();
        openInBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ota_data.getChangelogURL()));
                PackageManager pm = getContext().getPackageManager();
                if (browserIntent.resolveActivity(pm) != null) {
                    startActivity(browserIntent);
                } else {
                    Toast toast = Toast.makeText(getContext(), R.string.toast_message, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dDialog.dismiss();
            }
        });
        dDialog.show();
    }

    private void checkForUpdate(){
        mTask = CheckUpdateTask.getInstance(false,this);
        if (!mTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
        }
    }

    public void processOTACheckResult(OTADevice device, boolean updateAvailable){
        this.updateAvailable=updateAvailable;
        ota_data = device;
        if(updateAvailable){
            url = ota_data.getROMURL();
            filename = url.substring(url.lastIndexOf('/') + 1);
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + filename;
            btnFlash.setEnabled(device.isDownloadedAlready());
            if(device.isDownloadedAlready()){
                btnFlash.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.colorPrimaryDark,null));
            }else{btnFlash.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.card_gray,null));}
            ota_controls.setVisibility(View.VISIBLE);
            changelog_cv.setVisibility(View.VISIBLE);
            otaStatus.setText(String.format(getString(R.string.update_available1), device.getLatestVersion()));
            otaStatus_cv.setCardBackgroundColor(getResources().getColor(R.color.card_green,null));
            otaStatus_frame.setBackgroundColor(getResources().getColor(R.color.card_green,null));
            otaStatus_img.setImageDrawable(getResources().getDrawable(R.drawable.ic_ota_available,null));
        }
        else{
            url = "";
            filename = "";
            filePath = "";
            otaStatus.setText(R.string.no_update_available);
            ota_controls.setVisibility(View.GONE);
            changelog_cv.setVisibility(View.GONE);
            otaStatus_cv.setCardBackgroundColor(getResources().getColor(R.color.card_gray,null));
            otaStatus_frame.setBackgroundColor(getResources().getColor(R.color.card_gray,null));
            otaStatus_img.setImageDrawable(getResources().getDrawable(R.drawable.ic_ota_notavailable,null));
        }
    }

    private void getROMDetails(){
        currentVersion.setText(OTAUtils.getProp("ro.xenonhd.version"));
        rom_version.setText("XenonHD " + Build.VERSION.RELEASE);
        build_type.setText(OTAUtils.getProp("ro.xenonhd.type"));
        device_name.setText(Build.MODEL + " (" + Build.DEVICE + ")");
        maintainer.setText(OTAUtils.getProp("ro.xenonhd.maintainer"));
    }

    void downloadROM() {

    }
}
