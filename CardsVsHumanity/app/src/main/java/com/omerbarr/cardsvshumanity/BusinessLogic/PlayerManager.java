package com.omerbarr.cardsvshumanity.BusinessLogic;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConnected;
import com.omerbarr.cardsvshumanity.Utils.JsonConvertor;

import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConstants.READ_PACKET;
import static com.omerbarr.cardsvshumanity.JoinGameActivity.BROAD_CAST_START_GAME;

/**
 * Created by omer on 15/06/2017.
 */

public class PlayerManager implements GameCommandsConstants {

    public static final String BROAD_CAST_ACK_START_GAME = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_ACK_START_GAME";

    private final String TAG = "DEBUG: "+PlayerManager.class.getSimpleName();

    private BluetoothSocket mBluetoothSocket;
    private BluetoothConnected mBluetoothConnected;
    // Handler for all incoming messages from Bluetooth connected
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mServiceMessenger;
    private Context mServiceContext;

    // Player BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;


    public PlayerManager(BluetoothSocket mBluetoothSocket, Messenger mServiceMessenger, Context mServiceContext) {
        this.mBluetoothSocket = mBluetoothSocket;
        this.mServiceMessenger = mServiceMessenger;
        this.mServiceContext = mServiceContext;

        mBluetoothConnected = new BluetoothConnected(mBluetoothSocket,mMessenger,0);
        mBluetoothConnected.start();

        createPlayerBroadcastReceiver();
    }

    public void close(){
        mServiceContext.unregisterReceiver(mBroadcastReceiver);
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

    public void sendPacket(int command,String jsonContent){
        String jsonPacket = JsonConvertor.createJsonWithCommand(command,jsonContent);
        JsonConvertor.isJSONValid(jsonPacket);
        mBluetoothConnected.writePacket(jsonPacket);
        Log.e(TAG,"JsonString sent size is : "+jsonContent.length());
    }

    public void decodePacket(String jsonPacket){
        Log.e(TAG,"JsonString received size is : "+jsonPacket.length());
        JsonConvertor.isJSONValid(jsonPacket);
        try {
            int command = JsonConvertor.getCommand(jsonPacket);
            switch (command) {
                case CMD_START_GAME:
                    Log.e(TAG, "CMD_START_GAME");
                    String  string = JsonConvertor.getJsonContent(jsonPacket);
                    // send device name to activity to add to list
                    Intent msgToService = new Intent(BROAD_CAST_START_GAME);
                    mServiceContext.sendBroadcast(msgToService);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG,"Error in decodePacket method,error-"+e.getMessage());
        }
    }

    /**
     *  Player BroadcastReceiver
     */
    private void createPlayerBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(BROAD_CAST_ACK_START_GAME);
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case BROAD_CAST_ACK_START_GAME:
                        Log.e(TAG,"BROAD_CAST_ACK_START_GAME");
                        sendPacket(ACK_START_GAME,"dummy");
                        break;
                }
            }
        };
        mServiceContext.registerReceiver(mBroadcastReceiver, mFilter);
    }

}
