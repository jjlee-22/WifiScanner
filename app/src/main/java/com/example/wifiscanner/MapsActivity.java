package com.example.wifiscanner;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    /*
    group(0) - All
    group(1) - latitude
    group(2) - longitude
    group(3) - SSID
    group(4) - capabilities
     */
    private String pattern = "(.*)(-.*)<(.*)<(.*)";
    private String tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        HashMap<String, String> resultsList = (HashMap<String, String>) args.getSerializable("ARRAYLIST");

        // Creating pattern object
        Pattern r = Pattern.compile(pattern);

        for (Map.Entry<String, String> pair : resultsList.entrySet()) {
            // Creating matcher object
            Matcher m = r.matcher(pair.getValue());

            if (m.find()) {
                tmp = m.group(1);
                tmp = m.group(2);
                tmp = m.group(3);
                tmp = m.group(4);
                LatLng coordinates = new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
                mMap.addMarker(new MarkerOptions().position(coordinates).title(m.group(3)).draggable(true));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16.0f));
            }
        }

    }
}
