package com.omerbarr.cardsvshumanity.Bluetooth;

import java.util.UUID;

public interface BluetoothConstants {

    // inner interface
    int SECOND = 1000;
    int MINUTE = 1000*60;

    // Unique UUID for this application
    UUID APP_UUID = UUID.fromString("ca87c0d0-afac-11de-8a39-0800250c9a66");

    // Delimiter in bluetoothConnected
    String DELIMITER = "<!-12341234@12341234-!>";

    int DEVICE_ADDED = 111;

    int DEVICE_FOUND = 112;

    int FAILED_CONNECTING_TO_DEVICE = 113;

    int SUCCEED_CONNECTING_TO_DEVICE = 114;

    int READ_PACKET = 115;

    //int FAILED_DURING_RECEIVE_PACKET = 116;

    //int FAILED_DURING_WRITE_PACKET = 117;

    int DEVICE_DISCONNECTED = 118;

    int CMD_END_GAME = 119;




}
