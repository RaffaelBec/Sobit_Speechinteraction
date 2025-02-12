package com.example.javaspeechrecognizer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;

import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HotwordService extends Service implements RecognitionListener {

    private static final String TAG = "HotwordService";
    private static final String HOTWORD = "platon";
    private static final String CHANNEL_ID = "HotwordServiceChannel";
    private boolean isLaunchingApp = false;


    private Model model;
    private Recognizer recognizer;
    private SpeechService speechService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        // Erstelle den Notification Channel (ab API 26 erforderlich)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hotword Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Erstelle die Notification, die im Foreground-Service angezeigt werden soll
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Hotword Service")
                .setContentText("Die Hotword-Erkennung läuft im Hintergrund.")
                .setSmallIcon(R.drawable.splash_logo) // Ersetze ic_notification durch dein Icon
                .build();

        // Starte den Service als Foreground-Service
        startForeground(1, notification);

        Log.d(TAG, "Service started bluberi");
        // Führe hier die weitere Logik deines Service aus, z.B. startListener, etc.

        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initializeVosk();
                } catch (Exception e) {
                    Log.e(TAG, "Fehler bei der Initialisierung: ", e);
                }
            }
        }).start();

        createNotificationChannel();
        startForeground(1, getNotification());
        Log.d(TAG, "Foreground started");

    }

    private void initializeVosk() {
        try {
            String modelName = "vosk-model-small-de-0.15";
            File modelDir = new File(getExternalFilesDir(null), modelName);

            if (!modelDir.exists() || modelDir.list().length == 0) {
                Log.d("Vosk", "Copying model files...");
                copyAssets(modelName, modelName);
            }

            model = new Model(modelDir.getAbsolutePath());
            recognizer = new Recognizer(model, 16000);
            speechService = new SpeechService(recognizer, 16000);
            speechService.startListening(this);

            Log.d("Vosk", "Vosk initialized successfully");
        } catch (IOException e) {
            Log.e("Vosk", "Failed to initialize Vosk", e);
        }
    }

    private void copyAssets(String assetFolder, String outputFolder) throws IOException {
        AssetManager assetManager = getAssets();
        File outputDir = new File(getExternalFilesDir(null), outputFolder);
        if (!outputDir.exists()) outputDir.mkdirs();

        String[] files = assetManager.list(assetFolder);
        if (files == null || files.length == 0) {
            Log.e("Vosk", "Asset folder is empty: " + assetFolder);
            return;
        }

        for (String filename : files) {
            String assetPath = assetFolder + "/" + filename;
            File outFile = new File(outputDir, filename);

            if (assetManager.list(assetPath).length > 0) {
                outFile.mkdirs();
                copyAssets(assetPath, outputFolder + "/" + filename);
            } else {
                if (!outFile.exists()) {
                    try (InputStream in = assetManager.open(assetPath);
                         FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        Log.d("Vosk", "Copied: " + assetPath);
                    } catch (Exception e) {
                        Log.e("Vosk", "Failed to copy file: " + assetPath, e);
                    }
                }
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hotword Detection Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Listening for Hotword...")
                .setContentText("Say '" + HOTWORD + "' to wake up the app.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }



@Override
    public void onResult(String hypothesis) {
        if (hypothesis.contains(HOTWORD)) {
            Log.d(TAG, "Hotword detected");
            launchApp();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
        }
        Log.d(TAG, "Service stopped");
    }

    @Override
    public void onPartialResult(String hypothesis) {
        if (hypothesis.contains(HOTWORD)) {
            Log.d(TAG, "Hotword detected");
            launchApp();
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        if (hypothesis.contains(HOTWORD)) {
            launchApp();
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Error in speech recognition", e);
    }

    @Override
    public void onTimeout() {
        Log.d(TAG, "Speech service timeout");
    }


    private void launchApp() {
        if (isLaunchingApp) {
            return;
        }
        isLaunchingApp = true;
        Log.d(TAG, "Launching app...");
        // 2 Versuche
         //Intent intent = new Intent(this, MainActivity.class);
         //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         //startActivity(intent);

        Intent intent = new Intent("com.example.javaspeechrecognizer.HOTWORD_DETECTED");
        TODO:sendBroadcast(intent);

        // Setze das Flag nach einer definierten Verzögerung zurück (z.B. 1000 Millisekunden)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isLaunchingApp = false;
            }
        }, 1000);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}