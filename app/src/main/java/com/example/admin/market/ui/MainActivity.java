package com.example.admin.market.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.example.admin.market.net.DownloadService;
import com.example.admin.market.entities.MyStates;
import com.example.admin.market.R;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements AddressFilterFragment.OnFragmentInteractionListener {
    private TheResponse response;
    public String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        response = new TheResponse(this);
    }


    @Override
    public void onFragmentInteraction(String value, int flag, int position) {

        if (flag == AddressFilterFragment.STATE_FLAG) {
            state = MyStates.getStates().get(value);
            String url = "http://gomashup.com/json.php?fds=geo/usa/zipcode/state/" + state;
            Intent i = new Intent(MainActivity.this, DownloadService.class);
            i.putExtra("data", url);
            i.putExtra("type", "state");
            startService(i);
        } else if (flag == AddressFilterFragment.CITY_FLAG) {
            String url = "http://gomashup.com/json.php?fds=geo/usa/zipcode/city/" + value;
            Intent i = new Intent(MainActivity.this, DownloadService.class);
            i.putExtra("data", url);
            i.putExtra("type", "city");
            startService(i);
        }
    }

    public class TheResponse extends BroadcastReceiver {

        Context c;

        public TheResponse(Context c) {
            this.c = c;
        }

        public static final String STATUS_DONE_CITY = "CITY_ALL_DONE";

        public static final String STATUS_DONE_ZIP = "ZIP_ALL_DONE";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("receive", "inside");

            if (intent.getAction().equals(STATUS_DONE_CITY)) {

                String text = intent.getStringExtra("output_data");
                Log.d("STATE", text);
                try {
                    ArrayList<String> cityName = new ArrayList<>();
                    JSONObject jo = new JSONObject(text);
                    JSONArray ja = new JSONArray(jo.getString("result"));
                    for (int i = 0; i < ja.length(); i++) {
                        if (!cityName.contains(ja.getJSONObject(i).getString("City")))
                            cityName.add(ja.getJSONObject(i).getString("City"));
                    }
                    Collections.sort(cityName);
                    AddressFilterFragment af = (AddressFilterFragment) getSupportFragmentManager().findFragmentById(R.id.addressFr);
                    af.updateCity(cityName);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (intent.getAction().equals(STATUS_DONE_ZIP)) {
                String text = intent.getStringExtra("output_data");
                try {
                    ArrayList<String> zip = new ArrayList<String>();
                    JSONObject jo = new JSONObject(text);
                    JSONArray ja = new JSONArray(jo.getString("result"));
                    for (int i = 0; i < ja.length(); i++) {
                        String stateJSON = ja.getJSONObject(i).getString("State");
                        if (!zip.contains(ja.getJSONObject(i).getString("City")) && stateJSON.equals(state.toUpperCase()))
                            zip.add(ja.getJSONObject(i).getString("Zipcode"));

                    }
                    AddressFilterFragment af = (AddressFilterFragment) getSupportFragmentManager().findFragmentById(R.id.addressFr);
                    af.updateZip(zip);
                } catch (Exception e) {

                }
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(response);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(TheResponse.STATUS_DONE_CITY);
        IntentFilter filter2 = new IntentFilter(TheResponse.STATUS_DONE_ZIP);
        registerReceiver(response, filter);
        registerReceiver(response, filter2);
    }

    public static void start(final Context ctx) {
        ctx.startActivity(new Intent(ctx, MainActivity.class));
    }

}
