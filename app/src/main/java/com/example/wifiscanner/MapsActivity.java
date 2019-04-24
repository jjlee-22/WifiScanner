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
import android.content.Context;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


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
    private HashMap<String, String> resultsList;
    private HashMap<String, String> accessHashMap = new HashMap<>();
    private HashMap<String, String> totalHashMap = new HashMap<>();

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

        String filename = "Info";
        resultsList = (HashMap<String, String>) args.getSerializable("ARRAYLIST");

        try {
            accessHashMap = (HashMap) readObjectFromFile(this, filename);
        } catch (NullPointerException e) {
            accessHashMap.putAll(resultsList);
        }

        totalHashMap.putAll(resultsList);

        try {
            totalHashMap.putAll(accessHashMap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        writeObjectToFile(this, totalHashMap, filename);



        // Creating pattern object
        Pattern r = Pattern.compile(pattern);

        int offsetNum = 0;

        for (Map.Entry<String, String> pair : totalHashMap.entrySet()) {
            // Creating matcher object
            Matcher m = r.matcher(pair.getValue());


//            if (m.find()) {
//                LatLng coordinates = new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
//                mMap.addMarker(new MarkerOptions().position(coordinates).title(m.group(3)).draggable(true));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16.0f));
//            }

            if (m.find()) {
                LatLng coordinates = new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
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
    public static void writeObjectToFile(Context context, Object object, String filename) {

        ObjectOutputStream objectOut = null;
        try {

            FileOutputStream fileOut = context.openFileOutput(filename, MapsActivity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    // do not
                }
            }
        }
    }

    public static Object readObjectFromFile(Context context, String filename) {

        ObjectInputStream objectIn = null;
        Object object = null;
        try {

            FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();

        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    // do nowt
                }
            }
        }

        return object;
    }


}
