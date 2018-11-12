package com.xenonota.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.xenonota.R;
import com.xenonota.configs.MagiskConfig;
import com.xenonota.dialogs.WaitDialogHandler;
import com.xenonota.fragments.Fragment_OTA;
import com.xenonota.utils.OTAUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MagiskDownloadTask extends AsyncTask<Void, Void, MagiskConfig> {

    private boolean cancel = false;

    private static MagiskDownloadTask mInstance = null;
    private final Handler mHandler = new WaitDialogHandler();

    private Fragment_OTA frag;

    public static MagiskDownloadTask getInstance(Fragment_OTA frag) {
        if (mInstance == null) {
            mInstance = new MagiskDownloadTask();
        }
        mInstance.frag = frag;
        return mInstance;
    }

    private static boolean isConnectivityAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        } else {
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected());
        }
    }

    @Override
    protected void onPreExecute() {
        if (frag == null
                || frag.getContext() == null
                || !isConnectivityAvailable(frag.getContext())) {
            cancel = true;
        } else {
            showWaitDialog(frag.getContext());
        }

    }

    @Override
    protected MagiskConfig doInBackground(Void... params) {
        if (cancel) return null;

        String JSON_URL_STABLE = "https://raw.githubusercontent.com/topjohnwu/magisk_files/master/stable.json";
        String JSON_URL_BETA = "https://raw.githubusercontent.com/topjohnwu/magisk_files/master/beta.json";

        try {
            JSONObject json_stable = new JSONObject(readURL(JSON_URL_STABLE));
            JSONObject json_beta = new JSONObject(readURL(JSON_URL_BETA));
            JSONObject magisk_stable = json_stable.getJSONObject("magisk");
            JSONObject magisk_beta = json_beta.getJSONObject("magisk");
            String url_stable = magisk_stable.getString("link");
            String url_beta = magisk_beta.getString("link");
            String version_stable = magisk_stable.getString("version");
            String version_beta = magisk_beta.getString("version");
            String filename_stable = "Magisk-v" + version_stable + ".zip";
            String filename_beta = "Magisk-v" + version_beta + ".zip";

            String magisk_variant = MagiskConfig.getVariant(frag.getContext());

            switch(magisk_variant) {
                case "latest":{
                    int result = compareVersion(version_beta, version_stable);
                    if (result == 0) {
                        if (!"".equals(url_stable)) return new MagiskConfig(url_stable, filename_stable, version_stable);
                    } else if (result < 0) {
                        if (!"".equals(url_stable)) return new MagiskConfig(url_stable, filename_stable, version_stable);
                    } else  {
                        if (!"".equals(url_beta)) return new MagiskConfig(url_beta, filename_beta, version_beta);
                    }
                    break;
                }
                case "stable":{
                    if (!"".equals(url_stable)) return new MagiskConfig(url_stable, filename_stable, version_stable);
                    break;
                }
                case "beta":{
                    if (!"".equals(url_beta)) return new MagiskConfig(url_beta, filename_beta, version_beta);
                    break;
                }
            }
        } catch (Exception ex) {OTAUtils.logError(ex);}
        return null;
    }

    public int compareVersion(String version1, String version2) {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        int i=0;
        while(i<arr1.length || i<arr2.length){
            if(i<arr1.length && i<arr2.length){
                if(Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])){
                    return -1;
                }else if(Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])){
                    return 1;
                }
            } else if(i<arr1.length){
                if(Integer.parseInt(arr1[i]) != 0){
                    return 1;
                }
            } else if(i<arr2.length){
                if(Integer.parseInt(arr2[i]) != 0){
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }

    private String readURL(String url){
        String re="";
        try{
            InputStream inputStream = OTAUtils.downloadURL(url);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            re=total.toString();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return re;
    }

    @Override
    protected void onPostExecute(MagiskConfig magiskConfig) {
        super.onPostExecute(magiskConfig);

        if (magiskConfig == null) {
            showToast();
        } else {
            frag.processMagiskResult(magiskConfig);
        }

        hideWaitDialog();

        mInstance = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

    private void showWaitDialog(Context context) {
        Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_SHOW_DIALOG);
        msg.obj = context;
        msg.arg1 = R.string.magisk_check;
        mHandler.sendMessage(msg);
    }

    private void hideWaitDialog() {
        Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_CLOSE_DIALOG);
        mHandler.sendMessage(msg);
    }

    private void showToast() {
        if (frag != null && frag.getContext() != null) {
            OTAUtils.toast(R.string.magisk_error, frag.getContext());
        }
    }
}
