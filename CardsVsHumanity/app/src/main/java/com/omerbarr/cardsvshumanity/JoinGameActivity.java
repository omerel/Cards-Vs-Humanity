package com.omerbarr.cardsvshumanity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothScan;
import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothConstants;
import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService;

import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.KILL_SERVICE;
import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.SET_MANAGER;
import static com.omerbarr.cardsvshumanity.Bluetooth.BluetoothService.START_SEARCH;
import static com.omerbarr.cardsvshumanity.CreateGameActivity.ADD_DEVICE_TO_LIST;

public class JoinGameActivity extends AppCompatActivity implements View.OnClickListener , BluetoothConstants {

    private final String TAG = "DEBUG: "+JoinGameActivity.class.getSimpleName();

    public static final String UPDATE_UI_FOUND_DEVICE = "cardsvshumanity.BroadcastReceiver.UPDATE_UI_FOUND_DEVICE";
    public static final String CMD_START_GAME = "cardsvshumanity.BroadcastReceiver.CMD_START_GAME";

    // Views
    private EditText mPlayerName;
    private EditText mGameCode;
    private Button mJoinButton;
    private View mPlayerBox;
    private TextView mConnectedSign;
    private TextView mWaitingSign;
    private ProgressBar mProgressBar;

    //post delay handler
    private Handler mHandler;

    // General BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        changeStatusBarColor();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Join game");

        // set views
        mPlayerName = (EditText) findViewById(R.id.edit_text_nick_name);
        mGameCode = (EditText) findViewById(R.id.edit_text_game_code);
        mJoinButton = (Button) findViewById(R.id.button_button_join);
        mJoinButton.setOnClickListener(this);
        mPlayerBox =  findViewById(R.id.player_box);
        mConnectedSign = (TextView) findViewById(R.id.text_connected);
        mConnectedSign.setVisibility(View.GONE);
        mProgressBar = (ProgressBar) findViewById(R.id.spinner_waiting);
        mProgressBar.setVisibility(View.GONE);
        mWaitingSign = (TextView) findViewById(R.id.text_waiting);
        mWaitingSign.setVisibility(View.GONE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startService(new Intent(JoinGameActivity.this,BluetoothService.class));

        createGeneralBroadcastReceiver();

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // BroadCast that i'm manager to Service
                Intent msgToService = new Intent(SET_MANAGER);
                msgToService.putExtra("isManager",false);
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
        msgToService.putExtra("isManager",true);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_button_join:
                if (validateInput()){
                    mPlayerName.setEnabled(false);
                    mGameCode.setEnabled(false);
                    mJoinButton.setEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mBluetoothAdapter.setName(mPlayerName.getText().toString().trim());
                    startSearch();
                }
                else{
                    Toast.makeText(this,"Nickname should be 3 letters or more\n" +
                            "and game code 4 letters",Toast.LENGTH_LONG).show();
                }

                break;
        }

    }

    private void startSearch() {
        // send device name to activity to add to list
        Intent msgToService = new Intent(START_SEARCH);
        msgToService.putExtra("code",mGameCode.getText().toString().trim());
        sendBroadcast(msgToService);
    }

    private boolean validateInput() {
        String tempName = mPlayerName.getText().toString();
        if (tempName.trim().length() >= 3)
            if(mGameCode.getText().toString().trim().length() == 4)
                return true;
        return false;
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


    private void createGeneralBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(UPDATE_UI_FOUND_DEVICE);
        mFilter.addAction(CMD_START_GAME);

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    // When incoming message received
                    case UPDATE_UI_FOUND_DEVICE:
                        mConnectedSign.setVisibility(View.VISIBLE);
                        mWaitingSign.setVisibility(View.VISIBLE);
                        break;

                    case CMD_START_GAME:
                        String string = intent.getStringExtra("start_game");
                        Toast.makeText(getApplicationContext(),string,Toast.LENGTH_LONG).show();
                        goToGameActivity();
                        break;
                }
            }
        };
        registerReceiver(mBroadcastReceiver, mFilter);
    }

    private void goToGameActivity() {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
