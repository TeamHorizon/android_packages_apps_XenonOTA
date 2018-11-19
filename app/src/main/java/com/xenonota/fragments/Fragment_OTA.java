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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.support.v7.widget.CardView;
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
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.configs.MagiskConfig;
import com.xenonota.dialogs.Downloader;
import com.xenonota.dialogs.WaitDialogFragment;
import com.xenonota.tasks.CheckUpdateTask;
import com.xenonota.tasks.InitiateFlashTask;
import com.xenonota.tasks.MagiskDownloadTask;
import com.xenonota.utils.OTAUtils;
import com.xenonota.xml.OTADevice;

import java.io.File;

public class Fragment_OTA extends Fragment implements WaitDialogFragment.OTADialogListener, Downloader.DownloaderCallback, CheckUpdateTask.UpdateCheckerCallback {

    @ColorInt int colorAccent = android.R.attr.colorAccent;

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

    String url, filename, filePath;

    OTADevice ota_data;
    boolean updateAvailable;

    private CheckUpdateTask mCheckUpdateTask;
    private MagiskDownloadTask mCheckMagiskTask;
    private InitiateFlashTask mFlashTask;

    public OTADevice deviceFromExtras = null;

    public static Fragment_OTA newInstance() {
        return new Fragment_OTA();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getContext() != null) {
            TypedValue typedValue_accent = new TypedValue();
            Resources.Theme theme = getContext().getTheme();
            theme.resolveAttribute(android.R.attr.colorAccent, typedValue_accent, true);
            colorAccent = typedValue_accent.data;
        }

