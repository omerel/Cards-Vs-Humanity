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

import java.util.ArrayList;

import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConstants.READ_PACKET;
import static com.omerbarr.cardsvshumanity.CreateGameActivity.BROAD_CAST_START_GAME;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_CZAR_MODE;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_CZAR_WAITING;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PICK_ROUND_WINNER;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PLAYER_MODE;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_PLAYER_WAITING;
import static com.omerbarr.cardsvshumanity.GameActivity.BROAD_CAST_SHOW_ROUND_RESULT;



/**
 * Created by omer on 15/06/2017.
 */

public class GameManager implements  GameCommandsConstants {


    private final String TAG = "DEBUG: "+GameManager.class.getSimpleName();
    public static final String UPDATE_CZAR_DATA = "cardsvshumanity.BroadcastReceiver.UPDATE_CZAR_DATA";
    public static final String UPDATE_PLAYER_DATA = "cardsvshumanity.BroadcastReceiver.UPDATE_PLAYER_DATA";
    public static final String UPDATE_ROUND_RESULT = "cardsvshumanity.BroadcastReceiver.UPDATE_ROUND_RESULT";
    public static final String FINISH_ROUND = "cardsvshumanity.BroadcastReceiver.FINISH_ROUND";


    private final int ALL_DEVICES = -1;

    private ArrayList<BluetoothSocket> mSocketArrayList;
    private ArrayList<String> mPlayersNameArrayList;
    private BluetoothConnected[] mBluetoothConnections;
    // Handler for all incoming messages from Bluetooth connected
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mServiceMessenger;
    private Context mServiceContext;
    private GameData mGameData;

    // save last CMD that sent to devices
    private String mLastJsonSent;
    private int mLastCommandSent;
    // every command that send will respond with ack if not the command will be send again
    private boolean[] mAckStatus;
    private Runnable mRunnable;

    // picked answers from all users
    private DataTransferred.PlayerData[] mPlayersData;

    // handler
    private Handler mHandler;

