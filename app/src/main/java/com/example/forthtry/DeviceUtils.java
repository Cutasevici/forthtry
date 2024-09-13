package com.example.forthtry;

import android.content.res.Configuration;

public class DeviceUtils {
    public static boolean isTablet(MainActivity activity) {
        return (activity.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
