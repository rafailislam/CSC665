//********************************************************************
//
// Rafail Islam
// Computer Networks
// Programming Project : Peer-to-Peer messaging app using android Wifip2pManager and java socket programming
// December 5, 2019
// Instructor: Dr. Ajay K. Katangur
//
//********************************************************************

package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
//********************************************************************
//
// Activity 1 - MainActivity
//
// This activity is responsible for creating first layout, wifi on/off, searching peers, display peers and connect with peers
//
// Variables
// ---------------
//    Button                    onOffButton, discoverButton     // for making starting p2p network
//    ListView                  listView                        // for showing available peers list
//    TextView                  connectionStatus                // for showing status
//    Switch                    onOffSwitch                     // for turing wifi on/off
//
//    WifiManager               wifiStatus                      // for knowing wifi status
//    WifiP2pManager             p2pManager                     // for establishing p2p network
//    WifiP2pManager.Channel    p2pChannel                      // channel used to make p2p connection
//
//    BroadcastReceiver         p2pReceiver                     // to know if BroadcastReceiver is available
//    IntentFilter              p2pIntextFilter                 // intention filter to match the action
//
//    List<WifiP2pDevice> peersList = new ArrayList<WifiP2pDevice>() // an array to store WifiP2pDevice list
//    String[]                  deviceName                                          // device name
//    WifiP2pDevice []          deviceArray                     // to get exact device
//
//*******************************************************************

public class MainActivity extends Activity {

    Button onOffButton, discoverButton; // for making starting p2p network
    ListView listView; // for showing available peers list
    TextView  connectionStatus; // for showing status
    Switch onOffSwitch;   // for turing wifi on/off

    WifiManager wifiStatus; // for knowing wifi status
    public static WifiP2pManager p2pManager; // for establishing p2p network
    WifiP2pManager.Channel p2pChannel;       // channel used to make p2p connection

    public static BroadcastReceiver p2pReceiver; // to know if BroadcastReceiver is available
    IntentFilter p2pIntextFilter;

    List<WifiP2pDevice> peersList = new ArrayList<WifiP2pDevice>(); // an array to store WifiP2pDevice list
    String[] deviceName; // device name
    WifiP2pDevice [] deviceArray; // to get exact device

    // class objects
    public final static String EXTRA_MESSAGE = "key";
    public  static InetAddress ownerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting runtime location permission for android version > 8
        // this portion of code has taken from Stack Overflow
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            final int REQUEST_LOCATION = 2;


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        } else {
            // permission has been granted, continue as usual
            Task<Location> locationResult = LocationServices.getFusedLocationProviderClient(this /** Context */).getLastLocation();
        }



        setupConnection();
        action();
    }

    private void action(){

        // acton for all button and list
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                {
                    wifiStatus.setWifiEnabled(false);
                    Toast.makeText(getApplicationContext(), "Turning OFF wifi",Toast.LENGTH_SHORT).show();


                }
                else{
                    wifiStatus.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(), "Turning ON wifi",Toast.LENGTH_SHORT).show();

                }
            }
        });
        discoverButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // if wifi is not ON state it call back to previous state and ask to turn on the wifi
                if(!wifiStatus.isWifiEnabled())
                {
                    Toast.makeText(getApplicationContext(),"Turn wifi on first",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(),"Searching peers...",Toast.LENGTH_SHORT).show();
                // call discoverPeers function to discover  peers
                p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Peers Found");
                        // if peers found then set the listView visible to show the list of peers
                        listView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int reason) {
                        // if no peers found then set the listView invisible to
                        listView.setVisibility(View.INVISIBLE);
                        connectionStatus.setText("No peers found");
                    }
                });
            }
        });

        //  action for selection peer to be connected with
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig configuration = new WifiP2pConfig();
                configuration.deviceAddress = device.deviceAddress;

                //connect with selected peer
                p2pManager.connect(p2pChannel, configuration, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Connection Established with "+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Failed to Connect with "+device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }
    // initialize all variables
    private void setupConnection(){
        connectionStatus = (TextView) findViewById(R.id.connectionStat);
        onOffButton = (Button) findViewById(R.id.onOff);
        discoverButton = (Button) findViewById(R.id.discover);
        listView = (ListView) findViewById(R.id.peerListView);
        onOffSwitch = (Switch) findViewById(R.id.onOff);

        // getting wifi status of device
        wifiStatus = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
       // accessing WIFI p2p service from android system
        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        // initialize wifi 2p2 channel for connection call back
        p2pChannel = p2pManager.initialize(getApplicationContext(), getMainLooper(),null);

        //creating object of P2pBroadcastReceiver class which enable us to know about different actions like WIFI_P2P_PEERS_CHANGED_ACTION
        p2pReceiver = new P2pBroadcastReceiver(p2pManager,p2pChannel,this);

        //initialize action into intentfilter for future use
        p2pIntextFilter = new IntentFilter();
        p2pIntextFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntextFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntextFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntextFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // initially check the wifi status and set switch button accordingly
        if(wifiStatus.isWifiEnabled()) {
            onOffSwitch.setChecked(true);
        }
        else{
            onOffSwitch.setChecked(false);

        }

    }
    // to list the peers and display in ListView
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            if(!peers.getDeviceList().equals(peersList)){
                // clear previous peer list
                peersList.clear();
                peersList.addAll(peers.getDeviceList());

                deviceName = new String[peers.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peers.getDeviceList().size()];
                int i=0;
                // getting name of peer devices
                for ( WifiP2pDevice device : peers.getDeviceList())
                {
                    deviceName[i] = device.deviceName;
                    deviceArray[i] = device;
                    i++;

                }
                // showing devices name on listView via ArrayAdapter
                ArrayAdapter<String> adapter= new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceName);
                listView.setAdapter(adapter);


            }
            if(peersList.size() == 0)
            {
                Toast.makeText(getApplicationContext(),"No P2P user found",Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Open up new activity for messaging
    final WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
             ownerAddress = info.groupOwnerAddress;
          // if this device accept connection request it will be p2p HOST
             if (info.groupFormed && info.isGroupOwner) {
                 connectionStatus.setText("You are playing host role in P2P chat");
                 Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                 intent.putExtra(EXTRA_MESSAGE,"server");
                 startActivity(intent);
             } else if (info.groupFormed) {
                 connectionStatus.setText("Your are connection with your p2p host");
                 Toast.makeText(getApplicationContext(),"Conversation start",Toast.LENGTH_SHORT).show();

                 Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                 intent.putExtra(EXTRA_MESSAGE,"client");
                 startActivity(intent);
             }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(p2pReceiver,p2pIntextFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(p2pReceiver);
    }





}
