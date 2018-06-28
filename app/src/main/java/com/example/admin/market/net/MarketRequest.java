package com.example.admin.market.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.example.admin.market.entities.Market;
import org.json.JSONObject;


public final class MarketRequest extends BroadcastReceiver { //https://developer.android.com/reference/android/content/BroadcastReceiver.html
//expirimenting with callbacks
    //http://www.fandroid.info/urok-13-osnovy-java-metody-obratnogo-vyzova-callback/
    @FunctionalInterface
    public interface Callback { //called when response recieved
        void onResponse(Market market);
    }

    private final Market market;
    private final Callback callback;

    private MarketRequest(Market market, final Callback callback) {
        this.market = market;
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.unregisterReceiver(this);
        String text = "{" + intent.getStringExtra("output_data");
        try {
            JSONObject jo = new JSONObject(text);
            JSONObject marketdetails = new JSONObject(jo.getString("marketdetails"));
            String address = marketdetails.getString("Address");
            String google = marketdetails.getString("GoogleLink");
            String produce = marketdetails.getString("Products");
            String schedule = marketdetails.getString("Schedule");
            schedule = schedule.replaceAll("<br>", "");
            produce.replaceAll("<br>", "");
            market.setAddress(address);
            market.setSchedule(schedule);
            market.setGoogle(google);
            market.setProduce(produce);
            callback.onResponse(market);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start(Intent intent, Context context, String action, Market market, Callback callback) {
        context.registerReceiver(new MarketRequest(market, callback), new IntentFilter(action));
        context.startService(intent);
    }
}
