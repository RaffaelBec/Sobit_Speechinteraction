package com.example.javaspeechrecognizer;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;
import org.vosk.android.SpeechService;
import android.speech.RecognitionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private TextToSpeech t1;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private SpeechService speechService;
    private static final int REQUEST_MICROPHONE = 1;
    private SpeechRecognizer speechRecognizer;




    private TextView textViewGreeting;
    private Button buttonBackToLogin;
    private OrtEnvironment env;
    private OrtSession session;
    private EditText editTextInput;  // EditText field for user input
    public static ActivityResultLauncher<Intent> speechLauncher;

    private final BroadcastReceiver hotwordReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "Hotword detected");

            if (intent.getAction().equals("com.example.javaspeechrecognizer.HOTWORD_DETECTED")) {

                speechLauncher = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                List<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                                if (matches != null && !matches.isEmpty()) {
                                    String recognizedText = matches.get(0);
//                                    Toast.makeText(this, "You said: " + recognizedText, Toast.LENGTH_SHORT).show();
                                    processInputText(recognizedText);
                                }
                            }
                        }
                );
                Log.d("MainActivity", "Hotword detected");
                if (isAppInForeground()) {
                    startSpeechInput("Speak now", speechLauncher);
                } else {
                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startSpeechInput("Speak now", speechLauncher);
                    }, 500); // Adjust the delay as needed
                }
            }
        }
    };
    String[] labelNames = {
            "O",
            "B-Action",
            "I-Action",
            "B-Entity",
            "I-Entity",
            "B-Person",
            "I-Person",
            "B-Task",
            "I-Task",
            "B-Time",
            "I-Time",
            "B-Distance",
            "B-Question",
            "I-Question"
    };

    // Create the id2label map
    static Map<Integer, String> id2label = new HashMap<>();


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.GERMAN);
                }
            }
            });
        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        List<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            Toast.makeText(this, "You said: " + recognizedText, Toast.LENGTH_SHORT).show();
                            processInputText(recognizedText);
                        }
                    }
                }
        );

        for(int i = 0; i < labelNames.length; i++) {
            id2label.put(i, labelNames[i]);
        }





        //removes action bar color
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



//


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Hide the status bar but keep the navigation bar visible
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the status bar and navigation bar
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        // Hide the navigation bar but keep the status bar visible
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hides the navigation bar
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE; // Keeps layout stable
//        decorView.setSystemUiVisibility(uiOptions);




        // Initialize the ONNX environment and load the model (old)
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("bert_model_trained.onnx");

            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            inputStream.close();
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession(modelBytes, opts);
            Log.d("ONNX", "ONNX model loaded successfully!");
            createNotificationChannel();

//
//
////TODO: check if this works
//            OnnxModelManager.init(this);
//              Log.d("ONNX", "ONNX model loaded successfully!");
//            // Später kannst du das Modell wie folgt abrufen:
//            session = OnnxModelManager.getSession();
//





        } catch (Exception e) {
            Log.e("ONNX", "Failed to load ONNX model", e);
        }


        final ActivityResultLauncher<Intent> speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        List<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            Toast.makeText(this, "You said: " + recognizedText, Toast.LENGTH_SHORT).show();
                            processInputText(recognizedText);
                        }
                    }
                }
        );

        // no need for speechbutoon in the new version
