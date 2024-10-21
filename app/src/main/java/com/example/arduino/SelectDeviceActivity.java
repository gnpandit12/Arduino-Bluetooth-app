package com.example.arduino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arduino.model.DeviceInfoModel;
import com.example.arduino.model.DeviceListAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        // Bluetooth Setup
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            pairedDevices = bluetoothAdapter.getBondedDevices();
            List<Object> deviceList = new ArrayList<>();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                    deviceList.add(deviceInfoModel);
                }
                // Display paired device using recyclerView
                RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this,deviceList);
                recyclerView.setAdapter(deviceListAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            } else {
                View view = findViewById(R.id.recyclerViewDevice);
                Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                });
                snackbar.show();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 12);
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 12:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pairedDevices = bluetoothAdapter.getBondedDevices();
                    List<Object> deviceList = new ArrayList<>();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                            deviceList.add(deviceInfoModel);
                        }
                        // Display paired device using recyclerView
                        RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this,deviceList);
                        recyclerView.setAdapter(deviceListAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        View view = findViewById(R.id.recyclerViewDevice);
                        Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) { }
                        });
                        snackbar.show();
                    }
                }
        }
    }

}

