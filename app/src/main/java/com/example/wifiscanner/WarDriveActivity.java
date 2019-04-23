package com.example.wifiscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class WarDriveActivity extends AppCompatActivity implements Serializable {

    private WifiManager wifiManager;
    private ArrayList<String> resultsList = new ArrayList<>();
    private List<ScanResult> results;
    private TextView textView;
    private Button buttonPlot;
    private GPSTracker gps;
    private int tmp;

    private volatile boolean scan = true;

    private Bundle args = new Bundle();

    Thread thread1;

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_war_drive);
        textView = findViewById(R.id.counterText);

        buttonPlot = findViewById(R.id.plotBtn);    // Sets buttonPlot to be bound to the UI plotBtn element specified on the XML file
        buttonPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan = false;
                launchMapsActivity();
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        collectWifi();
    }



    private void collectWifi() {
        gps = new GPSTracker(this);

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {

                while(scan) {
                    try {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                textView.setText(Integer.toString(resultsList.size()));
                            }
                        });

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); // What the hell is this
                        wifiManager.startScan(); // Triggers a scan request
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        thread1.start();

    }

    private void launchMapsActivity() {
        // Creates a new Intent object called intent
        // Intent is just simple message object that is used to communicate between android components such as activities, content providers, data, broadcast receivers and services.
        // Learn more about intent here, https://acadgild.com/blog/intent-in-android-introduction
        Intent intent = new Intent(this, MapsActivity.class);
        args.putSerializable("ARRAYLIST", resultsList);
        intent.putExtra("BUNDLE", args);
        startActivity(intent); // The method startActivity() is from the imported Activity class (android.support.v7.app.AppCompatActivity, more specifically)
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults(); // Gets the latest found wifi hotspot put into object list results

            unregisterReceiver(this);

            // Populates the arrayList with its scan results
            for (ScanResult scanResult : results) {
                // Displays SSID (the name of the wifi hotspot) and capabilities (Security information)
                resultsList.add(scanResult.SSID + "<" + latitude + longitude);
            }
        }
    };
}