    // Player BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    public GameManager(ArrayList<BluetoothSocket> socketArrayList,
                       ArrayList<String> playersNameArrayList, Messenger serviceMessenger,
                       Context serviceContext) {
        this.mSocketArrayList = socketArrayList;
        this.mPlayersNameArrayList = playersNameArrayList;
        this.mServiceMessenger = serviceMessenger;
        this.mServiceContext = serviceContext;
        this.mGameData = new GameData(mPlayersNameArrayList);
        this.mHandler = new Handler();
        this.mAckStatus = new boolean[mSocketArrayList.size()];
        // initialize all devices
        mBluetoothConnections = new BluetoothConnected[mSocketArrayList.size()];
        mBluetoothConnections[0]= null; //manager
        for (int i = 1; i< mBluetoothConnections.length; i++){
            mBluetoothConnections[i] = new BluetoothConnected(mSocketArrayList.get(i),mMessenger,i);
            mBluetoothConnections[i].start();
        }

        // timer for device to respond
        this.mRunnable = new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i< mAckStatus.length; i++){
                    if (!mAckStatus[i])
                        sendPacket(i,mLastCommandSent,mLastJsonSent);
                }
                mHandler.postDelayed(mRunnable,MAX_TIME_FOR_RESPONED);
            }
        };

        createGameBroadcastReceiver();

        Intent intent  = new Intent(BROAD_CAST_START_GAME);
        intent.putExtra("id",0);
        mServiceContext.sendBroadcast(intent);

        resetAckStatus();
        sendPacketFirstTime();
    }

    public void close(){
        mServiceContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void resetAckStatus() {
        mAckStatus[0] = true;
        for (int i = 1; i < mSocketArrayList.size(); i++)
            mAckStatus[i] = false;
    }

    private void resetAckStatus(int czar) {
        for (int i = 0; i < mSocketArrayList.size(); i++)
            mAckStatus[i] = false;
        mAckStatus[czar] = true;
    }


    private void startRound(){
        mGameData.startRound();
        resetAckStatus();
        mLastCommandSent = CMD_START_ROUND;
        mLastJsonSent = JsonConvertor.convertToJson(mGameData.getRoundData());
        sendPacket(ALL_DEVICES,mLastCommandSent,mLastJsonSent);
        mHandler.postDelayed(mRunnable,MAX_TIME_FOR_RESPONED);
        // initial players picked answers
        this.mPlayersData = new DataTransferred.PlayerData[mBluetoothConnections.length];
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
                    int deviceId = msg.getData().getInt("id");
                    // update handshake with the new packet
                    decodePacket(packet,deviceId);
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
            for (int i = 1; i< mBluetoothConnections.length; i++)
                mBluetoothConnections[i].writePacket(jsonPacket);
        }
        else
            mBluetoothConnections[deviceNumber].writePacket(jsonPacket);
        Log.e(TAG,"JsonString sent size is : "+jsonContent.length());
    }

    private void sendPacketFirstTime(){
        for (int i = 1; i< mBluetoothConnections.length; i++) {
            String jsonPacket = JsonConvertor.createJsonWithCommand(CMD_START_GAME,String.valueOf(i));
            mBluetoothConnections[i].writePacket(jsonPacket);
        }

    }

    public void decodePacket(String jsonPacket,int deviceId){
        Log.e(TAG,"JsonString received size is : "+jsonPacket.length());
        Intent intent;
        JsonConvertor.isJSONValid(jsonPacket);
        try {
            int command = JsonConvertor.getCommand(jsonPacket);
            switch (command) {
                case ACK_START_GAME:
                    Log.e(TAG, "ACK_START_GAME");
                    mAckStatus[deviceId] = true;
                    if(checkAllDevicesReceived()){
                        startRound();
                    }
                    break;
                    case ACK_START_ROUND:
                    Log.e(TAG, "ACK_START_ROUND");
                    Log.e(TAG, "CZR:"+  mGameData.getCurrentCzar());
                    mAckStatus[deviceId] = true;
                    if(checkAllDevicesReceived()){
                        // cancel timer
                        mHandler.removeCallbacks(mRunnable);
                        // check manager mode(czar or player)
                        if (mGameData.getCurrentCzar() == 0){
                            // get into czar mode
                            intent = new Intent(BROAD_CAST_CZAR_MODE);
                            intent.putExtra("data",mLastJsonSent);
                            mServiceContext.sendBroadcast(intent);
                        }
                        else{
                            // get into player mode
                            Log.e(TAG, "START_ROUND");
                            intent = new Intent(BROAD_CAST_PLAYER_WAITING);
                            intent.putExtra("data",JsonConvertor.convertToJson(mGameData.getRoundData()));
                            mServiceContext.sendBroadcast(intent);
                        }
                    }
                    break;

                    //game manager will get it if a player manger is a czar
                case CMD_REVEAL_BLACK_CARD:
                    Log.e(TAG,"CMD_REVEAL_BLACK_CARD");
                    DataTransferred.CzarData czarData = JsonConvertor.JsonToCzarData(jsonPacket);
                    mGameData.pickBlackCard(czarData.pickedBlackCard);
                    resetAckStatus();
                    mLastCommandSent = CMD_REVEAL_BLACK_CARD; // send to all the rest of players
                    mLastJsonSent = JsonConvertor.getJsonContent(jsonPacket);
                    sendPacket(ALL_DEVICES,mLastCommandSent,mLastJsonSent);
                    break;

                    case ACK_REVEAL_BLACK_CARD:
                    Log.e(TAG, "ACK_REVEAL_BLACK_CARD");
                    mAckStatus[deviceId] = true;
                    if(checkAllDevicesReceived()){
                        // cancel timer
                        mHandler.removeCallbacks(mRunnable);
                        if (mGameData.getCurrentCzar() == 0){
                            // get into czar waiting
                            intent = new Intent(BROAD_CAST_CZAR_WAITING);
                            intent.putExtra("data",mLastJsonSent);
                            mServiceContext.sendBroadcast(intent);
                        }
                        else{
                            intent = new Intent(BROAD_CAST_PLAYER_MODE);
                            intent.putExtra("data",mLastJsonSent);
                            mServiceContext.sendBroadcast(intent);
                        }
                        // reset for the next operation- get answers from players
                        resetAckStatus(mGameData.getCurrentCzar());
                    }
                    break;
                    case UPDATE_PLAYER_ANSWER:
                    Log.e(TAG, "UPDATE_PLAYER_ANSWER");
                    mAckStatus[deviceId] = true;
                        String content = JsonConvertor.getJsonContent(jsonPacket);
                        DataTransferred.PlayerData playerData = JsonConvertor.JsonToPlayerData(content);
                        mPlayersData[deviceId] = playerData;
                        mGameData.removeCardFromPlayer(deviceId,playerData.pickedAnswers);

                    if(checkAllDevicesReceived()){
                        // go to pick roundWinner
                        intent = new Intent(BROAD_CAST_PICK_ROUND_WINNER);
                        intent.putExtra("data",JsonConvertor.convertToJson(mPlayersData));
                        mServiceContext.sendBroadcast(intent);
                        // send devices players answers
                        sendPacket(ALL_DEVICES,CMD_SHOW_ROUND_PLAYERS_ANSWERS,JsonConvertor.convertToJson(mPlayersData));
                    }
                    break;

                case UPDATE_ROUND_WINNER:
                    int winnerId = Integer.valueOf(JsonConvertor.getJsonContent(jsonPacket));
                    mGameData.addScoreToPlayer(winnerId);

                    mLastCommandSent = CMD_SHOW_ROUND_RESULT;
                    mLastJsonSent = JsonConvertor.convertToJson(mGameData.getRoundData());
                    sendPacket(ALL_DEVICES,mLastCommandSent,mLastJsonSent);

                    intent = new Intent(BROAD_CAST_SHOW_ROUND_RESULT);
                    intent.putExtra("data",mLastJsonSent);
                    mServiceContext.sendBroadcast(intent);
                    break;

                case CMD_FINISH_ROUND:
                    startRound();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG,"Error in decodePacket method,error-"+e.getMessage());
        }
    }

    private boolean checkAllDevicesReceived(){
        boolean received = true;
        for(int i = 0; i< mAckStatus.length; i++)
            if (!mAckStatus[i])
                received = false;
        return  received;
    }
    /**
     *  Player BroadcastReceiver
     */
    private void createGameBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(UPDATE_PLAYER_DATA);
        mFilter.addAction(UPDATE_CZAR_DATA);
        mFilter.addAction(UPDATE_ROUND_RESULT);
        mFilter.addAction(FINISH_ROUND);


        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case UPDATE_PLAYER_DATA:
                        Log.e(TAG,"UPDATE_PLAYER_DATA");
                        mAckStatus[0] = true;
                        // send data to manager
                        int[] pickedAnswers = intent.getIntArrayExtra("data");
                        DataTransferred.PlayerData playerData =
                                new DataTransferred.PlayerData(pickedAnswers,0);
                        mPlayersData[0] = playerData;
                        mGameData.removeCardFromPlayer(0,playerData.pickedAnswers);

                        if(checkAllDevicesReceived()){
                            // go to pick roundWinner
                            intent = new Intent(BROAD_CAST_PICK_ROUND_WINNER);
                            intent.putExtra("data",JsonConvertor.convertToJson(mPlayersData));
                            mServiceContext.sendBroadcast(intent);
                            // send devices players answers
                            sendPacket(ALL_DEVICES,CMD_SHOW_ROUND_PLAYERS_ANSWERS,JsonConvertor.convertToJson(mPlayersData));
                        }

                        break;
                    case UPDATE_CZAR_DATA:
                            Log.e(TAG,"UPDATE_CZAR_DATA");
                            int pickedCard = intent.getIntExtra("data",0);
                            mGameData.pickBlackCard(pickedCard);
                            mLastCommandSent = CMD_REVEAL_BLACK_CARD;
                            mLastJsonSent = JsonConvertor.convertToJson(new DataTransferred.CzarData(pickedCard));
                            sendPacket(ALL_DEVICES,mLastCommandSent,mLastJsonSent);
                            resetAckStatus();
                        break;
                    case UPDATE_ROUND_RESULT:
                        int winnerId = intent.getIntExtra("data",0);
                        mGameData.addScoreToPlayer(winnerId);

                        mLastCommandSent = CMD_SHOW_ROUND_RESULT;
                        mLastJsonSent = JsonConvertor.convertToJson(mGameData.getRoundData());
                        sendPacket(ALL_DEVICES,mLastCommandSent,mLastJsonSent);

                        intent = new Intent(BROAD_CAST_SHOW_ROUND_RESULT);
                        intent.putExtra("data",mLastJsonSent);
                        mServiceContext.sendBroadcast(intent);
                        break;

                    case FINISH_ROUND:
                        startRound();
                        break;
                }
            }
        };
        mServiceContext.registerReceiver(mBroadcastReceiver, mFilter);
    }
}
