/**
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xenonota.MainActivity;
import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.utils.OTAUtils;
import com.xenonota.dialogs.OpenFileDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fragment_Gapps extends Fragment {

    public static Fragment_Gapps newInstance() {
        return new Fragment_Gapps();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gapps, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_gapps, menu);
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

    private void ChooseGappsZIP(){
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

    CardView do_card;
    SharedPreferences data_of_download;
    String a_v;
    String c_u;
    String v_r;
    SharedPreferences.Editor save_data;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity act = getActivity();
        Log.i("TTTT",osRelease );
        if(osRelease.length() >= 3)
            a_v = osRelease.substring(0,3);
        else
            a_v = osRelease + ".0";
        if(osCPU.contains("armeabi")){
            c_u="arm";
        }else if(osCPU.contains("arm64")){
            c_u="arm64";
        }else if(osCPU.contains("x86_64")){
            c_u="x86_64";
        }else if(osCPU.equals("x86")){
            c_u="x86";
        }
        v_r = AppConfig.getGappsVariant(getContext().getApplicationContext());
        if(act!=null){
            act.setTitle(R.string.app_name);
            data_of_download = act.getSharedPreferences("data", Context.MODE_PRIVATE);
            save_data = act.getSharedPreferences("data",Context.MODE_PRIVATE).edit();
            do_card = act.findViewById(R.id.state_latest);
        }
        myDeviceInfo();
        stateGApps();
        GetLatestVersion();
        do_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickDownload();
            }
        });
        setHasOptionsMenu(true);

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

    private void GetLatestVersion(){
        String inCPU = c_u;
        if(inCPU.equals("arm")){
            getLatest_ARM();
        }else if(inCPU.equals("arm64")){
            getLatest_ARM64();
        }else if(inCPU.equals("x86_64")){
            getLatest_x86_64();
        }else if(inCPU.equals("x86")){
            getLatest_x86();
        }
    }

    private void clickDownload(){
        final String date = data_of_download.getString("date","");
        v_r = AppConfig.getGappsVariant(getContext().getApplicationContext());

        LayoutInflater inflater = getLayoutInflater();
        View dLayout = inflater.inflate(R.layout.custom_alertdialog, null);
        android.support.v7.app.AlertDialog.Builder dBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
        TextView message = dLayout.findViewById(R.id.custom_message);
        Button cancel = dLayout.findViewById(R.id.custom_negative);
        Button download = dLayout.findViewById(R.id.custom_positive);
        message.setText(getString(R.string.download_confirm_1)+a_v+getString(R.string.download_confirm_2)+c_u+getString(R.string.download_confirm_3)+v_r);
        cancel.setText(R.string.cancel);
        download.setText(R.string.download);
        dBuilder.setTitle(R.string.confirm);
        dBuilder.setView(dLayout);
        dBuilder.setCancelable(true);
        final android.support.v7.app.AlertDialog dDialog = dBuilder.create();
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadGapps("https://github.com/opengapps/"+c_u+"/releases/download/"+date+"/open_gapps-"+c_u+"-"+a_v+"-"+v_r+"-"+date+".zip");
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

    public void stateGApps(){
        boolean stateofGApps = isInstall(getActivity().getApplicationContext(),"com.google.android.gms");
        ImageView status_icon = getActivity().findViewById(R.id.status_icon);
        FrameLayout status_container = getActivity().findViewById(R.id.status_container);
        TextView status_text = getActivity().findViewById(R.id.status_text);
        if(stateofGApps){
            status_icon.setImageResource(R.drawable.ic_check_circle);
            status_container.setBackgroundResource(R.color.card_green);
            status_text.setText(R.string.gapps_ok);
            status_text.setTextColor(this.getResources().getColor(R.color.card_green,null));
        }else{
            status_icon.setImageResource(R.drawable.ic_warning);
            status_container.setBackgroundResource(R.color.warning);
            status_text.setText(R.string.gapps_warning);
            status_text.setTextColor(this.getResources().getColor(R.color.warning));
        }
    }

    String osRelease = android.os.Build.VERSION.RELEASE;
    String osModel = Build.MODEL;
    String osCPU = Build.SUPPORTED_ABIS[0];
    int osSDK = android.os.Build.VERSION.SDK_INT;
    public void myDeviceInfo(){
        TextView myDeviceModelText = getActivity().findViewById(R.id.my_device_model);
        TextView myDeviceOS = getActivity().findViewById(R.id.my_device_sdk);
        TextView myDeviceCPU = getActivity().findViewById(R.id.my_device_cpu);
        TextView status_latest = getActivity().findViewById(R.id.status_latest);
        myDeviceModelText.setText(osModel);
        myDeviceCPU.setText(c_u);
        myDeviceOS.setText("Android "+osRelease+" (API "+osSDK+")");
        status_latest.setText(R.string.loading);
    }


    public int progress = 0;

    public String arm64;
    public void getLatest_ARM64(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://api.github.com/repos/opengapps/arm64/releases/latest");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) !=null){
                        response.append(line);
                    }
                    arm64 = response.toString();
                    JSONObject json = new JSONObject(arm64);
                    String version = json.getString("tag_name");
                    save_data.putString("date",version).apply();
                    progress = 100;
                    TextView status_latest = getActivity().findViewById(R.id.status_latest);
                    status_latest.setText(getString(R.string.latest_release)+" ARM64: "+version);
                    ProgressBar progressBar = getActivity().findViewById(R.id.progress_bar);
                    if(progress ==100){
                        progressBar.setVisibility(View.INVISIBLE);
                        ImageView status_github = getActivity().findViewById(R.id.status_github);
                        status_github.setImageResource(R.drawable.ic_github);
                        status_github.setVisibility(View.VISIBLE);
                        do_card.setClickable(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader!=null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public String arm;
    public void getLatest_ARM(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://api.github.com/repos/opengapps/arm/releases/latest");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) !=null){
                        response.append(line);
                    }
                    arm = response.toString();
                    JSONObject json = new JSONObject(arm);
                    String version = json.getString("tag_name");
                    save_data.putString("date",version).apply();
                    progress = 100;
                    TextView status_latest = getActivity().findViewById(R.id.status_latest);
                    status_latest.setText(getString(R.string.latest_release)+" ARM: "+version);
                    ProgressBar progressBar = getActivity().findViewById(R.id.progress_bar);
                    if(progress ==100){
                        progressBar.setVisibility(View.INVISIBLE);
                        ImageView status_github = getActivity().findViewById(R.id.status_github);
                        status_github.setImageResource(R.drawable.ic_github);
                        status_github.setVisibility(View.VISIBLE);
                        do_card.setClickable(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader!=null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public String x86;
    public void getLatest_x86(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://api.github.com/repos/opengapps/x86/releases/latest");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) !=null){
                        response.append(line);
                    }
                    x86 = response.toString();
                    JSONObject json = new JSONObject(x86);
                    String version = json.getString("tag_name");
                    save_data.putString("date",version).apply();
                    progress = 100;
                    TextView status_latest = getActivity().findViewById(R.id.status_latest);
                    status_latest.setText(getString(R.string.latest_release)+" x86: "+version);
                    ProgressBar progressBar = getActivity().findViewById(R.id.progress_bar);
                    if(progress ==100){
                        progressBar.setVisibility(View.INVISIBLE);
                        ImageView status_github = getActivity().findViewById(R.id.status_github);
                        status_github.setImageResource(R.drawable.ic_github);
                        status_github.setVisibility(View.VISIBLE);
                        do_card.setClickable(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader!=null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public String x86_64;
    public void getLatest_x86_64(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://api.github.com/repos/opengapps/arm/releases/latest");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) !=null){
                        response.append(line);
                    }
                    x86_64 = response.toString();
                    JSONObject json = new JSONObject(x86_64);
                    String version = json.getString("tag_name");
                    save_data.putString("date",version).apply();
                    progress = 100;
                    TextView status_latest = getActivity().findViewById(R.id.status_latest);
                    status_latest.setText(getString(R.string.latest_release)+" x86_64: "+version);
                    ProgressBar progressBar = getActivity().findViewById(R.id.progress_bar);
                    if(progress ==100){
                        progressBar.setVisibility(View.INVISIBLE);
                        ImageView status_github = getActivity().findViewById(R.id.status_github);
                        status_github.setImageResource(R.drawable.ic_github);
                        status_github.setVisibility(View.VISIBLE);
                        do_card.setClickable(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader!=null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    void downloadGapps(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + filename;
    }
}