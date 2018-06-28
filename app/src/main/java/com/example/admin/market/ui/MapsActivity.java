package com.example.admin.market.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.example.admin.market.R;
import com.example.admin.market.ui.map.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity {

    private static final String ARG_QUERY = "com.example.admin.market.ui.MapsActivity#QUERY";
    private static final String ARG_LATLNG = "com.example.admin.market.ui.MapsActivity#LATLNG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        final LatLng latLng = getIntent().getParcelableExtra(ARG_LATLNG);
        if (latLng != null) {
            mapFragment.show(latLng);
        } else {
            mapFragment.search(getIntent().getStringExtra(ARG_QUERY));
        }
    }


    public static void start(Context context,String query) {
        context.startActivity(
            new Intent(context, MapsActivity.class)
                .putExtra(ARG_QUERY, query)
        );
    }

    public static void start(Context context, LatLng latLng) {
        context.startActivity(
            new Intent(context, MapsActivity.class)
                .putExtra(ARG_LATLNG, latLng)
        );
    }
}
