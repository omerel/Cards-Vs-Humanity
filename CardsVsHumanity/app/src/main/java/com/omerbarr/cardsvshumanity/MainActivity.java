package com.omerbarr.cardsvshumanity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {


    private android.support.v7.widget.AppCompatButton mButtonCreateGame;
    private android.support.v7.widget.AppCompatButton mButtonJoinGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeStatusBarColor();

        mButtonCreateGame = (AppCompatButton) findViewById(R.id.button_main_create_new_game);
        mButtonCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCreateGameActivity();
            }
        });
        mButtonJoinGame = (AppCompatButton) findViewById(R.id.button_main_join_game);
        mButtonJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToJoinGameActivity();
            }
        });

    }


    private void goToJoinGameActivity() {
        Intent intent = new Intent(getApplicationContext(), JoinGameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void goToCreateGameActivity() {
        Intent intent = new Intent(getApplicationContext(), CreateGameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
}
