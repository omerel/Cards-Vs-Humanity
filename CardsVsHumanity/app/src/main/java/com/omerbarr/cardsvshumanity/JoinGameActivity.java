package com.omerbarr.cardsvshumanity;

import android.bluetooth.BluetoothDevice;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.Bluetooth.BluetoothScan;
import com.omerbarr.cardsvshumanity.Bluetooth.BlutoothConstants;

public class JoinGameActivity extends AppCompatActivity implements View.OnClickListener , BlutoothConstants {

    private final String TAG = "DEBUG: "+JoinGameActivity.class.getSimpleName();

    // Views
    private EditText mPlayerName;
    private EditText mGameCode;
    private Button mJoinButton;
    private View mPlayerBox;
    private TextView mConnectedSign;
    private TextView mWaitingSign;
    private ProgressBar mProgressBar;

    // Handler for all incoming messages from BL classes
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

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
        BluetoothScan  bluetoothScan = new BluetoothScan(this,
                mGameCode.getText().toString().trim(),mMessenger);
        bluetoothScan.startScan();
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

    /**
     * Handler of incoming messages from one of the BL classes
     */
    class IncomingHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTED:
                    Log.e(TAG, "DEVICE_CONNECTED");
                    mConnectedSign.setVisibility(View.VISIBLE);
                    mWaitingSign.setVisibility(View.VISIBLE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
