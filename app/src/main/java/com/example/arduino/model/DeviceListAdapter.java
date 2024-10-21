package com.example.arduino.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arduino.MainActivity;
import com.example.arduino.R;

import java.util.List;

/**
 * @Author Gaurav Naresh Pandit
 * @Since 18/10/24
 **/
public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Object> deviceList;

    public DeviceListAdapter(Context context, List<Object> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        itemHolder.textName.setText(deviceInfoModel.getDeviceName());
        itemHolder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());

        // When a device is selected
        itemHolder.deviceInfoCardView.setOnClickListener(view -> {
            Intent intent = new Intent(context, MainActivity.class);
            // Send device details to the MainActivity
            intent.putExtra("deviceName", deviceInfoModel.getDeviceName());
            intent.putExtra("deviceAddress",deviceInfoModel.getDeviceHardwareAddress());
            // Call MainActivity
            context.startActivity(intent);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        CardView deviceInfoCardView;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            deviceInfoCardView = v.findViewById(R.id.device_info_card_view);
        }
    }

    @Override
    public int getItemCount() {
        int dataCount = deviceList.size();
        return dataCount;
    }

}
