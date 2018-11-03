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

        String JSON_URL = "https://raw.githubusercontent.com/topjohnwu/MagiskManager/update/stable.json";

        try {
            JSONObject json = new JSONObject(readURL(JSON_URL));
            JSONObject magisk = json.getJSONObject("magisk");
            String url = magisk.getString("link");
            String version = magisk.getString("version");
            String filename = "Magisk-v" + version + ".zip";

            if (!"".equals(url)) return new MagiskConfig(url, filename, version);
        } catch (Exception ex) {OTAUtils.logError(ex);}
        return null;
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
