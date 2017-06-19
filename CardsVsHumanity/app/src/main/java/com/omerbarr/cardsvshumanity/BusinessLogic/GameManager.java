package com.omerbarr.cardsvshumanity.BusinessLogic;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConnected;
import com.omerbarr.cardsvshumanity.Utils.JsonConvertor;

import java.util.ArrayList;

import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConstants.READ_PACKET;

/**
 * Created by omer on 15/06/2017.
 */

public class GameManager implements  GameCommandsConstants {

    private final String TAG = "DEBUG: "+GameManager.class.getSimpleName();

    private final int ALL_DEVICES = -1;

    private ArrayList<BluetoothSocket> mSocketArrayList;
    private ArrayList<String> mPlayersNameArrayList;
    private BluetoothConnected[] mBluetoothConnections;
    // Handler for all incoming messages from Bluetooth connected
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mServiceMessenger;
    private Context mServiceContext;

    public GameManager(ArrayList<BluetoothSocket> mSocketArrayList,
                       ArrayList<String> mPlayersNameArrayList, Messenger mServiceMessenger,
                       Context mServiceContext) {
        this.mSocketArrayList = mSocketArrayList;
        this.mPlayersNameArrayList = mPlayersNameArrayList;
        this.mServiceMessenger = mServiceMessenger;
        this.mServiceContext = mServiceContext;


        // initialize all devices
        mBluetoothConnections = new BluetoothConnected[mSocketArrayList.size()-1];
        for (int i = 0; i< mBluetoothConnections.length; i++){
            mBluetoothConnections[i] = new BluetoothConnected(mSocketArrayList.get(i+1),mMessenger);
            mBluetoothConnections[i].start();
        }

        sendPacket(ALL_DEVICES,TESTING,"hello world");
    }

    /**
     * Handler of incoming messages from one of the BL classes
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_PACKET:
                    Log.e(TAG, "READ_PACKET");
                    String packet = msg.getData().getString("packet");
                    // update handshake with the new packet
                    decodePacket(packet);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    // if deviceNumber is -1, send message to all devices
    private void sendPacket(int deviceNumber, int command ,String jsonContent){
        String jsonPacket = JsonConvertor.createJsonWithCommand(command,jsonContent);
        JsonConvertor.isJSONValid(jsonPacket);
        if (deviceNumber == ALL_DEVICES){
            for (int i = 0; i< mBluetoothConnections.length; i++)
                mBluetoothConnections[i].writePacket(jsonPacket);
        }
        else
            mBluetoothConnections[deviceNumber].writePacket(jsonPacket);
        Log.e(TAG,"JsonString sent size is : "+jsonContent.length());
    }

    public void decodePacket(String jsonPacket){
        Log.e(TAG,"JsonString received size is : "+jsonPacket.length());
        JsonConvertor.isJSONValid(jsonPacket);
        try {
            int command = JsonConvertor.getCommand(jsonPacket);
            switch (command) {
                case TESTING:
                    Log.e(TAG, "TESTING");

                    break;
            }
        } catch (Exception e) {
            Log.e(TAG,"Error in decodePacket method,error-"+e.getMessage());
        }
    }
}
