package com.example.application;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application.activities.MainActivity;
import com.example.application.activities.login;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        Intent i;
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            i = new Intent(SplashScreen.this, MainActivity.class);
            Toast.makeText(this, "Logging in as: " + preferenceManager.getString(Constants.KEY_NAME), Toast.LENGTH_SHORT).show();
        } else if (preferenceManager.getString(Constants.KEY_NAME) == null || preferenceManager.getString(Constants.KEY_NAME).equals("")) {
            i = new Intent(SplashScreen.this, login.class);
            Toast.makeText(this, "Welcome User ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Internal error with one time authentication ", Toast.LENGTH_SHORT).show();
            i = new Intent();

        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}

