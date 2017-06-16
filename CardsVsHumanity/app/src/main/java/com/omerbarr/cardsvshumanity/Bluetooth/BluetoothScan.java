package com.omerbarr.cardsvshumanity.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;



/**
 * Created by omer on 03/01/2017.
 * Bluetooth scan allows user scan manually near device when the device doesn't support ble
 * advertisement
 */

public class BluetoothScan implements BluetoothConstants {

    private final String TAG = "RELAY_DEBUG: "+BluetoothScan.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;
    private boolean isDeviceFound;
    private String mGameCode;
    private Context mContext;
    private Messenger mMessenger;


    public BluetoothScan(Context context,String gameCode,Messenger messenger) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.isDeviceFound = false;
        this.mGameCode = gameCode;
        this.mContext = context;
        this.mMessenger = messenger;
    }

    public void startScan(){

        createBroadcastReceiver();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startDiscovery();
            Log.e(TAG, "start Discovery ");
        }
    }

    private void stopScan(){
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();
        Log.e(TAG, "finish Discovery ");
    }

    public void close(){
        stopScan();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Send message to service
     * @param msg message type
     */
    private void sendMessageToService(int msg)  {
        try {
            mMessenger.send(Message.obtain(null, msg));
        } catch (RemoteException e) {
            Log.e(TAG, "Problem with sendMessageToManager ");
        }
    }
    /**
     * BroadcastReceiver of bluetooth scan
     */
    private  void createBroadcastReceiver() {

            mFilter = new IntentFilter();
            // Add all all the actions to filter
            mFilter.addAction(BluetoothDevice.ACTION_FOUND);

            mBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    switch (action) {
                        // When discovery finds a device
                        case BluetoothDevice.ACTION_FOUND:
                            // Get the BluetoothDevice object from the Intent
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            Log.e(TAG, "found device: "+device.getName() );
                            String[] split = null;
                            if (device.getName() != null)
                                split = device.getName().split("_");
                            if (split != null && split.length > 0) {
                                if (split[0].equals(mGameCode)) {
                                    isDeviceFound = true;
                                    Log.e(TAG, "ACTION_FOUND " + device.getName());
                                    stopScan();
                                    sendMessageToService(DEVICE_FOUND);
                                }
                            }
                            break;
                    }
                }
            };
            mContext.registerReceiver(mBroadcastReceiver, mFilter);
    }
}