        view = inflater.inflate(R.layout.fragment_ota, container, false);
        assignObjects(view);
        assignEvents();
        getROMDetails();
        checkDeviceUpdates();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_ota, menu);
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
            case R.id.recovery:
                ReboottoRecovery();
                return true;
            case R.id.magisk:
                checkForMagisk();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressCancelled() {
        if (mCheckUpdateTask != null) {
            mCheckUpdateTask.cancel(true);
            mCheckUpdateTask = null;
        }
        if (mCheckMagiskTask != null) {
            mCheckMagiskTask.cancel(true);
            mCheckMagiskTask = null;
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
        lv_maintainer = view.findViewById(R.id.maintainer);

        btnDownload.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
        btnFlash.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
    }

    private void assignEvents(){
        if (getContext() == null) return;
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
        changelog_cv.setOnClickListener(new Button.OnClickListener() {
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

    public void checkDeviceUpdates(){
        if (deviceFromExtras != null) {
            processOTACheckResult(deviceFromExtras,true);
            deviceFromExtras = null;
        }
    }

    private void flashROM(){
        if (getContext() == null) return;
        
        View dLayout = View.inflate(getContext(), R.layout.dialog_flash, null);
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(getContext());
        dBuilder.setTitle(R.string.flash);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        Button btnStartFlash = dLayout.findViewById(R.id.btn_startFlash);
        final CheckBox cbFlashClean = dLayout.findViewById(R.id.cb_flash_clean);
        final CheckBox cbFlashGapps = dLayout.findViewById(R.id.cb_flash_gapps);
        final CheckBox cbFlashMagisk = dLayout.findViewById(R.id.cb_magisk);

        String gapps_path = AppConfig.getGappsZipPath(getContext().getApplicationContext());
        String magisk_path = AppConfig.getMagiskZipPath(getContext().getApplicationContext());
        if(("".equals(gapps_path.trim())) || !((new File(gapps_path)).exists())){
            cbFlashGapps.setEnabled(false);
        }
        if(("".equals(magisk_path.trim())) || !((new File(magisk_path)).exists())){
           cbFlashMagisk.setEnabled(false);
        }

        final Fragment_OTA frag = this;
        btnStartFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashTask = InitiateFlashTask.getInstance(false,frag,cbFlashClean.isChecked(),cbFlashGapps.isChecked(),cbFlashMagisk.isChecked());
                if (mFlashTask != null && !mFlashTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    mFlashTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        AlertDialog dDialog = dBuilder.create();
        dDialog.show();
    }

    private void ReboottoRecovery(){
        if (getContext() == null) return;
        View dLayout = View.inflate(getContext(), R.layout.custom_alertdialog, null);
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

    private void showChangelog() {
        if (getContext() == null) return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.changelog_title);
        builder.setMessage(ota_data.getChangelog());
        builder.setNeutralButton(R.string.open_in_browser, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void checkForUpdate(){
        mCheckUpdateTask = CheckUpdateTask.getInstance(false,this);
        if (!mCheckUpdateTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mCheckUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void processOTACheckResult(OTADevice device, boolean updateAvailable){
        if (getContext() == null) return;
        this.updateAvailable=updateAvailable;
        ota_data = device;
        if(updateAvailable){
            url = ota_data.getROMURL();
            filename = device.getFilename();
            filePath = device.getFilepath();
            btnFlash.setEnabled(device.isDownloadedAlready());
            if(device.isDownloadedAlready()){
                btnFlash.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
            }else{btnFlash.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.card_gray,null));}
            ota_controls.setVisibility(View.VISIBLE);
            changelog_cv.setVisibility(View.VISIBLE);
            otaStatus.setText(String.format(getString(R.string.update_available1), device.getLatestVersion(), device.getROMSize()));
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

    private void checkForMagisk(){
        mCheckMagiskTask = MagiskDownloadTask.getInstance(this);
        if (!mCheckMagiskTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mCheckMagiskTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void processMagiskResult(final MagiskConfig magiskConfig) {
        if (getContext() == null || magiskConfig == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.magisk);
        builder.setMessage(getString(R.string.magisk_message, magiskConfig.getVersion(), magiskConfig.getSize()));

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadMagisk(magiskConfig);
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void getROMDetails(){
        currentVersion.setText(OTAUtils.getProp("ro.xenonhd.version"));
        rom_version.setText(getString(R.string.xenonhd_version, Build.VERSION.RELEASE));
        build_type.setText(OTAUtils.getProp("ro.xenonhd.type"));
        device_name.setText(getString(R.string.device_name, Build.MODEL, Build.DEVICE));
        String maintainer_name=OTAUtils.getProp("ro.xenonhd.maintainer");
        if (maintainer_name != null && !maintainer_name.trim().isEmpty()) {
            maintainer.setText(maintainer_name);
        } else {
            lv_maintainer.setVisibility(View.GONE);
        }
    }

    void downloadROM() {
        if(updateAvailable && ota_data != null && getContext() != null){
            Downloader downloader = new Downloader(getContext(), this);
            downloader.Start(url, filePath, filename, "OTA");
        }
    }

    void downloadMagisk(MagiskConfig magiskConfig) {
        if (getContext() == null) return;
        Downloader downloader = new Downloader(getContext(), this);
        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + magiskConfig.getFilename();
        downloader.Start(magiskConfig.getUrl(), filePath, magiskConfig.getFilename(), getString(R.string.magisk));
    }

    @Override
    public void onDownloadError(String reason) {
        if (getContext() == null) return;

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setTitle(R.string.download_interrupted_title)
                .setMessage(getString(R.string.download_interrupted_msg, reason))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onDownloadSuccess(String filePath, String type) {
        if (getContext() == null) return;

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setTitle(R.string.download_complete_title);
        if ("OTA".equals(type)){
            builder.setMessage(R.string.download_complete_msg_ota);
            AppConfig.persistOtaZipPath(filePath,getContext().getApplicationContext());
            AppConfig.persistOtaZipChecksum(ota_data.getChecksum(),getContext().getApplicationContext());
            btnFlash.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
            btnFlash.setEnabled(true);
        } else if (getString(R.string.magisk).equals(type)) {
            builder.setMessage(R.string.download_complete_msg_magisk);
            AppConfig.persistMagiskZipPath(filePath,getContext().getApplicationContext());
        }
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    @Override
    public void onDownloadCancelled() {
        if (getContext() == null) return;

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setTitle(R.string.download_cancelled_title)
                .setMessage(R.string.download_cancelled_msg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
