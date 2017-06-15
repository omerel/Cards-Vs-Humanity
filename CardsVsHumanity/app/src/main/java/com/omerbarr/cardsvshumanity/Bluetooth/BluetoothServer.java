package com.omerbarr.cardsvshumanity.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListAdapter;

import com.omerbarr.cardsvshumanity.Utils.PlayerListArrayAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by omer on 10/12/2016.
 * BluetoothServer , a thread which waits for other device to ask for connection. when it happen, BluetoothServer
 * creates BluetoothSocket.
 */

public class BluetoothServer extends Thread  implements BlutoothConstants{

    private final String TAG = "DEBUG: "+BluetoothServer.class.getSimpleName();


    private final BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private Messenger mMessenger;
    private BluetoothDevice mConnectedDevice;
    private Context mContext;
    private PlayerListArrayAdapter mListArrayAdapter;
    private ArrayList<BluetoothSocket> mSocketArrayList;

    /**
     * BluetoothServer constructor
     * @param messenger to bluetooth manager
     * @param socketArrayList
     * @param listArrayAdapter
     */
    public BluetoothServer(Context context, Messenger messenger,
                           ArrayList<BluetoothSocket> socketArrayList,
                           PlayerListArrayAdapter listArrayAdapter) {

        // Use messenger to update bluetooth manger
        this.mMessenger = messenger;
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mListArrayAdapter = listArrayAdapter;
        this.mSocketArrayList = socketArrayList;
        this.mConnectedDevice = null;

        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;

        try {
            // APP_UUID is the app's UUID string, also used by the client code
             tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("GAME", APP_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Problem with creating listenUsingRfcommWithServiceRecord");
        }
        mmServerSocket = tmp;
        Log.d(TAG, "Class created");
    }

    // Start thread
    public void run() {
        Log.d(TAG, "Start Thread");

        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                Log.e(TAG, "Waiting to mmServerSocket.accept() ");
                socket = mmServerSocket.accept();
                Log.e(TAG, "SUCCESSFULLY CONNECTED");
            } catch (IOException e) {
                Log.e(TAG, "Problem with mmServerSocket.accept()  IOException:"+e.getMessage());
                break;
            }catch (NullPointerException e){
                Log.e(TAG, "Problem with mmServerSocket.accept() [null] ");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // get connected device
                mConnectedDevice = socket.getRemoteDevice();
                String deviceName = getDeviceName(mConnectedDevice);
                addDeviceToGame(socket,deviceName);
            }
            // todo break to close the thread
            break;
        }
    }

    private void addDeviceToGame(BluetoothSocket bluetoothSocket, String deviceName) {

        if (deviceName != null && bluetoothSocket != null){
            mSocketArrayList.add(bluetoothSocket);
            mListArrayAdapter.add(deviceName);
            sendMessageToManager(DEVICE_ADDED);
        }

    }

    private String getDeviceName(BluetoothDevice mConnectedDevice) {

        String[] split = null;
        if (mConnectedDevice.getName() != null)
            split = mConnectedDevice.getName().split("_");
        if (split != null && split.length > 0) {
            return split[1];
        }
        return null;
    }

    /**
     * Send message to activity
     * @param msg message type
     */
    private void sendMessageToManager(int msg)  {
        try {
            mMessenger.send(Message.obtain(null, msg));
        } catch (RemoteException e) {
            Log.e(TAG, "Problem with sendMessageToManager ");
        }
    }


}
