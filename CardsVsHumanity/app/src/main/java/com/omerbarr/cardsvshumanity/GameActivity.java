package com.omerbarr.cardsvshumanity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.omerbarr.cardsvshumanity.BusinessLogic.Cards;
import com.omerbarr.cardsvshumanity.BusinessLogic.DataTransferred;
import com.omerbarr.cardsvshumanity.BusinessLogic.GameCommandsConstants;
import com.omerbarr.cardsvshumanity.Utils.JsonConvertor;


public class GameActivity extends AppCompatActivity implements GameCommandsConstants,
        PickWhiteCardFragment.OnFragmentInteractionListener,PickBlackCardFragment.OnFragmentInteractionListener,
        WaitingToPlayersFragment.OnFragmentInteractionListener, WaitingToCzarFragment.OnFragmentInteractionListener,
        PickRoundWinnerFragment.OnFragmentInteractionListener,RoundWinnerFragment.OnFragmentInteractionListener{

    private final String TAG = "DEBUG: "+GameActivity.class.getSimpleName();

    public static final String BROAD_CAST_CZAR_MODE = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_CZAR_MODE";
    public static final String BROAD_CAST_PLAYER_MODE = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_PLAYER_MODE";
    public static final String BROAD_CAST_PLAYER_WAITING = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_PLAYER_WAITING";
    public static final String BROAD_CAST_CZAR_WAITING = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_CZAR_WAITING";
    public static final String BROAD_CAST_PICK_ROUND_WINNER = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_PICK_ROUND_WINNER";
    public static final String BROAD_CAST_SHOW_ROUND_RESULT = "cardsvshumanity.BroadcastReceiver.BROAD_CAST_SHOW_ROUND_RESULT";
    public static final String UPDATE_MANAGER_WITH_CZAR_DATA = "cardsvshumanity.BroadcastReceiver.UPDATE_MANAGER_WITH_CZAR_DATA";




    final int PLAYER_MODE = 1;
    final int CZAR_MODE = 2;
    final int PLAYER_WAITING = 3;
    final int CZAR_WAITING = 4;
    final int PICK_ROUND_WINNER = 5;
    final int ROUND_WINNER = 6;


    private TextView mTextScore;
    private TextView mRound;
    // current fragment
    private Fragment mFragment;

    // animation between views
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;

    // Activity BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    private int myId;

    // picked answers from all users
    private DataTransferred.PlayerData[] mPlayersData;

    private DataTransferred.RoundData recievedRoundData;
    private DataTransferred.CzarData recievedCzarData;
    private DataTransferred.PlayerData recievedPlayerData;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        backDialog("Warning","Are you sure you want to quit?");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        changeStatusBarColor();

        mContentView = findViewById(R.id.content_body);
        mLoadingView = findViewById(R.id.loading_spinner);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        mTextScore = (TextView)findViewById(R.id.text_score);
        mTextScore.setText(SCORE+0);

        mTextScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowScoreTable();
            }
        });
        mRound = (TextView)findViewById(R.id.text_game_round);
        mRound.setText(ROUND+1);

        createGameActivityBroadcastReceiver();

        myId = getIntent().getIntExtra("id",0);
        boolean isCzar = getIntent().getBooleanExtra("czar",false);

        // start
        if (isCzar)
            displayFragment(CZAR_WAITING);
        else
            displayFragment(PLAYER_WAITING);
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
     * Animation between two views
     */
    private void crossfade(int mShortAnimationDuration) {

        // setup progress bar
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setAlpha(1f);

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        mContentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        mLoadingView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setVisibility(View.GONE);
                    }
                });
    }


    private void displayFragment(int mode) {

        //Initially hide the content view.
        mFragment = null;
        mContentView.setVisibility(View.INVISIBLE);

        switch (mode){
            case PLAYER_MODE:
                String blackCard = Cards.BLACK_CARDS[recievedCzarData.pickedBlackCard];
                int answers = blackCard.split("_").length-1;
                // if there is no "_" in the sentence
                if (answers == 0)
                    answers = 1;
                int[] cards =  new int [recievedRoundData.mPlayersCardsPull[myId].size()];
                for(int i=0;i<cards.length;i++)
                        cards[i] = recievedRoundData.mPlayersCardsPull[myId].get(i);
                mFragment = PickWhiteCardFragment.newPickWhiteCardFragment(cards,answers,recievedCzarData.pickedBlackCard);
                break;
            case CZAR_MODE:
                mFragment = PickBlackCardFragment.newPickBlackCardFragment(JsonConvertor.convertToJson(recievedRoundData));
                break;
            case PLAYER_WAITING:
                mFragment = WaitingToCzarFragment.newWaitingToCzarFragment();
                break;
            case CZAR_WAITING:
                mFragment = WaitingToPlayersFragment.newWaitingToPlayersFragment();
                break;
            case PICK_ROUND_WINNER:
                boolean amICzar = (myId == recievedRoundData.mCurrentCzar);
                mFragment = PickRoundWinnerFragment.newPickRoundWinnerFragment(JsonConvertor.convertToJson(mPlayersData),
                        recievedCzarData.pickedBlackCard,amICzar);
                break;
            case ROUND_WINNER:
                mTextScore.setText(SCORE+recievedRoundData.mScoreTable[myId]);
                mFragment = RoundWinnerFragment.newRoundWinnerFragment(JsonConvertor.convertToJson(recievedRoundData),myId);
                break;
        }

        if (mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_body, mFragment);
            fragmentTransaction.commit();
        }
        crossfade(mShortAnimationDuration);
    }


    @Override
    public void onFragmentInteraction(int[] pickedAnswers) {
    }

    @Override
    public void onFragmentInteraction(int cmd) {
        // not in use
    }

    /**
     *  Player BroadcastReceiver
     */
    private void createGameActivityBroadcastReceiver() {

        mFilter = new IntentFilter();
        mFilter.addAction(BROAD_CAST_CZAR_MODE);
        mFilter.addAction(BROAD_CAST_CZAR_WAITING);
        mFilter.addAction(BROAD_CAST_PLAYER_MODE);
        mFilter.addAction(BROAD_CAST_PLAYER_WAITING);
        mFilter.addAction(BROAD_CAST_PICK_ROUND_WINNER);
        mFilter.addAction(UPDATE_MANAGER_WITH_CZAR_DATA);
        mFilter.addAction(BROAD_CAST_SHOW_ROUND_RESULT);



        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    case BROAD_CAST_CZAR_MODE:
                        recievedRoundData = JsonConvertor.JsonToRoundData(intent.getStringExtra("data"));
                        mTextScore.setText(SCORE+recievedRoundData.mScoreTable[myId]);
                        mRound.setText(ROUND+recievedRoundData.mRound);
                        displayFragment(CZAR_MODE);
                        Log.e(TAG,"BROAD_CAST_CZAR_MODE");
                        break;
                    case BROAD_CAST_CZAR_WAITING:
                        displayFragment(CZAR_WAITING);
                        Log.e(TAG,"BROAD_CAST_CZAR_WAITING");
                        break;
                    case BROAD_CAST_PLAYER_MODE:
                        Log.e(TAG,"BROAD_CAST_PLAYER_MODE");
                        recievedCzarData = JsonConvertor.JsonToCzarData(intent.getStringExtra("data"));
                        displayFragment(PLAYER_MODE);

                        break;
                    case BROAD_CAST_PLAYER_WAITING:
                        Log.e(TAG,"BROAD_CAST_PLAYER_WAITING");
                        recievedRoundData = JsonConvertor.JsonToRoundData(intent.getStringExtra("data"));
                        mTextScore.setText(SCORE+recievedRoundData.mScoreTable[myId]);
                        mRound.setText(ROUND+recievedRoundData.mRound);
                        displayFragment(PLAYER_WAITING);
                        break;
                    case BROAD_CAST_PICK_ROUND_WINNER:
                        String data = intent.getStringExtra("data");
                        mPlayersData = JsonConvertor.JsonToPlayersData(data);
                        displayFragment(PICK_ROUND_WINNER);
                        break;
                    case UPDATE_MANAGER_WITH_CZAR_DATA:
                        Log.e(TAG,"UPDATE_MANAGER_WITH_CZAR_DATA");
                        recievedCzarData = JsonConvertor.JsonToCzarData(intent.getStringExtra("data"));
                        break;
                    case BROAD_CAST_SHOW_ROUND_RESULT:
                        Log.e(TAG,"BROAD_CAST_SHOW_ROUND_RESULT");
                        recievedRoundData = JsonConvertor.JsonToRoundData(intent.getStringExtra("data"));
                        displayFragment(ROUND_WINNER);
                        break;
                }
            }
        };
        registerReceiver(mBroadcastReceiver, mFilter);
    }

    private void ShowScoreTable() {

        String score="";
        if (recievedRoundData != null){
            for (int i = 0; i< recievedRoundData.mPlayersNameArrayList.size(); i++){
                score+= "\nPlayer "+(i+1)+"("+recievedRoundData.mPlayersNameArrayList.get(i)+"): "+
                        recievedRoundData.mScoreTable[i]+" points.";
            }

        }
        else
            score = "No data available";


        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Score");
        alertDialog.setMessage(score);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
    }

    private void backDialog(String title, String content) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(content);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        unregisterReceiver(mBroadcastReceiver);
                        onDestroy();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog.show();
    }


}
