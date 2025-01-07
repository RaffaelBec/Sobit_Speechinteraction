package com.example.javaspeechrecognizer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class HelpActivity extends AppCompatActivity { //previously called HelpActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //removes action bar color
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help); // This will be your custom layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.help), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setting up a simple TextView to explain help or settings
        TextView textViewHelp = findViewById(R.id.textViewHelp);

        // Add the button to check if German is supported
        Button checkGermanButton = findViewById(R.id.checkGermanButton);
        checkGermanButton.setOnClickListener(v -> {
            // Call the method to check if German is supported for speech recognition
            checkIfGermanLocaleIsSupported(this);
        });
    }

    public void checkIfGermanLocaleIsSupported(Context context) {
        // Check if speech recognition is available on the device
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to initiate speech recognition in German and catch any exception
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY);

            // Launch the speech recognition intent
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);

            // If activities are found, it indicates that German is supported
            if (activities.size() > 0) {
                Toast.makeText(context, "German is supported for speech recognition.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "German is not supported for speech recognition.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // In case of any errors, assume that the language is not supported
            Toast.makeText(context, "Error checking for German language support.", Toast.LENGTH_SHORT).show();
        }
    }

//        public void checkIfGermanLocaleIsSupported(Context context) {
//        // Check if speech recognition is available on the device
//        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
//            Toast.makeText(context, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Get the list of locales supported for speech recognition
//        List<Locale> availableLocales = RecognizerIntent.getLocales();
//
//        // Check if German (Locale.GERMANY) is supported
//        boolean isGermanSupported = false;
//        for (Locale locale : availableLocales) {
//            if (locale.equals(Locale.GERMANY)) {
//                isGermanSupported = true;
//                break;
//            }
//        }
//
//        // Display a message based on whether German is supported
//        if (isGermanSupported) {
//            Toast.makeText(context, "German is supported for speech recognition.", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, "German is not supported for speech recognition.", Toast.LENGTH_SHORT).show();
//        }
//    }
}
