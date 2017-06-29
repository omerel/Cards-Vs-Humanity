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
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_CZAR_MODE;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_CZAR_WAITING;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PICK_ROUND_WINNER;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PLAYER_MODE;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PLAYER_WAITING;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_SHOW_ROUND_RESULT;
import static com.omerbarr.cardsvshumanity.JoinGameActivity.BROAD_CAST_START_GAME;

/**
 * Created by omer on 15/06/2017.
 */

public class PlayerManager implements GameCommandsConstants {

    public static final String BROAD_CAST_ACK_WAITING = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_ACK_WAITING";
    public static final String UPDATE_CZAR_DATA = "cardsvshumanity.BroadcastReceiver.UPDATE_CZAR_DATA";
    public static final String UPDATE_PLAYER_DATA = "cardsvshumanity.BroadcastReceiver.UPDATE_PLAYER_DATA";
    public static final String FINISH_ROUND = "cardsvshumanity.BroadcastReceiver.FINISH_ROUND";
    public static final String UPDATE_ROUND_RESULT = "cardsvshumanity.BroadcastReceiver.UPDATE_ROUND_RESULT";

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


    private  DataTransferred.RoundData mRoundData;

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
        Intent intent;
        String  jsonRoundData;
        String  jsonPlayersData;

        Log.e(TAG,"JsonString received size is : "+jsonPacket.length());
        JsonConvertor.isJSONValid(jsonPacket);
        try {
            int command = JsonConvertor.getCommand(jsonPacket);
            switch (command) {
                case CMD_START_GAME:
                    Log.e(TAG, "CMD_START_GAME");
                    String  idString = JsonConvertor.getJsonContent(jsonPacket);
                    mBluetoothConnected.setId(Integer.valueOf(idString.trim()));
                    // send device name to activity to add to list
                    intent = new Intent(BROAD_CAST_START_GAME);
                    intent.putExtra("id",mBluetoothConnected.getMyId());
                    mServiceContext.sendBroadcast(intent);
                    sendPacket(ACK_START_GAME,"dummy");
                    break;
                case CMD_START_ROUND:
                    Log.e(TAG, "CMD_START_ROUND");
                    jsonRoundData = JsonConvertor.getJsonContent(jsonPacket);
                    mRoundData = JsonConvertor.JsonToRoundData(jsonRoundData);
                    // check manager mode(czar or player)
                    if (mBluetoothConnected.getMyId() == mRoundData.mCurrentCzar ){
                        // get into czar mode
                        intent = new Intent(BROAD_CAST_CZAR_MODE);
                        intent.putExtra("data",jsonRoundData);
                        mServiceContext.sendBroadcast(intent);
                        sendPacket(ACK_START_ROUND,"dummy");
                    }
                    else{
                        // get into player mode
                        intent = new Intent(BROAD_CAST_PLAYER_WAITING);
                        intent.putExtra("data",jsonRoundData);
                        mServiceContext.sendBroadcast(intent);
                        sendPacket(ACK_START_ROUND,"dummy");
                    }
                    break;
                case CMD_REVEAL_BLACK_CARD:
                    Log.e(TAG, "CMD_REVEAL_BLACK_CARD");
                    String  jsonCzarData = JsonConvertor.getJsonContent(jsonPacket);
                    // if the player is czar go to waiting
                    if (mBluetoothConnected.getMyId() == mRoundData.mCurrentCzar){
                        // get into czar waiting
                        intent = new Intent(BROAD_CAST_CZAR_WAITING);
                        intent.putExtra("data",jsonCzarData);
                        mServiceContext.sendBroadcast(intent);
                    }else{
                        intent = new Intent(BROAD_CAST_PLAYER_MODE);
                        intent.putExtra("data",jsonCzarData);
                        mServiceContext.sendBroadcast(intent);
                    }
                    sendPacket(ACK_REVEAL_BLACK_CARD,"dummy");
                    break;

                case CMD_SHOW_ROUND_RESULT:
                    Log.e(TAG, "CMD_SHOW_ROUND_RESULT");
                    jsonRoundData = JsonConvertor.getJsonContent(jsonPacket);
                    intent = new Intent(BROAD_CAST_SHOW_ROUND_RESULT);
                    intent.putExtra("data",jsonRoundData);
                    mServiceContext.sendBroadcast(intent);
                    break;

                case CMD_SHOW_ROUND_PLAYERS_ANSWERS:
                    Log.e(TAG, "CMD_SHOW_ROUND_PLAYERS_ANSWERS");
                    jsonPlayersData = JsonConvertor.getJsonContent(jsonPacket);
                    // go to pick roundWinner
                    intent = new Intent(BROAD_CAST_PICK_ROUND_WINNER);
                    intent.putExtra("data",jsonPlayersData);
                    mServiceContext.sendBroadcast(intent);
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
        mFilter.addAction(BROAD_CAST_ACK_WAITING);
        mFilter.addAction(UPDATE_CZAR_DATA);
        mFilter.addAction(UPDATE_PLAYER_DATA);
        mFilter.addAction(FINISH_ROUND);
        mFilter.addAction(UPDATE_ROUND_RESULT);

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case BROAD_CAST_ACK_WAITING:
                        Log.e(TAG,"BROAD_CAST_ACK_WAITING");
                        sendPacket(ACK_START_GAME,"dummy");
                        break;
                    case UPDATE_CZAR_DATA:
                        Log.e(TAG,"UPDATE_CZAR_DATA");
                        int cmd;
                        String content;
                        // send data to manager
                        int pickedCard = intent.getIntExtra("data",0);
                        cmd = CMD_REVEAL_BLACK_CARD;
                        content = JsonConvertor.convertToJson(new DataTransferred.CzarData(pickedCard));
                        sendPacket(cmd,content);
                        //go to waiting
                        break;
                    case UPDATE_PLAYER_DATA:
                        Log.e(TAG,"UPDATE_PLAYER_DATA");
                        // send data to manager
                        int[] pickedAnswers = intent.getIntArrayExtra("data");
                        DataTransferred.PlayerData playerData =
                                new DataTransferred.PlayerData(pickedAnswers,mBluetoothConnected.getMyId());
                        sendPacket(UPDATE_PLAYER_ANSWER,JsonConvertor.convertToJson(playerData));
                        break;

                    case UPDATE_ROUND_RESULT:
                        int winnerId = intent.getIntExtra("data",0);
                        sendPacket(UPDATE_ROUND_WINNER,String.valueOf(winnerId));
                        break;

                    case FINISH_ROUND:
                        sendPacket(CMD_FINISH_ROUND,"dummy");
                        break;
                }
            }
        };
        mServiceContext.registerReceiver(mBroadcastReceiver, mFilter);
    }

}
