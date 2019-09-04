package com.xenonota.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xenonota.R;
import com.xenonota.configs.DeviceConfig;
import com.xenonota.configs.GappsConfig;
import com.xenonota.fragments.Fragment_Gapps;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckGappsTask extends AsyncTask<Void, Void, String> {

    private boolean cancel = false;
    private static CheckGappsTask mInstance = null;
    private Fragment_Gapps frag;

    public static CheckGappsTask getInstance(Fragment_Gapps frag) {
        if (mInstance == null) {
            mInstance = new CheckGappsTask();
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
                || frag.getActivity() == null) {
            cancel = true;
        } else {
            TextView status_latest = frag.getActivity().findViewById(R.id.status_latest);
            ProgressBar progressBar = frag.getActivity().findViewById(R.id.progress_bar);
            ImageView status_github = frag.getActivity().findViewById(R.id.status_github);
            CardView download_card = frag.getActivity().findViewById(R.id.state_latest);

            if (isConnectivityAvailable(frag.getContext())) {
                status_latest.setText(R.string.loading);
                download_card.setClickable(false);
                progressBar.setVisibility(View.VISIBLE);
                status_github.setVisibility(View.INVISIBLE);
            } else {
                cancel = true;
                status_latest.setText(frag.getString(R.string.latest_release_msg, DeviceConfig.getCPU().toUpperCase(), "UNKNOWN"));
                download_card.setClickable(false);
                progressBar.setVisibility(View.INVISIBLE);
                status_github.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        if (cancel) return null;

        String version = null;
        String gappsUrl = null;
        HttpURLConnection connection1 = null;
        HttpURLConnection connection2 = null;
        BufferedReader reader1 = null;
        BufferedReader reader2 = null;
        try{
            URL listUrl = new URL("https://api.opengapps.org/list");
            connection1 = (HttpURLConnection)listUrl.openConnection();
            connection1.setRequestMethod("GET");
            connection1.setConnectTimeout(8000);
            connection1.setReadTimeout(8000);
            InputStream in1 = connection1.getInputStream();
            reader1 = new BufferedReader(new InputStreamReader(in1));
            StringBuilder response1 = new StringBuilder();
            String line;
            while((line = reader1.readLine()) !=null){
                response1.append(line);
            }
            JSONObject archData = (new JSONObject(response1.toString())).getJSONObject("archs").getJSONObject(DeviceConfig.getCPU());
            version = archData.getString("date");

            URL downloadUrl = new URL("https://api.opengapps.org/download?arch=" + DeviceConfig.getCPU() + "&api=" + DeviceConfig.getRelease() + "&variant=" + GappsConfig.getVariant(frag.getContext()) + "&date=" + version);
            connection2 = (HttpURLConnection)downloadUrl.openConnection();
            connection2.setRequestMethod("GET");
            connection2.setConnectTimeout(8000);
            connection2.setReadTimeout(8000);
            InputStream in = connection2.getInputStream();
            reader2 = new BufferedReader(new InputStreamReader(in));
            StringBuilder response2 = new StringBuilder();
            while((line = reader2.readLine()) !=null){
                response2.append(line);
            }
            gappsUrl = (new JSONObject(response2.toString())).getString("zip");
            Log.e("GAPPS", "Version:" + version + " URL:" + gappsUrl);
            GappsConfig.setLatestVersion(version, frag.getContext());
            GappsConfig.setURL(gappsUrl, frag.getContext());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(reader1!=null){
                try{
                    reader1.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(reader2!=null){
                try{
                    reader2.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(connection1!=null){
                connection1.disconnect();
            }
            if(connection2!=null){
                connection2.disconnect();
            }
        }

        return version;
    }

    @Override
    protected void onPostExecute(String version) {
        super.onPostExecute(version);

        if (cancel || frag.getActivity() == null) return;
        TextView status_latest = frag.getActivity().findViewById(R.id.status_latest);
        ProgressBar progressBar = frag.getActivity().findViewById(R.id.progress_bar);
        ImageView status_github = frag.getActivity().findViewById(R.id.status_github);
        CardView download_card = frag.getActivity().findViewById(R.id.state_latest);

        if (version == null) {
            status_latest.setText(frag.getString(R.string.latest_release_msg, DeviceConfig.getCPU().toUpperCase(), "UNKNOWN"));
            download_card.setClickable(false);
        } else {
            status_latest.setText(frag.getString(R.string.latest_release_msg, DeviceConfig.getCPU().toUpperCase(), version));
            download_card.setClickable(true);
        }
        progressBar.setVisibility(View.INVISIBLE);
        status_github.setImageResource(R.drawable.ic_ota_available);
        status_github.setVisibility(View.VISIBLE);

        mInstance = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

}
