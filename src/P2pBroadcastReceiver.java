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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

//********************************************************************
// P2pBroadcastReceiver
//
//
// This class is responsible for manipulating Broadcast Signals
//
// Variables
// ---------------
// WifiP2pManager           p2pManager      // to handle wifi direct framework
// WifiP2pManager.Channel   p2pChannel      // channel for connection
// MainActivity             p2pActivity     // to refer caller activity
//
//*******************************************************************

public class P2pBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager p2pManager;  // to handle wifi direct framework
    private WifiP2pManager.Channel p2pChannel; // channel for connecti
    private MainActivity p2pActivity; // to refer caller activity

    //this constructor initializes variables
    public P2pBroadcastReceiver(WifiP2pManager p2pManager, WifiP2pManager.Channel p2pChannel,MainActivity p2pActiviey)
    {
        this.p2pManager = p2pManager;
        this.p2pChannel = p2pChannel;
        this.p2pActivity = p2pActiviey;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // condition to check if wifi status changed
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            int state= intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context, "Wifi is on",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "Wifi is off",Toast.LENGTH_SHORT).show();
            }
        }
        // condition to check if peer change action- like if new peer found or someone gone out of connection
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(p2pManager != null)
            {
                p2pManager.requestPeers(p2pChannel,p2pActivity.peerListListener);
            }
        }
        // condition to check if peer change connection
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            if(p2pManager == null)
            {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected())
            {
                p2pManager.requestConnectionInfo(p2pChannel,p2pActivity.connectionInfoListener);
            }
            else{
                p2pActivity.connectionStatus.setText("Connection Disconnected");
            }

        }


    }
}
