package com.example.arduino.model;

/**
 * @Author Gaurav Naresh Pandit
 * @Since 18/10/24
 **/
public class DeviceInfoModel {

    private String deviceName, deviceHardwareAddress;

    public DeviceInfoModel(){}

    public DeviceInfoModel(String deviceName, String deviceHardwareAddress){
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName(){return deviceName;}

    public String getDeviceHardwareAddress(){return deviceHardwareAddress;}

}