//        Button speechButton = findViewById(R.id.btn_speech);
//        speechButton.setOnClickListener(v -> {
//            if (hasPermissionToMicrophone()) {
//                if (isSpeechAvailable()) {
//                    startSpeechInput("Speak now", speechLauncher);
//                } else {
//                    Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                requestMicrophonePermission();
//            }
//        });
        requestMicrophonePermission();
        Log.d("Notification channel", "Starting notification channel");

        //displaying info from login
        textViewGreeting = findViewById(R.id.textViewGreeting);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "User");
        String lastName = sharedPreferences.getString("lastName", "");

        textViewGreeting.setText(getString(R.string.welcome_message, firstName, lastName));

        // Button to clear user data
        Button buttonClearData = findViewById(R.id.buttonClearData);
        buttonClearData.setOnClickListener(v -> {
            clearUserData();
        });

        // Disable the Back To Login button initially
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);
        buttonBackToLogin.setEnabled(false); // Makes the button unclickable
        buttonBackToLogin.setClickable(false); // Prevents touch events -> Not sure if this does anything
        buttonBackToLogin.setAlpha(0.7f); // Grays out the button (use somewhere between 0.5f and 0.9f)

        // Button to navigate to LoginActivity
        buttonBackToLogin.setOnClickListener(v -> {
            navigateToLogin();
        });


        // Help/Settings Button
        Button helpButton = findViewById(R.id.btn_help);
        helpButton.setOnClickListener(v -> {
            // Launch HelpActivity when clicked
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });
        IntentFilter filter = new IntentFilter("com.example.javaspeechrecognizer.HOTWORD_DETECTED");
        ContextCompat.registerReceiver(this,hotwordReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
        Button buttonClearResults = findViewById(R.id.buttonClearResults);
        buttonClearResults.setOnClickListener(v -> clearResults());

        // Request RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

        }
        else {
            Log.d("Hotword Service", "Starting hotword service");
            startHotwordService();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        // Register the BroadcastReceiver



    }


    // Method to clear user data from SharedPreferences
    private void clearUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Removes all data from SharedPreferences
        editor.apply();  // Apply changes

        Toast.makeText(this, "User data cleared", Toast.LENGTH_SHORT).show();

        // Optionally, update UI or perform actions after clearing
        textViewGreeting.setText(getString(R.string.user_data_cleared));
        // Enable the Logout button after clearing data
        buttonBackToLogin.setEnabled(true); // Makes the button clickable
        buttonBackToLogin.setClickable(true); // Enables touch events again -> Not sure if this does anything
        buttonBackToLogin.setAlpha(1.0f); // Restores the button's original color
    }

    // Method to navigate back to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Close MainActivity
    }

    public boolean isSpeechAvailable() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return !activities.isEmpty();
    }

    public void startSpeechInput(String title, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, title);
        speechRecognizer.startListening(intent);
        //launcher.launch(intent);
    }

    public boolean hasPermissionToMicrophone() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    //private void requestMicrophonePermission() {
    //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
    //}

    public void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): Check for both permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            }
        } else {
            // For older Android versions, only check RECORD_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHotwordService();
            } else {
                Toast.makeText(this, "Microphone or Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void processInputText(String inputText){

        /* what raffael and maxim wrote on 25.11.2024 (didnt work - wrong)
       // BertTokenizer tokenizer;
//        HuggingFaceTokenizer tokenizer;
//        try {
//            Path tokenizerpath = Paths.get("C:\\Users\\helle\\Desktop\\Prototype_SANER\\app\\src\\main\\assets\\tokenizer\\tokenizer.json");
//            //tokenizer = new BertTokenizer("assets/vocab.txt"); // Replace with your vocab file path
//            tokenizer = HuggingFaceTokenizer.builder().optTokenizerPath(tokenizerpath).build();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        Encoding tokenizedInput = tokenizer.encode(inputText.toLowerCase());
//        long[] inputIds1DArray = tokenizedInput.getIds();

*/

        /* what maxim used for the old model before 25.11.2024 (but some parts have been deleted)
//        List<Integer> tokenizedInput = tokenizer.decode(inputText.toLowerCase());
//
//        Log.d("ONNX input test",tokenizedInput.toString());
//        tokenizer.addSpecialTokens(tokenizedInput);
//        List<Integer> tokensWithSpecial = tokenizer.addSpecialTokens(tokenizedInput);
//
//        long[] inputIds1DArray = new long[tokensWithSpecial.size()];
//        for (int i = 0; i < tokensWithSpecial.size(); i++) {
//            inputIds1DArray[i] = tokensWithSpecial.get(i); // Convert Integer to long
//        } */

        //new and should work (but does not)
        BertTokenizer tokenizer;
        try {
            tokenizer = new BertTokenizer("assets/vocab.txt"); // Replace with your vocab file path

        } catch (IOException e) {
            Log.e("MainActivity", "Failed to initialize tokenizer", e);
            throw new RuntimeException(e);
        }
        List<Integer> tokenizedInput = tokenizer.tokenize(inputText);
        tokenizer.addSpecialTokens(tokenizedInput);
        List<Integer> tokensWithSpecial = tokenizer.addSpecialTokens(tokenizedInput);

        long[] inputIds1DArray = new long[tokensWithSpecial.size()];
        for (int i = 0; i < tokensWithSpecial.size(); i++) {
            inputIds1DArray[i] = tokensWithSpecial.get(i); // Convert Integer to long
        }

// Now, wrap this array inside a 2D array, as required by the ONNX model
        long[][] inputIdsArray = {inputIds1DArray}; // This gives you long[][]

        // Perform ONNX model inference when the button is clicked
        //long[][] inputIdsArray = {{3,4111,26897,86,6426,2200,4}}; // Replace with actual data
        long[][] attentionMaskArray = generateAttentionMask(inputIdsArray); // Replace with actual data



        List<int[]> result = performOnnxInference(inputIdsArray, attentionMaskArray);
        List<List<String>> labeledResult = idToLabel(result);

        if (result != null && !result.isEmpty()) {
            for (int[] array : result) {
                List<Integer> singleSequence = new ArrayList<>();
                for (int id : array) {
                   singleSequence.add(id);
                }

                List<String> decodedStrings = tokenizer.decodeToStrings(singleSequence);
                Log.d("ONNX Inference", "Decoded Sequence: " + decodedStrings);
            }
            // Handle the result
            //Log.d("ONNX Inference", "Input: " + tokenizedInput);


            List<String> modifiedLabeledResult = null; //has to be declared outside of if for some reason
            //because otherwise |String logMessage_textViewLabeledResult2 = TextUtils.join(" ", modifiedLabeledResult);| does not work

            // Modify the labeledResult to remove the first and last strings
            if (!labeledResult.isEmpty()) {
                modifiedLabeledResult = new ArrayList<>(labeledResult.get(0));

                // Remove first and last elements
                if (modifiedLabeledResult.size() > 1) {
                    modifiedLabeledResult.remove(0); // Remove first element
                    modifiedLabeledResult.remove(modifiedLabeledResult.size() - 1); // Remove last element
                }

                // Update labeledResult with modified data
                labeledResult.set(0, modifiedLabeledResult);
            }

            TextView textViewTokenizedInputHeading = findViewById(R.id.textViewTokenizedInputHeading);
            TextView textViewLabeledResultHeading = findViewById(R.id.textViewLabeledResultHeading);

            // Find the TextView
            TextView textViewTokenizedInput = findViewById(R.id.textViewTokenizedInput);
            TextView textViewLabeledResult = findViewById(R.id.textViewLabeledResult);

            // Old Log message (with bracets)
            String logMessage_textViewTokenizedInput = "" + tokenizer.decodeToStrings(tokenizedInput);
            String logMessage_textViewLabeledResult = "" + labeledResult;

            // Convert the tokenized input list to a string (remove brackets)
            String logMessage_textViewTokenizedInput2 = TextUtils.join(" ", tokenizer.decodeToStrings(tokenizedInput));

            // Flatten the modifiedLabeledResult list and join the elements with spaces (remove brackets)
            String logMessage_textViewLabeledResult2 = TextUtils.join(" ", modifiedLabeledResult);


            Log.d("ONNX Inference", "Output: " + result);
//            Log.d("ONNX Inference", "Output: " + tokenizer.decodeToStrings(tokenizedInput));
//            Log.d("ONNX Inference", "Output: " + labeledResult);

            // Show in Logcat (for developers) (with bracets)
            Log.d("ONNX Inference", logMessage_textViewTokenizedInput);
            Log.d("ONNX Inference", logMessage_textViewLabeledResult);


            textViewTokenizedInputHeading.setVisibility(View.VISIBLE);
            textViewLabeledResultHeading.setVisibility(View.VISIBLE);
            textViewTokenizedInput.setVisibility(View.VISIBLE);
            textViewLabeledResult.setVisibility(View.VISIBLE);


            //NEW
            SQLiteHelper dbHelper = new SQLiteHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT ActualTimeStart FROM Assignment " +
                            "JOIN Client ON Assignment.ClientId = Client.ClientId " +
                            "WHERE Client.FirstName = ? AND Client.LastName = ? " +
                            "ORDER BY ActualTimeStart DESC LIMIT 1",
                    new String[]{"John", "Doe"}
            );
            String lastAppointment = "Nothing to show";
            if (cursor.moveToFirst()) {
                lastAppointment = cursor.getString(0);
                System.out.println("Last Appointment: " + lastAppointment);
                t1.speak(lastAppointment, TextToSpeech.QUEUE_FLUSH, null, null);
                Log.d("SQL", "Last Appointment: " + lastAppointment);
            }
            cursor.close();
            db.close();
            // Display to the user
            textViewTokenizedInput.setText(logMessage_textViewTokenizedInput2);
            //this was used before modifiedLabeledResult was added to code but it still works now (but it used to be logMessage_textViewLabeledResult)
            textViewLabeledResult.setText(logMessage_textViewLabeledResult2);
            textViewLabeledResult.setText(lastAppointment);

            //i though i needed this line after creating the modified labeled result
            //this probably only outputs the two labels that were removed
            //textViewLabeledResult.setText(modifiedLabeledResult.toString());
        }
    }

    // Method to perform ONNX model inference
    List<int[]> performOnnxInference(long[][] inputIdsArray, long[][] attentionMaskArray) {
        try {
            // Create ONNX tensors for input_ids and attention_mask
            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, inputIdsArray);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, attentionMaskArray);

            // Create a Map to pass both input tensors
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            // Run the session to get the output
            OrtSession.Result result = session.run(inputs);

            if(result == null || result.get("output").isEmpty()) {
                Log.e("MainActivity", "Failed to perform Onnx Inference");
                return null;
            } else {
                result.get("output").get();
            }
            // Retrieve the output using the output name
            float[][][] output = (float[][][]) result.get("output").get().getValue();

            // Clean up resources
            inputIdsTensor.close();
            attentionMaskTensor.close();
            result.close();
            List<int[]> predictedLabels = getPredictedLabels(output);
            for (int[] sequencePredictions : predictedLabels) {
                System.out.println("Predicted labels: " + Arrays.toString(sequencePredictions));
            }
            return predictedLabels;

        } catch (OrtException e) {
            Log.e("MainActivity", "Failed to perform Onnx Inference", e);
            throw new RuntimeException(e);
        }
    }

    public float[] softmax(float[] logits) {
        float[] expScores = new float[logits.length];
        float sum = 0.0f;

        // Compute the exponentials and sum
        for (int i = 0; i < logits.length; i++) {
            expScores[i] = (float) Math.exp(logits[i]);
            sum += expScores[i];
        }

        // Divide by the sum to get probabilities
        for (int i = 0; i < expScores.length; i++) {
            expScores[i] /= sum;
        }

        return expScores;
    }

    public List<int[]> getPredictedLabels(float[][][] logits) {
        List<int[]> predictedLabels = new ArrayList<>();

        // Iterate through each sequence of logits
        for (float[][] tokenLogits : logits) {
            int[] tokenPredictions = new int[tokenLogits.length];

            // Apply softmax and get the class with the highest probability (argmax)
            for (int i = 0; i < tokenLogits.length; i++) {
                float[] probabilities = softmax(tokenLogits[i]);
                int predictedClass = argmax(probabilities);
                tokenPredictions[i] = predictedClass;
            }

            predictedLabels.add(tokenPredictions);
        }

        return predictedLabels;
    }

    // Helper function to find the index of the maximum value (argmax)
    public int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static long[][] generateAttentionMask(long[][] inputIdsArray) {
        // Assuming that padding tokens are zero, and non-padding tokens are greater than 0.
        long[][] attentionMask = new long[inputIdsArray.length][];

        // Loop over each example in the batch
        for (int i = 0; i < inputIdsArray.length; i++) {
            long[] inputIds = inputIdsArray[i];
            long[] attentionMaskForExample = new long[inputIds.length];

            // Generate attention mask: '1' for non-padding tokens, '0' for padding tokens
            for (int j = 0; j < inputIds.length; j++) {
                // Check if the token is non-padding (assumes 0 is padding, adjust if necessary)
                attentionMaskForExample[j] = (inputIds[j] > 0) ? 1 : 0;
            }

            // Set the attention mask for the current example
            attentionMask[i] = attentionMaskForExample;
        }
        System.out.println(Arrays.deepToString(attentionMask));
        return attentionMask;
    }

    public static List<List<String>> idToLabel(List<int[]> intList){
        List<List<String>> labelList = new ArrayList<>();
        for (int[] array : intList) {
            List<String> labels = new ArrayList<>();
            for (int id : array) {
                labels.add(id2label.getOrDefault(id, "UNKNOWN")); // Use "UNKNOWN" for missing ids
            }
            labelList.add(labels);
        }
        return labelList;
    }

    //this method hides the text views of the results
    private void clearResults() {
        // Find all the TextViews
        TextView textViewTokenizedInputHeading = findViewById(R.id.textViewTokenizedInputHeading);
        TextView textViewLabeledResultHeading = findViewById(R.id.textViewLabeledResultHeading);
        TextView textViewTokenizedInput = findViewById(R.id.textViewTokenizedInput);
        TextView textViewLabeledResult = findViewById(R.id.textViewLabeledResult);

        // Hide all TextViews
        textViewTokenizedInputHeading.setVisibility(View.GONE);
        textViewLabeledResultHeading.setVisibility(View.GONE);
        textViewTokenizedInput.setVisibility(View.GONE);
        textViewLabeledResult.setVisibility(View.GONE);
    }

    /*
    private void initVosk() {
        new Thread(() -> {
            try {
                String modelName = "vosk-model-small-de-0.15";
                File modelDir = new File(getExternalFilesDir(null), modelName);

                if (!modelDir.exists() || modelDir.list().length == 0) {
                    Log.d("Vosk", "Copying model files...");
                    copyAssets(modelName, modelName);
                }

                Model model = new Model(modelDir.getAbsolutePath());
                Recognizer recognizer = new Recognizer(model, 16000);
                speechService = new SpeechService(recognizer, 16000);
                speechService.startListening(this);

                Log.d("Vosk", "Vosk initialized successfully");
            } catch (IOException e) {
                Log.e("Vosk", "Failed to initialize Vosk", e);
            }
        }).start();
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
                // If it's a directory, create it and recurse
                outFile.mkdirs();
                copyAssets(assetPath, outputFolder + "/" + filename);
            } else {
                // If it's a file, copy it
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

     */

    private void startHotwordService() {
        Intent serviceIntent = new Intent(this, HotwordService.class);
        startForegroundService(serviceIntent);
        showPopupDialog();
    }

    private void showPopupDialog() {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
                .setTitle("All Set!")
                .setMessage("You can close this app now and let Platon do the magic. Enjoy!")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }



    // Method to check if the app is in the foreground
    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
        }
        unregisterReceiver(hotwordReceiver);

    }

    @Override
    public void onResults(Bundle results) {
        // Called when recognition results are ready
        List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String recognizedText = matches.get(0);
            Toast.makeText(this, "You said: " + recognizedText, Toast.LENGTH_SHORT).show();
            showNotification(recognizedText);
            processInputText(recognizedText);
        }
    }



    @Override
    public void onReadyForSpeech(Bundle params) {
        // Called when the recognizer is ready to listen
    }

    @Override
    public void onBeginningOfSpeech() {
        // Called when the user starts speaking
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Called when the sound level in the audio stream has changed
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Called when more sound has been received
    }

    @Override
    public void onEndOfSpeech() {
        // Called when the user stops speaking
    }

    @Override
    public void onError(int error) {
        // Called when a recognition error occurs
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // Called when partial recognition results are available
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // Called when a recognition event occurs
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "SpeechRecognitionChannel",
                "Speech Recognition",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String recognizedText) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "SpeechRecognitionChannel";
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Speech Recognized")
                .setContentText("You said: " + recognizedText)
                .setSmallIcon(R.drawable.splash_logo) // Replace with your icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        notificationManager.notify(1, notification);
    }


}
