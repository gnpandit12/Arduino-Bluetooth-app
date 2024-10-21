package com.example.arduino;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.arduino.model.ConnectedThread;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    public final static int CONNECTING_STATUS = 1;
    public final static int MESSAGE_READ = 2;
    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setEnabled(false);

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, this);
            createConnectThread.start();


        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonToggle.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        switch (arduinoMsg.toLowerCase()){
                            case "led is turned on":
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "led is turned off":
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                        }
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(view -> {
            // Move to adapter list
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
        });

        // Button to ON/OFF LED on Arduino Board
        buttonToggle.setOnClickListener(view -> {
            String cmdText = null;
            String btnState = buttonToggle.getText().toString().toLowerCase();
            switch (btnState){
                case "turn on":
                    buttonToggle.setText("Turn Off");
                    // Command to turn on LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn on>";
                    break;
                case "turn off":
                    buttonToggle.setText("Turn On");
                    // Command to turn off LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn off>";
                    break;
            }
            // Send command to Arduino board
            connectedThread.write(cmdText);
        });

        /* ============================ Terminate Connection at BackPress ====================== */
        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (createConnectThread != null){
                    createConnectThread.cancel();
                }
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
        });

    }

    public class CreateConnectThread extends Thread {

        public static final String TAG = "CreateConnectThread";
        private Context mContext;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Context context) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final
             */

            this.mContext = context;

            if (ContextCompat.checkSelfPermission(
                    mContext, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED) {
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                BluetoothSocket tmp = null;
                try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                    UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
                    tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

                } catch (IOException e) {
                    Log.d(TAG, "Socket's create() method failed", e);
                }
                mmSocket = tmp;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
            }
        }

        @Override
        public void run() {

            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            if (ContextCompat.checkSelfPermission(
                    mContext, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
            }

            if (ContextCompat.checkSelfPermission(
                    mContext, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED) {
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                    Log.d("MainActivity", "Device connected");
                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                        Log.d("MainActivity", "Cannot connect to device");
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    } catch (IOException closeException) {
                        Log.d("MainActivity", "Could not close the client socket", closeException);
                    }
                    return;
                }

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.start();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                }
            }

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("MainActivity", "Could not close the client socket", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }
            case 2:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }
            case 3:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}