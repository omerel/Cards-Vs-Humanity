package com.omerbarr.cardsvshumanity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.BusinessLogic.GameCommandsConstants;

public class GameActivity extends AppCompatActivity implements GameCommandsConstants, PickCardFragment.OnFragmentInteractionListener,WaitingToCzarFragment.OnFragmentInteractionListener {


    final int PLAYER_MODE = 1;
    final int CZAR_MODE = 2;
    final int PLAYER_WAITING = 3;
    final int CZAR_CZAR = 4;


    private TextView mTextScore;
    private TextView mRound;
    // current fragment
    private Fragment mFragment;

    // animation between views
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;

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
        mRound = (TextView)findViewById(R.id.text_game_round);
        mRound.setText(ROUND+1);

        boolean isCzar = getIntent().getBooleanExtra("czar",false);
        // start
        if (isCzar)
            displayFragment(CZAR_MODE);
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
                mFragment = PickCardFragment.newInstance(new int[]{1,14,22,6,8,7},1,13);
                break;
            case CZAR_MODE:
                break;
            case PLAYER_WAITING:
                mFragment = WaitingToCzarFragment.newInstance();
                break;
            case CZAR_CZAR:
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
        Toast.makeText(this,"the answers is "+pickedAnswers[0],Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(int cmd) {

    }
}
