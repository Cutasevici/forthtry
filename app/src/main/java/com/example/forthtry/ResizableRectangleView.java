package com.example.forthtry;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.core.Rect;

public class ResizableRectangleView extends View {
    private float initialTouchX, initialTouchY;
    private int initialWidth, initialHeight;
    private static final int MIN_SIZE = 100;  // Minimum size of the rectangle
    private int touchZoneSize = 70; // Size of the edge touch zone for resizing

    public ResizableRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Capture initial points and dimensions
                initialTouchX = x;
                initialTouchY = y;
                initialWidth = getWidth();
                initialHeight = getHeight();
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - initialTouchX;
                float deltaY = y - initialTouchY;
                ViewGroup.LayoutParams params = getLayoutParams();

                // Determine if touch is near the edge for vertical or horizontal resizing
                if (Math.abs(x - initialTouchX) > Math.abs(y - initialTouchY)) {
                    // Horizontal movement: update width
                    if (x > getWidth() - touchZoneSize || x < touchZoneSize) {
                        params.width = Math.max(MIN_SIZE, (int) (initialWidth + deltaX));
                    }
                } else {
                    // Vertical movement: update height
                    if (y > getHeight() - touchZoneSize || y < touchZoneSize) {
                        params.height = Math.max(MIN_SIZE, (int) (initialHeight + deltaY));
                    }
                }

                setLayoutParams(params);
                return true;
        }
        return super.onTouchEvent(event);
    }
    public Rect getRectangleCoordinates() {
        // Get the top-left corner of the view (x, y) and the current size (width, height)
        int x = getLeft();
        int y = getTop();
        int width = getWidth();
        int height = getHeight();

        // Return the OpenCV Rect object with the current rectangle's position and size
        return new Rect(x, y, width, height);
    }
}
