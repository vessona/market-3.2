package com.example.admin.market.ui.map;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class MapFragment extends Fragment implements MapApi, OnMapReadyCallback {

    private static final float ZOOM_MAX = 15F;
    private static final float ZOOM_MIN = 3F;
    private Geocoder geocoder;
    private MapView mapView;
    private GoogleMap googleMap;
    private final List<Marker> markers = new LinkedList<>();
    private String pendingQuery;
    private LatLng pendingLatLng;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mapView = new MapView(getActivity());
        mapView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); //A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback). This class automatically initializes the maps system and the view. (Google docs)
        return mapView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.geocoder = new Geocoder(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void search(final String query) {
        if (googleMap != null) {
            try {
                final List<Address> locations = geocoder.getFromLocationName(query, 1);
                if (!locations.isEmpty()) {
                    final Address address = locations.get(0);
                    final LatLng pos = new LatLng(address.getLatitude(), address.getLongitude());
                    for (final Marker marker : markers) {
                        marker.remove();
                    }
                    markers.clear();
                    markers.add(
                            googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(pos)
                                            .title(query)
                            )
                    );
                    final int searchTokenCount = query.length() - query
                            .replace(",", "").length(); //depends on what we are looking - state, city, zip - camera will move the zoom

                    final float zoom = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, ZOOM_MAX - (ZOOM_MAX - ZOOM_MIN) * (3 - searchTokenCount) / 3F));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
                }
            } catch (IOException e) {
                Log.e("MapFragment", e.toString());
                Toast.makeText(getActivity(), "Search failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            pendingQuery = query; //waitingg until map is ready
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (pendingLatLng != null) {
            show(pendingLatLng);
            pendingLatLng = null;
        } else if (pendingQuery != null) {
            search(pendingQuery);
            pendingQuery = null;
        }
    }

    public void show(final LatLng latLng) {
        if (googleMap == null) {
            pendingLatLng = latLng;
            return;
        }
        markers.clear();
        markers.add(
                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                )
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }
}

