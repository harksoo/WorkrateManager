package com.sovate.workratemanager.bean;

import android.bluetooth.BluetoothDevice;

import com.sovate.workratemanager.common.UploadStatus;

import java.util.Comparator;

/**
 * Created by harks on 2016-03-01.
 */

public class DeviceInfo {

    BluetoothDevice device;
    ActivityDeviceStudentInfo deviceStudentInfo;
    ActivityWorkRate workRate = new ActivityWorkRate();
    String name = "";
    UploadStatus uploadStatus = UploadStatus.NOEN;


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

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public static class UserNameAscCompare implements Comparator<DeviceInfo> {

        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(DeviceInfo arg0, DeviceInfo arg1) {
            // TODO Auto-generated method stub
            return arg0.getDeviceStudentInfo().getUserName().compareTo(arg1.getDeviceStudentInfo().getUserName());
        }

    }

    public static class UserNameDscCompare implements Comparator<DeviceInfo> {

        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(DeviceInfo arg0, DeviceInfo arg1) {
            // TODO Auto-generated method stub
            return arg1.getDeviceStudentInfo().getUserName().compareTo(arg0.getDeviceStudentInfo().getUserName());
        }

    }

    public static class DeviceNameAscCompare implements Comparator<DeviceInfo> {

        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(DeviceInfo arg0, DeviceInfo arg1) {
            // TODO Auto-generated method stub
            if(arg0.getDeviceStudentInfo().getName().length() == 0){
                return 1;
            }
            else if(arg1.getDeviceStudentInfo().getName().length() == 0){
                return -1;
            }
            return arg0.getDeviceStudentInfo().getName().compareTo(arg1.getDeviceStudentInfo().getName());
        }

    }

    public static class DeviceNameDscCompare implements Comparator<DeviceInfo> {

        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(DeviceInfo arg0, DeviceInfo arg1) {
            // TODO Auto-generated method stub
            return arg1.getDeviceStudentInfo().getName().compareTo(arg0.getDeviceStudentInfo().getName());
        }

    }

}


