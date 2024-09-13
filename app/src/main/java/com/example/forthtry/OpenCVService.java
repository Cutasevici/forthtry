package com.example.forthtry;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import android.graphics.Bitmap;
public class OpenCVService {

    // Method to extract the region within the resizable rectangle and convert to grayscale
    public Mat extractAndConvertToGrayscale(Mat originalImage, Rect rectangle) {
        // Crop the original image to the area defined by the rectangle
        Mat croppedImage = new Mat(originalImage, rectangle);

        // Convert the cropped image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(croppedImage, grayImage, Imgproc.COLOR_RGB2GRAY);

        // Release the cropped image as we only need the grayscale image
        croppedImage.release();

        return grayImage; // Return the grayscale image in Mat format
    }

    // Method to convert OpenCV Mat to Bitmap for Tesseract usage
    public Bitmap matToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    // Call this method after Tesseract finishes to ensure memory is cleaned up
    public void releaseMat(Mat mat) {
        mat.release();
    }
}
