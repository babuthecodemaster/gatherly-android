package com.cosmic.gatherly;

import android.app.Application;
import android.util.Log;

/**
 * Minimal Application class - No complex initialization, no crashes
 */
public class MinimalApplication extends Application {
    private static final String TAG = "MinimalApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MinimalApplication started - no complex initialization");
    }
}