package com.example.forthtry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.core.Mat;

import java.util.concurrent.ExecutionException;

public class CameraModule {

    private final PreviewView cameraPreview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private final MainActivity mainActivity;

    public CameraModule(MainActivity mainActivity, PreviewView cameraPreview) {
        this.mainActivity = mainActivity;
        this.cameraPreview = cameraPreview;
        checkCameraPermissionAndSetup();
    }

    /**
     * Checks for camera permission and sets up the camera if granted.
     */
    private void checkCameraPermissionAndSetup() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setUpCamera();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Request the camera permission.
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    /**
     * Handles permission result for the camera.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUpCamera();
        } else {
            Toast.makeText(mainActivity, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up the camera by binding it to the lifecycle of the activity.
     */
    private void setUpCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(mainActivity);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraModule", "Error setting up camera", e);
            }
        }, ContextCompat.getMainExecutor(mainActivity));
    }

    /**
     * Binds the camera preview to the lifecycle and sets up the preview view.
     */
    private void bindCameraPreview(ProcessCameraProvider cameraProvider) {
        try {
            Log.d("CameraModule", "Starting camera preview binding");

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder()
                    .setTargetRotation(cameraPreview.getDisplay().getRotation())
                    .build();

            preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            cameraProvider.bindToLifecycle((LifecycleOwner) mainActivity, cameraSelector, preview);

            Log.d("CameraModule", "Camera preview bound successfully");
        } catch (Exception e) {
            Log.e("CameraModule", "Failed to bind camera preview", e);
        }
    }

    /**
     * Releases the camera by unbinding all use cases.
     */
    public void releaseCamera() {
        try {
            if (cameraProviderFuture != null && cameraProviderFuture.isDone()) {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("CameraModule", "Failed to release camera", e);
        }
    }

    public void initializeCamera() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setUpCamera();
        } else {
            Log.d("CameraModule", "Camera permission not granted. Cannot initialize.");
        }
    }
    public Mat captureImage() {
        // Capture the current frame from the camera as a Mat
        // (Implementation will depend on your camera setup and whether you're capturing real-time frames)
        // You may need to handle converting the PreviewView into a Bitmap and then into Mat
        Bitmap bitmap = cameraPreview.getBitmap(); // Example method (depends on your camera preview library)
        Mat mat = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        return mat;
    }
}
