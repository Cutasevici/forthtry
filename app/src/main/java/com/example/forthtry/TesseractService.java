package com.example.forthtry;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractService {

    private static final String TAG = "TesseractService";
    private static final String DATA_PATH = "tesseract/";
    private static final String LANG = "eng";  // You can change this to any other language code you use
    private TessBaseAPI tessBaseAPI;
    private final Context context;

    public TesseractService(Context context) {
        this.context = context;
        initializeTesseract(); // Initialize when the service is first created
    }

    /**
     * Initializes Tesseract by copying the trained data to the internal storage
     * and setting up the API with the correct language model.
     */
    public void initializeTesseract() {
        if (tessBaseAPI != null) {
            Log.d(TAG, "Tesseract is already initialized.");
            return;  // Tesseract is already initialized, no need to re-initialize
        }

        tessBaseAPI = new TessBaseAPI();

        try {
            // Define the path to the tessdata folder in internal storage
            String tessDataPath = context.getFilesDir() + "/" + DATA_PATH + "tessdata/";

            // Check if the tessdata directory exists, if not, create it
            File dir = new File(tessDataPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Copy the trained data file (eng.traineddata) from assets to internal storage
            String trainedDataFilePath = tessDataPath + LANG + ".traineddata";
            File trainedDataFile = new File(trainedDataFilePath);
            if (!trainedDataFile.exists()) {
                copyTrainedDataToInternalStorage(trainedDataFilePath);
            }

            // Initialize Tesseract with the trained data
            if (tessBaseAPI.init(context.getFilesDir() + "/" + DATA_PATH, LANG)) {
                Log.d(TAG, "Tesseract initialized successfully.");
            } else {
                Log.e(TAG, "Tesseract failed to initialize.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Tesseract: " + e.getMessage(), e);
        }
    }

    /**
     * Copies the Tesseract trained data file from the assets folder to the internal storage.
     *
     * @param destinationPath The destination path in the internal storage.
     */
    private void copyTrainedDataToInternalStorage(String destinationPath) {
        try {
            InputStream in = context.getAssets().open("tessdata/" + LANG + ".traineddata");
            OutputStream out = new FileOutputStream(destinationPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();
            Log.d(TAG, "Trained data file copied to internal storage.");
        } catch (Exception e) {
            Log.e(TAG, "Error copying trained data file: " + e.getMessage(), e);
        }
    }

    /**
     * Perform OCR on a given bitmap and return the extracted text.
     *
     * @param bitmap The image bitmap to perform OCR on.
     * @return The extracted text from the image, or null if an error occurs.
     */
    public String performOCR(Bitmap bitmap) {
        String extractedText = null;

        try {
            // Check if the bitmap is null before processing
            if (bitmap == null) {
                Log.e(TAG, "Bitmap is null, cannot perform OCR.");
                return null;
            }

            tessBaseAPI.setImage(bitmap);

            // Set the character whitelist to letters, numbers, and symbols (you can modify this as needed)
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-.");

            // Perform OCR and get the extracted text
            extractedText = tessBaseAPI.getUTF8Text();

            // Check if extracted text is null or empty
            if (extractedText == null || extractedText.isEmpty()) {
                Log.e(TAG, "No text found in the image.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during OCR: " + e.getMessage(), e);
        }

        return extractedText;
    }

    /**
     * Clean up the Tesseract API resources and prepare to reinitialize.
     * This should only be called when the app is terminating.
     */
    public void cleanUp() {
        if (tessBaseAPI != null) {
            tessBaseAPI.end();  // Release all memory used by Tesseract
            tessBaseAPI = null;  // Set the object to null so it's properly recreated next time
            Log.d(TAG, "Tesseract API resources cleaned up.");
        }
    }
}
