package com.example.admin.market.net;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.example.admin.market.ui.AddressFilterFragment;
import com.example.admin.market.ui.MainActivity;
import com.example.admin.market.ui.MarketsActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadService extends IntentService {


    public DownloadService() {
        super(DownloadService.class.getName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String url_site = intent.getStringExtra("data");
        String type = intent.getStringExtra("type");
        String results = getRemoteData(url_site);

        Intent broadcast = new Intent();

        if (type.equals("state")) {
            broadcast.setAction(MainActivity.TheResponse.STATUS_DONE_CITY);
            broadcast.putExtra("output_data", results.substring(1, results.length() - 1));
            sendBroadcast(broadcast);
        } else if (type.equals("city")) {
            broadcast.setAction(MainActivity.TheResponse.STATUS_DONE_ZIP);

            broadcast.putExtra("output_data", results.substring(1, results.length() - 1));
            sendBroadcast(broadcast);
        } else if (type.equals("zip")) {
            broadcast.setAction(AddressFilterFragment.TheResponse.STATUS_DONE_MARKETS);
            Log.d("SENDING MARKETS", "HI");
            broadcast.putExtra("output_data", results.substring(1, results.length() - 1));
            sendBroadcast(broadcast);
        } else if (type.equals("market")) {
            Log.d("sending market details", results.substring(1, results.length() - 1));
            broadcast.setAction(MarketsActivity.TitlesFragment.STATUS_DONE_DETAILS);
            broadcast.putExtra("output_data", results.substring(1, results.length() - 1));
            sendBroadcast(broadcast);
        }
    }

    private static String trim(String origin) {
        final JSONObject json;
        try {
            json = new JSONObject(origin.substring(1, origin.length() - 1));
        } catch (JSONException e) {
            return origin;
        }
        final JSONArray array;
        try {
            array = json.getJSONArray("result");
        } catch (JSONException e) {
            return origin;
        }
        for (int i = 0; i < array.length(); i++) {
            if (i > 800) {
                array.remove(i);
            }
        }
        json.remove("result");
        try {
            json.put("result", array);
        } catch (JSONException e) {
            return origin;
        }
        return "(" + json.toString() + ")";
    }


    private String getRemoteData(String site) {

        HttpURLConnection c = null;
        try {
            URL u = new URL(site);
            c = (HttpURLConnection) u.openConnection();
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return trim(sb.toString());
            }

        } catch (Exception ex) {
            Log.d("Error", ex.toString());
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {

                }
            }
        }
        return null;

    }
}
