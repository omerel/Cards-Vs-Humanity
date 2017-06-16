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




}
