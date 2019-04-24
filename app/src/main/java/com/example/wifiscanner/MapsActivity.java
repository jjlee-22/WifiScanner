package com.example.wifiscanner;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener;
import com.google.maps.android.clustering.Cluster;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    /*
    group(0) - All
    group(1) - latitude
    group(2) - longitude
    group(3) - SSID
    group(4) - capabilities
     */
    private String pattern = "(.*)(-.*)<(.*)<(.*)";
    private String tmp;
    private ClusterManager<MyItem> mClusterManager;

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
        mUiSettings = mMap.getUiSettings();

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);

        mClusterManager.setOnClusterClickListener(new OnClusterClickListener<MyItem>() {
                    @Override
                    public boolean onClusterClick(final Cluster<MyItem> cluster) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                cluster.getPosition(), (float) Math.floor(mMap
                                        .getCameraPosition().zoom + 3)), 800,
                                null);
                        return true;
                    }
                });

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        HashMap<String, String> resultsList = (HashMap<String, String>) args.getSerializable("ARRAYLIST");

        // Creating pattern object
        Pattern r = Pattern.compile(pattern);

        int offsetNum = 0;

        for (Map.Entry<String, String> pair : resultsList.entrySet()) {
            // Creating matcher object
            Matcher m = r.matcher(pair.getValue());


//            if (m.find()) {
//                LatLng coordinates = new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
//                mMap.addMarker(new MarkerOptions().position(coordinates).title(m.group(3)).draggable(true));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16.0f));
//            }

            if (m.find()) {
                LatLng coordinates = new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
                //mMap.addMarker(new MarkerOptions().position(coordinates).title(m.group(3)).draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16.0f));

                double lat;
                double lng;

                if (offsetNum > 10){
                    offsetNum = 0;
                }

                double offset = offsetNum / 70000d;
                lat = Double.parseDouble(m.group(1));
                lng = Double.parseDouble(m.group(2)) + offset;

                String title = m.group(3);
                String snippet = m.group(4);

                MyItem infoWindowItem = new MyItem(lat, lng, title, snippet);
                mClusterManager.addItem(infoWindowItem);
                offsetNum++;
            }
        }

    }

    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyItem offsetItem = new MyItem(lat, lng);
            mClusterManager.addItem(offsetItem);
        }
    }
}
