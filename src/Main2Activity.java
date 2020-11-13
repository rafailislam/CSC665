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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.myapplication.MainActivity.p2pReceiver;

//********************************************************************
//
// Activity 2 - Main2Activity
//
// This activity is responsible for creating messaging page, add send-receive message
//
// Variables
// ---------------
// EditText         editText           // to get text form user
// Button           button             // send button
// TextView         displayMessage     // to display message
// Server           objServer          // server class object to create server thread
// Client           objClient          // client class object to create client thread
// ChatExchange     objChatExchange    // to exchange message

// String           serverOrcllient    // get info from MainActivity whether server or client
// InetAddress      hostaddress        // to get host address
// MainActivity      mainActivity      // to access previous activity
//*******************************************************************

public class Main2Activity extends Activity {

    EditText editText; // to get text form user
    Button button; // send button
    TextView displayMessage; // to display message
    TextView displayMessage1;
    Server objServer;  // server class object to create server thread
    Client objClient;  // client class object to create client thread
    ChatExchange objChatExchange; // to exchange message

    String serverOrcllient; // get info from MainActivity whether server or client
    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // get info. from caller activity
        Intent intent = getIntent();
        serverOrcllient =  intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // check whether message is server or client to call specific thread
        if(serverOrcllient.equals("server"))
        {
            getServerInfo();
        }
        else if(serverOrcllient.equals("client"))
        {
            getClientInfo(MainActivity.ownerAddress);
        }

        // this portion of code has taken from Stack Overflow to avoid a crashing error
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setupConnection();
        action();
    }

    // this handler is responsible for manipulation incoming message
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg) {
            Log.i("Inside  ","handler ");
            switch (msg.what)
            {
                case 1: // what will be 1 if it reads message
                    byte[] messageBuffer = (byte[]) msg.obj;
                    String message1 = new String(messageBuffer,0,msg.arg1);

                    displayMessage.setText(message1);
                    break;
            }
            return true;
        };
    });

    // this method initialize variables
    private void setupConnection() {
        displayMessage = (TextView) findViewById(R.id.displayMsg);
        displayMessage1 = (TextView) findViewById(R.id.displayMsg1);
        button = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);

    }
    // if nay action performed
    private void action(){

        //if we click send button it will perform this OnClickListener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                displayMessage1.setText(message);
                editText.getText().clear();
                editText.clearFocus();
                objChatExchange.sendMessage(message.getBytes());

            }
        });

    }

    // method for creating server thread
    void getServerInfo(){
        objServer = new Server();
        objServer.start(); // starting host thread
        Log.i("your are host", "Host");

    }
    // method for creating client thread
    void getClientInfo(InetAddress serverAddress){
        objClient = new Client(serverAddress);
        objClient.start(); // starting client thread
        Log.i("your are client", "Client");
    }

    // this inner class is for Server side
    public class Server extends Thread{
        Socket sock;
        ServerSocket serverSocket;

        @Override
        public void run(){
            try {
                serverSocket = new ServerSocket(8888);
                sock = serverSocket.accept(); // accept request from any client
                if(sock != null)
                {
                    Log.i(" con",sock.getLocalSocketAddress()+" is connected with"+sock.getRemoteSocketAddress());
                }

                objChatExchange  = new ChatExchange(sock); // call ChatExchange() to exchange message
                objChatExchange.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // this inner class is for Server side
    public class Client extends Thread{
        Socket sock;
        String serverAddr;
        // this constructor initializes host address to connect with
        public Client(InetAddress serverAddress) {
            serverAddr = serverAddress.getHostAddress();
            sock = new Socket();
        }
        @Override
        public void run(){
            try {
                sock.connect(new InetSocketAddress(serverAddr, 8888),4400);// requests server for connection
                if (sock!=null)
                {
                    Log.i(" con",sock.getLocalSocketAddress()+" is connected with"+sock.getRemoteSocketAddress());
                }

                objChatExchange = new ChatExchange(sock);// call ChatExchange() to exchange message
                objChatExchange.start(); // thread start
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    // this inner class is for exchanging message
    class ChatExchange extends Thread{
        private InputStream inputMessage;
        private OutputStream outputMessage;
        private Socket sock;

        public ChatExchange(Socket socket){
            sock = socket;
            try {
                inputMessage = sock.getInputStream();
                outputMessage = sock.getOutputStream();

                Log.i("ex","in out is good ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            super.run();

            byte [] buffer = new byte[512];
            int countBytes;
            while(sock != null)
            {
                try {
                    countBytes = inputMessage.read(buffer); //read() returns the number of bytes in Buffer; returns -1 if no bytes is read
                    //Log.i("before hadler ","before handler");
                    if(countBytes > 0) {
                        handler.obtainMessage(1,countBytes,-1,buffer).sendToTarget();
                        Log.i("handler ","started");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        // this method will write message to send to the peer
        public void sendMessage(byte[] msg) {
            try {

                outputMessage.write(msg);
                Log.i("send message func","started");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            unregisterReceiver(p2pReceiver);
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(p2pReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(p2pReceiver);
    }
}


