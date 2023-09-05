package com.myapplication;

import android.bluetooth.BluetoothSocket;

public class BluetoothSocketManager {
    private static BluetoothSocket bluetoothSocket;

    public static BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public static void setBluetoothSocket(BluetoothSocket socket) {
        bluetoothSocket = socket;
    }
}
