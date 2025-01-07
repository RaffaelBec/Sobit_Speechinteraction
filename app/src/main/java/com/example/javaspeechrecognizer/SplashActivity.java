package com.example.javaspeechrecognizer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import androidx.core.splashscreen.SplashScreen;


import com.example.javaspeechrecognizer.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Attach the splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Optionally customize splash screen exit animation
        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            // Customize the exit animation if needed
            splashScreenView.remove(); // Remove splash screen after animation
        });

        // After the splash screen is done, start LoginActivity
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }
}
