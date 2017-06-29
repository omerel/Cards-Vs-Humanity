package com.omerbarr.cardsvshumanity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothServer;
import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConstants;
import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService;
import com.omerbarr.cardsvshumanity.Utils.PlayerListArrayAdapter;

import java.util.ArrayList;
import java.util.Random;

import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.KILL_SERVICE;
import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.OPEN_SEVER;
import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.SET_MANAGER;
import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.START_GAME;

public class CreateGameActivity extends AppCompatActivity implements View.OnClickListener , BluetoothConstants {

    private final String TAG = "DEBUG: "+CreateGameActivity.class.getSimpleName();

    public static final String ADD_DEVICE_TO_LIST = "cardsvshumanity.BroadcastReceiver.ADD_DEVICE_TO_LIST";
    public static final String BROAD_CAST_START_GAME = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_START_GAME";


    // Views
    private EditText mPlayerName;
    private TextView mTextViewCode;
    private ListView mPlayerList;
    private Button mPublishButton;
    private Button mStartButton;
    private View mPlayerBox;
    private View mPlayersBox;
    private View mCodeBox;

    // Objects
    private PlayerListArrayAdapter mListArrayAdapter;
    private ArrayList<String> mPlayersArrayList;
    //post delay handler
    private Handler mHandler;

    private String mGamecode;

    private BluetoothAdapter mBluetoothAdapter;

    // General BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        changeStatusBarColor();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create new game");


        // set objects
        mPlayersArrayList = new ArrayList<>();
        mListArrayAdapter = new PlayerListArrayAdapter(this,mPlayersArrayList);
        mHandler = new Handler();

        // set views
        mPlayerName = (EditText) findViewById(R.id.edit_text_nick_name);
        mPlayerList = (ListView) findViewById(R.id.player_list);
        mPlayerList.setAdapter(mListArrayAdapter);
        mPublishButton = (Button) findViewById(R.id.button_button_publish);
        mPublishButton.setOnClickListener(this);
        mStartButton = (Button) findViewById(R.id.button_start_game);
        mStartButton.setOnClickListener(this);
        mPlayerBox =  findViewById(R.id.player_box);
        mCodeBox =  findViewById(R.id.code_box);
        mPlayersBox =  findViewById(R.id.players_box);
        mTextViewCode = (TextView) findViewById(R.id.text_game_code);

        // set boxes visibility
        mPlayersBox.setVisibility(View.GONE);
        mCodeBox.setVisibility(View.GONE);

        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        startService(new Intent(CreateGameActivity.this,BluetoothService.class));

        // activity broadastReceiver
        createGeneralBroadcastReceiver();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // BroadCast that i'm manager to Service
                Intent msgToService = new Intent(SET_MANAGER);
                msgToService.putExtra("isManager",true);
                sendBroadcast(msgToService);
            }
        },200);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(mBroadcastReceiver);
        // BroadCast kill service to Service
        Intent msgToService = new Intent(KILL_SERVICE);
        sendBroadcast(msgToService);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return false;
        }
        return true;
    }

    private void changeStatusBarColor(){
        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusBarColor));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_button_publish:
                if (validateName()){
                    mPlayerBox.setVisibility(View.GONE);
                    mPublishButton.setVisibility(View.GONE);
                    mPlayersBox.setVisibility(View.VISIBLE);
                    mCodeBox.setVisibility(View.VISIBLE);
                    mGamecode = generateGameCode();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListArrayAdapter.add("Player "+(mPlayersArrayList.size()+1)+": Me");
                        }
                    },1000);
                    startPublish();
                }
                else{
                    Toast.makeText(this,"Nickname should be 3 letters or more",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.button_start_game:
                if(mListArrayAdapter.getCount() >= 2) {
                    Toast.makeText(this, "Start game", Toast.LENGTH_SHORT).show();
                    // BroadCast start game to Service
                    Intent msgToService = new Intent(START_GAME);
                    msgToService.putExtra("name",mPlayerName.getText().toString());
                    sendBroadcast(msgToService);
                }
                else
                    Toast.makeText(this,"You need at least 2 players to start the game ",Toast.LENGTH_SHORT).show();

                break;

        }
    }

    private String generateGameCode() {
        Random random = new Random();
        String letters = "1234567890qwertyuiopasdfghjklzxcvbnm";
        String code = "";
        for (int i = 0; i < 4; i++) {
            code+=letters.charAt(random.nextInt(letters.length()));
        }
        mTextViewCode.setText(code);
        return code;
    }

    private void startPublish() {

        mBluetoothAdapter.setName(mGamecode+"_"+mPlayerName.getText().toString());
        beDiscoverable();
        sendBroadcast(new Intent(OPEN_SEVER));
    }

    private boolean validateName() {
        String tempName = mPlayerName.getText().toString();
        if (tempName.trim().length() >= 3)
            return true;
        return false;
    }


    /**
     *  Make the device be discoverable
     */
    private void beDiscoverable(){
        if (mBluetoothAdapter != null){
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,240);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(discoverableIntent);
        }
    }

    private void createGeneralBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(ADD_DEVICE_TO_LIST);
        mFilter.addAction(BROAD_CAST_START_GAME);

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case ADD_DEVICE_TO_LIST:
                        Log.e(TAG,"ADD_DEVICE_TO_LIST");
                        String deviceName = intent.getStringExtra("name");
                        mListArrayAdapter.add("Player "+(mPlayersArrayList.size()+1)+": "+deviceName);
                        break;
                    case BROAD_CAST_START_GAME:
                        int id = intent.getIntExtra("id",0);
                        goToGameActivity(true,id);
                        break;
                }
            }
        };
        registerReceiver(mBroadcastReceiver, mFilter);
    }


    private void goToGameActivity(boolean czar, int id) {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra("czar",czar);
        intent.putExtra("id",id);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
