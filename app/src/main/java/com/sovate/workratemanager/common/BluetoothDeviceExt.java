package com.sovate.workratemanager.common;

import android.bluetooth.BluetoothDevice;

/**
 * Created by harksoo on 2016-03-18.
 */
public class BluetoothDeviceExt {
    BluetoothDevice bluetoothDevice;

    UploadStatus uploadStatus = UploadStatus.NOEN;

    // listview의 position 의 위치
    int position = -1;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
