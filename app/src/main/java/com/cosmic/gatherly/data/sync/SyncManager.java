package com.cosmic.gatherly.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String PERIODIC_SYNC_WORK = "periodic_sync_work";
    private static final String IMMEDIATE_SYNC_WORK = "immediate_sync_work";
    
    private static SyncManager instance;
    private WorkManager workManager;
    
    private SyncManager(Context context) {
        workManager = WorkManager.getInstance(context.getApplicationContext());
    }
    
    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }
    
    public void startPeriodicSync() {
        Log.d(TAG, "Starting periodic sync");
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        
        PeriodicWorkRequest periodicSyncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, // Repeat every 15 minutes
                TimeUnit.MINUTES
        )
        .setConstraints(constraints)
        .addTag(PERIODIC_SYNC_WORK)
        .build();
        
        workManager.enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
        );
        
        Log.d(TAG, "Periodic sync scheduled");
    }
    
    public void stopPeriodicSync() {
        Log.d(TAG, "Stopping periodic sync");
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK);
    }
    
    public void syncNow() {
        Log.d(TAG, "Starting immediate sync");
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest immediateSyncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .addTag(IMMEDIATE_SYNC_WORK)
                .build();
        
        workManager.enqueue(immediateSyncRequest);
    }
    
    public void cancelAllSync() {
        Log.d(TAG, "Cancelling all sync work");
        workManager.cancelAllWorkByTag(PERIODIC_SYNC_WORK);
        workManager.cancelAllWorkByTag(IMMEDIATE_SYNC_WORK);
    }
}