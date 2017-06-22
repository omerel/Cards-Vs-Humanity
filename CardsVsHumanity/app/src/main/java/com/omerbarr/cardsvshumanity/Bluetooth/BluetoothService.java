package com.omerbarr.cardsvshumanity.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.BusinessLogic.GameManager;
import com.omerbarr.cardsvshumanity.BusinessLogic.PlayerManager;

import java.io.IOException;
import java.util.ArrayList;

import static com.omerbarr.cardsvshumanity.CreateGameActivity.ADD_DEVICE_TO_LIST;
import static com.omerbarr.cardsvshumanity.JoinGameActivity.UPDATE_UI_FOUND_DEVICE;

/**
 * Created by omer on 15/06/2017.
 */

public class BluetoothService extends Service implements BluetoothConstants{

    private final String TAG = "DEBUG: "+ BluetoothService.class.getSimpleName();
    public static final String KILL_SERVICE = "cardsvshumanity.BroadcastReceiver.KILL_SERVICE";
    public static final String START_GAME = "cardsvshumanity.BroadcastReceiver.START_GAME";
    public static final String SET_MANAGER = "cardsvshumanity.BroadcastReceiver.SET_MANAGER";
    public static final String OPEN_SEVER = "cardsvshumanity.BroadcastReceiver.OPEN_SEVER";
    public static final String START_SEARCH = "cardsvshumanity.BroadcastReceiver.START_SEARCH";



    private boolean manager;
    private PowerManager.WakeLock mWakeLock;
    // Power manager to keep service wake when phone locked
    private PowerManager mPowerManager;
    // General BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    BluetoothDevice mFoundDevice;
    private String mGameCode;


    // List of all connected devices
    private ArrayList<BluetoothSocket> mSocketArrayList;
    private ArrayList<String> mPlayersNameArrayList;

    // Handler for all incoming messages from BL classes
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private BluetoothScan  mBluetoothScan;
    private BluetoothClient mBluetoothClient;

    // player
    private PlayerManager mPlayerManager;

    // manager
    private GameManager mGameManager;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service started");
        createGeneralBroadcastReceiver();
        setWakeLock();
        manager = false;

        // set socket list
        mSocketArrayList = new ArrayList<>();
        mSocketArrayList.add(null);// my socket will be null
        // initial players name and set  my name
        mPlayersNameArrayList = new ArrayList<>();
        return  START_NOT_STICKY;
    }

    public void setManager(boolean isManager){
       this.manager = isManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        Log.d(TAG, "Service destroyed");
    }
    /**
     *  General BroadcastReceiver
     */
    private void createGeneralBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(KILL_SERVICE);
        mFilter.addAction(SET_MANAGER);
        mFilter.addAction(OPEN_SEVER);
        mFilter.addAction(START_SEARCH);
        mFilter.addAction(START_GAME);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case KILL_SERVICE:
                        killService();
                        Log.e(TAG,"KILL_SERVICE");
                        break;
                    case SET_MANAGER:
                        Log.e(TAG,"SET_MANAGER");
                        boolean bool = intent.getBooleanExtra("isManager",false);
                        setManager(bool);
                        break;
                    case OPEN_SEVER:
                        Log.e(TAG,"OPEN_SEVER");
                        BluetoothServer bluetoothServer = new BluetoothServer(mMessenger,mSocketArrayList);
                        bluetoothServer.start();
                        break;
                    // When the device in wifi mode and bluetooth is on
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF){
                            Log.e(TAG,"Bluetooth is off");
                        }
                        if (state == BluetoothAdapter.STATE_ON) {
                            Log.e(TAG,"Bluetooth is on");
                        }
                        break;
                    case START_SEARCH:
                        mGameCode= intent.getStringExtra("code");
                        Log.e(TAG,"START_SEARCH for code: "+mGameCode);
                        mBluetoothScan = new BluetoothScan(getApplicationContext(),mGameCode,mMessenger);
                        mBluetoothScan.startScan();
                        break;
                    case START_GAME:
                        String name = intent.getStringExtra("name");
                        mPlayersNameArrayList.add(0,name);
                        mGameManager = new GameManager(mSocketArrayList,mPlayersNameArrayList,
                                mMessenger,getApplicationContext());
                        break;
                }
            }
        };
        registerReceiver(mBroadcastReceiver, mFilter);
    }

    /**
     * Kill service
     */
    private void killService(){


        // close sockets
        if (manager){
            // unregisterReceiver
            mGameManager.close();
           for(int i = 0; i < mSocketArrayList.size(); i++){
               if (mSocketArrayList.get(i) != null)
                   try {
                       mSocketArrayList.get(i).close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
           }
        }
        else {
            // unregisterReceiver
            mPlayerManager.close();
            //close socket if exist
            if (mBluetoothClient != null)
                mBluetoothClient.cancel();
        }
        // unregisterReceiver
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

        stopSelf();
    }

    /**
     * makes service works when device is lock
     */
    private void setWakeLock() {
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        mWakeLock.acquire();
    }

    /**
     * Handler of incoming messages from one of the BL classes
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_ADDED:
                    Log.e(TAG, "DEVICE_ADDED");
                    Toast.makeText(getApplicationContext(),"new player has joined the game",Toast.LENGTH_SHORT).show();
                    String name = msg.getData().getString("message");
                    mPlayersNameArrayList.add(name);
                    // send device name to activity to add to list
                    Intent msgToService = new Intent(ADD_DEVICE_TO_LIST);
                    msgToService.putExtra("name",name);
                    sendBroadcast(msgToService);
                    break;
                case DEVICE_FOUND:
                    Log.e(TAG, "DEVICE_FOUND");
                    mFoundDevice = mBluetoothScan.getDevice();
                    mBluetoothScan.close();
                    mBluetoothClient = new BluetoothClient(mMessenger,mFoundDevice);
                    mBluetoothClient.start();
                    break;

                case FAILED_CONNECTING_TO_DEVICE:
                    Log.e(TAG, "FAILED_CONNECTING_TO_DEVICE");
                    Log.e(TAG,"START_SEARCH for code: "+mGameCode);
                    mBluetoothScan = new BluetoothScan(getApplicationContext(),mGameCode,mMessenger);
                    mBluetoothScan.startScan();
                    break;

                case SUCCEED_CONNECTING_TO_DEVICE:
                    Log.e(TAG, "SUCCEED_CONNECTING_TO_DEVICE");
                    // connect to device
                    sendBroadcast(new Intent(UPDATE_UI_FOUND_DEVICE));
                    mPlayerManager = new PlayerManager(mBluetoothClient.getBluetoothSocket(),mMessenger,getApplicationContext());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
