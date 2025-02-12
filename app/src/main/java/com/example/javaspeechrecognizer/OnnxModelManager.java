package com.example.javaspeechrecognizer;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.SessionOptions;

public class OnnxModelManager {

    private static OrtEnvironment ortEnvironment;
    private static OrtSession ortSession;
    private static final String MODEL_FILE_NAME = "dein-model.onnx";


    public static synchronized void init(Context context) {
        if (ortSession != null) {
            // Modell wurde bereits initialisiert
            return;
        }

        try {
            File modelFile = new File(context.getFilesDir(), MODEL_FILE_NAME);
            if (!modelFile.exists()) {
                try (InputStream inputStream = context.getAssets().open(MODEL_FILE_NAME);
                     FileOutputStream outputStream = new FileOutputStream(modelFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }

            ortEnvironment = OrtEnvironment.getEnvironment();
            ortSession = ortEnvironment.createSession(modelFile.getAbsolutePath(), new SessionOptions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static OrtSession getSession() {
        if (ortSession == null) {
            throw new IllegalStateException("Modell wurde noch nicht initialisiert. Bitte rufe OnnxModelManager.init(context) auf.");
        }
        return ortSession;
    }
}
