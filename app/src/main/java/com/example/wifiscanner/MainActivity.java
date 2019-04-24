package com.example.wifiscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements Serializable {

    private WifiManager wifiManager; // Creates a new WifiManager object called wifiManager
    private ListView listView; // Create a new ListView object called listView
    private Button buttonScan; // Creates a new Button object called buttonScan
    private Button buttonPlot; // Creates a new Button object called buttonPlot
    private Button buttonWar;
    private int size = 0;
    private List<ScanResult> results; // Creates a new List object(ScanResult type) called results
    private ArrayList<String> arrayList = new ArrayList<>(); // Creates an ArrayList object(String type) called arrayList
    private ArrayList<String> resultsList = new ArrayList<>(); // Creates an ArrayList object(String type) called resultsList
    private ArrayAdapter adapter; // Creates a new ArrayAdapter object called ArrayAdapter

    private double latitude, longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Does some magic I don't really understand
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonScan = findViewById(R.id.scanBtn); // Sets buttonScan to be bound to the UI scanBtn element specified on the XML file
        // Initializes the button to "listen" for the user to tap on the button
        // If clicked, the scanWifi() method is executed
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        // Initializes the button to "listen" for the user to tap on the button
        // Switches to the MapsActivity class
        buttonPlot = findViewById(R.id.plotBtn);    // Sets buttonPlot to be bound to the UI plotBtn element specified on the XML file
        buttonPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapsActivity();
            }
        });

        buttonWar = findViewById(R.id.warBtn);
        buttonWar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               launchWarActivity();
           }
        });

        listView = findViewById(R.id.wifiList); // Sets listView to be bound to the UI wifiList element specified on the XML file

        // Not sure what this does, I think it sets an object that can get all the wifi-related services from the android phone
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // If Wifi is not turned on, the wifiManager object should detect that (boolean) and turn on the wifi automatically
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true); // turn on wifi automatically
        }

        // Treat the rest of this like an else statement
        // Not sure what this does, but I think it's meant to convert data from wifi to a readable listView
        // simple_list_item_1 is android's own XML layout to make the list of found wifi names (not in any of the XML file you made)
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);  // Try this link to learn more "https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView"
        listView.setAdapter(adapter); // listView pulls content from source using the adapter
        scanWifi();
    }

    /**
     * Scans for any detected wifi hotspots in range
     */
    private void scanWifi() {
        arrayList.clear();

        GPSTracker gps = new GPSTracker(this);
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); // What the hell is this
        wifiManager.startScan(); // Triggers a scan request
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show(); // Toast is that little notification pop up that disappears after a short time
    }

    /**
     * Opens the maps activity, in this case, google maps
     */
    private void launchMapsActivity() {
        // Creates a new Intent object called intent
        // Intent is just simple message object that is used to communicate between android components such as activities, content providers, data, broadcast receivers and services.
        // Learn more about intent here, https://acadgild.com/blog/intent-in-android-introduction
        Intent intent = new Intent(this, MapsActivity.class);
        //Bundle args = new Bundle();
        //args.putSerializable("ARRAYLIST", (Serializable)resultsList);
        //intent.putExtra("BUNDLE", args);
        startActivity(intent); // The method startActivity() is from the imported Activity class (android.support.v7.app.AppCompatActivity, more specifically)
    }

    private void launchWarActivity() {
        Intent intent = new Intent(this, WarDriveActivity.class);
        startActivity(intent);
    }

    // Creates new BroadcastReceiver object called wifiReceiver
    // Not sure what it does but I think it broadcasts messages (also known as intents) to other applications
    // Triggers once it receives a broadcast message, hence the name "Broadcast Receiver"
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults(); // Gets the latest found wifi hotspot put into object list results

            unregisterReceiver(this);

            // Populates the arrayList with its scan results
            for (ScanResult scanResult : results) {
                // Displays SSID (the name of the wifi hotspot) and capabilities (Security information)
                arrayList.add(scanResult.SSID + "-" + latitude + longitude);
                //resultsList.add(scanResult.SSID + "<" + latitude + longitude);
                adapter.notifyDataSetChanged(); // adapter needs to know that you changes the List in the activity
            }
        }
    };
}