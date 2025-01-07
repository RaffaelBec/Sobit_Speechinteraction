package com.example.javaspeechrecognizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;


public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText editTextFirstName, editTextLastName;
    private Button buttonLogin;

    // ExecutorService to load the ONNX model in the background
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //removes action bar color
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);

        // Check if the user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sharedPreferences.contains("firstName") && sharedPreferences.contains("lastName")) {
            // Skip login if details are already saved
            navigateToMainActivity();
        }

        // Create a single thread ExecutorService to load the ONNX model in the background
        executorService = Executors.newSingleThreadExecutor();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);


                if (!firstName.isEmpty() && !lastName.isEmpty()) {
                    // Save data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("firstName", firstName);
                    editor.putString("lastName", lastName);
                    editor.apply();

                    // Start the background task of loading the ONNX model
//                    loadModelAndNavigateToMain();

                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);

                    // Navigate to MainActivity
                    navigateToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter both names", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadModelAndNavigateToMain() {
        // Start a background thread to load the model
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Load the model (this operation may take time)
                    loadModel();

                    // Once the model is loaded, move to MainActivity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);  // Hide progress bar
                            navigateToMainActivity();
                        }
                    });
                } catch (IOException | OrtException e) {
                    // Handle the error (e.g., show a Toast with the error message)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);  // Hide progress bar
                            Toast.makeText(LoginActivity.this, "Failed to load model", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    private void loadModel() throws IOException, OrtException {
        // Model loading logic
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("bert_model_trained.onnx");

        byte[] modelBytes = new byte[inputStream.available()];
        inputStream.read(modelBytes);
        inputStream.close();

        OrtEnvironment env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        OrtSession session = env.createSession(modelBytes, opts);

        Log.d("ONNX", "ONNX model loaded successfully!");
    }

        private void navigateToMainActivity() {
        progressBar.setVisibility(View.GONE);  // Hide progress bar
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close LoginActivity
    }
}
