package com.omerbarr.cardsvshumanity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothServer;
import com.omerbarr.cardsvshumanity.Utils.PlayerListArrayAdapter;

import java.util.ArrayList;

public class CreateGameActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "DEBUG: "+CreateGameActivity.class.getSimpleName();


    // Views
    private EditText mPlayerName;
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

    // List of all connected devices
    private ArrayList<BluetoothSocket> mSocketArrayList;

    // Handler for all incoming messages from BL classes
    private final Messenger mMessenger = new Messenger(new IncomingHandler());



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

        // set boxes visibility
        mPlayersBox.setVisibility(View.GONE);
        mCodeBox.setVisibility(View.GONE);


        // set socket list
        mSocketArrayList = new ArrayList<>();
        mSocketArrayList.add(null);// my socket will be null

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
                if(mListArrayAdapter.getCount() >= 2)
                    Toast.makeText(this,"Start game",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"You need at least 2 players to start the game ",Toast.LENGTH_SHORT).show();

                break;

        }
    }

    private void startPublish() {
        BluetoothServer bluetoothServer = new BluetoothServer(this,mMessenger,mSocketArrayList,mListArrayAdapter);
        bluetoothServer.start();
        beDiscoverable();
    }

    private boolean validateName() {
        String tempName = mPlayerName.getText().toString();
        if (tempName.trim().length() >= 3)
            return true;
        return false;
    }

    /**
     * Handler of incoming messages from one of the BL classes
     */
    class IncomingHandler extends Handler {

        String address;
        BluetoothDevice bl;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.e(TAG, "DEVICE_CONNECTED_SUCCESSFULLY_TO_BLUETOOTH_SERVER");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    /**
     *  Make the device be discoverable
     */
    private void beDiscoverable(){
        BluetoothAdapter bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(discoverableIntent);
        }
    }
}
