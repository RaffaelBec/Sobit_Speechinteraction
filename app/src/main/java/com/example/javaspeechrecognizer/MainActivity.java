package com.example.javaspeechrecognizer;

import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//additional imports
import android.content.Context;
import android.speech.SpeechRecognizer;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_MICROPHONE = 1;


    private TextView textViewGreeting;
    private Button buttonBackToLogin;
    private OrtEnvironment env;
    private OrtSession session;
    private EditText editTextInput;  // EditText field for user input

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        for(int i = 0; i < labelNames.length; i++) {
            id2label.put(i, labelNames[i]);
        }
        super.onCreate(savedInstanceState);

        //removes action bar color
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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



        } catch (IOException | OrtException e) {
            e.printStackTrace();
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

        Button speechButton = findViewById(R.id.btn_speech);
        speechButton.setOnClickListener(v -> {
            if (hasPermissionToMicrophone()) {
                if (isSpeechAvailable()) {
                    startSpeechInput("Speak now", speechLauncher);
                } else {
                    Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestMicrophonePermission();
            }
        });

        //displaying info from login
        textViewGreeting = findViewById(R.id.textViewGreeting);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "User");
        String lastName = sharedPreferences.getString("lastName", "");

        textViewGreeting.setText("Welcome, " + firstName + " " + lastName + "!");


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

        Button buttonClearResults = findViewById(R.id.buttonClearResults);
        buttonClearResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResults();
            }
        });
    }

    // Method to clear user data from SharedPreferences
    private void clearUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Removes all data from SharedPreferences
        editor.apply();  // Apply changes

        Toast.makeText(this, "User data cleared", Toast.LENGTH_SHORT).show();

        // Optionally, update UI or perform actions after clearing
        textViewGreeting.setText("User data cleared. Please log in.");

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
        launcher.launch(intent);
    }

    public boolean hasPermissionToMicrophone() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    //private void requestMicrophonePermission() {
    //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
    //}

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void processInputText(String inputText){

        //what raffael and maxim wrote on 25.11.2024 (didnt work - wrong)
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



        //what maxim used for the old model before 25.11.2024 (but some parts have been deleted)
//        List<Integer> tokenizedInput = tokenizer.decode(inputText.toLowerCase());
//
//        Log.d("ONNX input test",tokenizedInput.toString());
//        tokenizer.addSpecialTokens(tokenizedInput);
//        List<Integer> tokensWithSpecial = tokenizer.addSpecialTokens(tokenizedInput);
//
//        long[] inputIds1DArray = new long[tokensWithSpecial.size()];
//        for (int i = 0; i < tokensWithSpecial.size(); i++) {
//            inputIds1DArray[i] = tokensWithSpecial.get(i); // Convert Integer to long
//        }


        //new and should work (but does not)
        BertTokenizer tokenizer;
        try {
            tokenizer = new BertTokenizer("assets/vocab.txt"); // Replace with your vocab file path

        } catch (IOException e) {
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

        if (result != null) {
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

            // Display to the user
            textViewTokenizedInput.setText(logMessage_textViewTokenizedInput2);
            //this was used before modifiedLabeledResult was added to code but it still works now (but it used to be logMessage_textViewLabeledResult)
            textViewLabeledResult.setText(logMessage_textViewLabeledResult2);

            //i though i needed this line after creating the modified labeled result
            //this probably only outputs the two labels that were removed
            //textViewLabeledResult.setText(modifiedLabeledResult.toString());
        }
    }

    // Method to perform ONNX model inference
    private List<int[]> performOnnxInference(long[][] inputIdsArray, long[][] attentionMaskArray) {
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
            e.printStackTrace();
            return null;
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

}