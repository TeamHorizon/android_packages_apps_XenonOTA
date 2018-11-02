package com.xenonota.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
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
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try{
            URL url = new URL("https://api.github.com/repos/opengapps/" + DeviceConfig.getCPU() + "/releases/latest");
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
            String responseData = response.toString();
            JSONObject json = new JSONObject(responseData);
            version = json.getString("tag_name");
            GappsConfig.setLatestVersion(version, frag.getContext());
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
        status_github.setImageResource(R.drawable.ic_github);
        status_github.setVisibility(View.VISIBLE);

        mInstance = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

}
