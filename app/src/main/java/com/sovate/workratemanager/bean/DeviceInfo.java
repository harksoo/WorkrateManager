package com.sovate.workratemanager.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Created by harks on 2016-03-01.
 */
public class DeviceInfo {

    BluetoothDevice device;
    ActivityDeviceStudentInfo deviceStudentInfo;
    ActivityWorkRate workRate = new ActivityWorkRate();
    String name;


    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public ActivityDeviceStudentInfo getDeviceStudentInfo() {
        return deviceStudentInfo;
    }

    public void setDeviceStudentInfo(ActivityDeviceStudentInfo deviceStudentInfo) {
        this.deviceStudentInfo = deviceStudentInfo;
    }

    public ActivityWorkRate getWorkRate() {
        return workRate;
    }

    public void setWorkRate(ActivityWorkRate workRate) {
        this.workRate = workRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}
