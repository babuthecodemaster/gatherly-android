package com.cosmic.gatherly;

import android.app.Application;

public class GatherlyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize any global configurations here
        // For example, you could initialize analytics, crash reporting, etc.
    }
}