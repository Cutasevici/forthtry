package com.example.forthtry;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TesseractService tesseractService;
    private CameraModule cameraModule;
    private PreviewView cameraPreview;
    private ResizableRectangleView resizableRectangleView;
    private OpenCVService openCVService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Step 1: Initialize OpenCV before doing anything else
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed!");
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "OpenCV initialized successfully.");
        }

        // Initialize camera, OpenCV, and Tesseract services
        cameraPreview = findViewById(R.id.camera_preview);
        cameraModule = new CameraModule(this, cameraPreview);
        resizableRectangleView = findViewById(R.id.scan_area);
        openCVService = new OpenCVService();
        tesseractService = new TesseractService(this);

        // Set screen orientation based on the device type (tablet or phone)
        if (DeviceUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // Set up the scan button click listener
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(v -> onScanButtonClicked());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraModule.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraModule != null) {
            cameraModule.releaseCamera();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraModule != null) {
            cameraModule.releaseCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraModule != null) {
            cameraModule.initializeCamera(); // Ensure the camera is initialized correctly
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tesseractService != null) {
            tesseractService.cleanUp();
        }
    }

    // Handles the "Scan" button click to perform image processing and OCR
    private void onScanButtonClicked() {
        // Step 1: Capture the image from the camera preview (requires CameraModule to implement capture)
        Mat cameraImage = cameraModule.captureImage();

        if (cameraImage != null) {
            // Step 2: Get the rectangle coordinates from ResizableRectangleView
            Rect rectangle = resizableRectangleView.getRectangleCoordinates();

            // Step 3: Use OpenCV to crop and convert the captured image to grayscale
            Mat grayImage = openCVService.extractAndConvertToGrayscale(cameraImage, rectangle);

            // Step 4: Convert the processed image (grayscale) to a Bitmap for Tesseract OCR
            Bitmap bitmap = openCVService.matToBitmap(grayImage);

            // Step 5: Perform OCR using the TesseractService
            String extractedText = tesseractService.performOCR(bitmap);

            // Step 6: Release the Mat object to free up memory
            openCVService.releaseMat(grayImage);

            // Step 7: Handle and display the extracted text
            handleExtractedText(extractedText);
        } else {
            Toast.makeText(this, "Failed to capture image from the camera.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handles the display or processing of the extracted text
    private void handleExtractedText(String extractedText) {
        // For now, we simply show a Toast with the extracted text. You can modify this
        // to display the text in a TextView or store it in a database.
        if (extractedText != null && !extractedText.isEmpty()) {
            Toast.makeText(this, "Extracted Text: " + extractedText, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No text found in the image.", Toast.LENGTH_SHORT).show();
        }
    }
}
