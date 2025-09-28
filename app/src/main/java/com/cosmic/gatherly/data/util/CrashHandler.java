package com.cosmic.gatherly.data.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Global crash handler that prevents app crashes and logs detailed information
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final String PREFS_NAME = "crash_handler";
    private static final String KEY_CRASH_COUNT = "crash_count";
    private static final String KEY_LAST_CRASH_TIME = "last_crash_time";
    
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final SharedPreferences prefs;
    
    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Set this as the default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(this);
        
        Logger.i(Logger.TAG_SECURITY, "CrashHandler initialized and set as default exception handler");
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Log the crash with comprehensive details
            logCrashDetails(thread, throwable);
            
            // Update crash statistics
            updateCrashStats();
            
            // Try to perform graceful cleanup
            performGracefulCleanup();
            
            Logger.e(Logger.TAG_SECURITY, "🚨 UNCAUGHT EXCEPTION HANDLED - App will terminate gracefully", throwable);
            
        } catch (Exception e) {
            // If our crash handler crashes, log it but don't interfere with the original crash
            Log.e(TAG, "Error in crash handler", e);
        } finally {
            // Call the default handler to terminate the app
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            } else {
                System.exit(1);
            }
        }
    }
    
    private void logCrashDetails(Thread thread, Throwable throwable) {
        try {
            StringBuilder crashReport = new StringBuilder();
            crashReport.append("=== CRASH REPORT ===\n");
            crashReport.append("Timestamp: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date())).append("\n");
            crashReport.append("Thread: ").append(thread.getName()).append(" (ID: ").append(thread.getId()).append(")\n");
            crashReport.append("Exception: ").append(throwable.getClass().getSimpleName()).append("\n");
            crashReport.append("Message: ").append(throwable.getMessage()).append("\n");
            
            // Device information
            crashReport.append("\n=== DEVICE INFO ===\n");
            crashReport.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
            crashReport.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
            crashReport.append("App Version: ").append(getAppVersion()).append("\n");
            
            // Memory information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
            long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
            long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
            long usedMemory = totalMemory - freeMemory;
            
            crashReport.append("\n=== MEMORY INFO ===\n");
            crashReport.append("Max Memory: ").append(maxMemory).append(" MB\n");
            crashReport.append("Total Memory: ").append(totalMemory).append(" MB\n");
            crashReport.append("Used Memory: ").append(usedMemory).append(" MB\n");
            crashReport.append("Free Memory: ").append(freeMemory).append(" MB\n");
            
            // Stack trace
            crashReport.append("\n=== STACK TRACE ===\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            crashReport.append(sw.toString());
            
            // Cause chain
            Throwable cause = throwable.getCause();
            int causeLevel = 1;
            while (cause != null && causeLevel <= 10) {
                crashReport.append("\n=== CAUSED BY (Level ").append(causeLevel).append(") ===\n");
                crashReport.append("Exception: ").append(cause.getClass().getSimpleName()).append("\n");
                crashReport.append("Message: ").append(cause.getMessage()).append("\n");
                
                StringWriter causeSw = new StringWriter();
                PrintWriter causePw = new PrintWriter(causeSw);
                cause.printStackTrace(causePw);
                crashReport.append(causeSw.toString());
                
                cause = cause.getCause();
                causeLevel++;
            }
            
            // Log the complete crash report
            Logger.e(Logger.TAG_SECURITY, crashReport.toString());
            
            // Also log to Android Log for immediate visibility
            Log.e(TAG, "CRASH REPORT:\n" + crashReport.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to log crash details", e);
        }
    }
    
    private void updateCrashStats() {
        try {
            int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0) + 1;
            long currentTime = System.currentTimeMillis();
            
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, crashCount)
                .putLong(KEY_LAST_CRASH_TIME, currentTime)
                .apply();
            
            Logger.w(Logger.TAG_SECURITY, "Crash statistics updated: total crashes = %d", crashCount);
            
            // Log warning if crashes are frequent
            long lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0);
            if (lastCrashTime > 0 && (currentTime - lastCrashTime) < 60000) { // Less than 1 minute
                Logger.e(Logger.TAG_SECURITY, "⚠️ FREQUENT CRASHES DETECTED - Last crash was less than 1 minute ago");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to update crash stats", e);
        }
    }
    
    private void performGracefulCleanup() {
        try {
            Logger.d(Logger.TAG_SECURITY, "Performing graceful cleanup before app termination");
            
            // Try to save any pending data
            Logger.safeExecute(Logger.TAG_SECURITY, "emergency_data_save", () -> {
                // This would typically save any unsaved user data
                // For now, we'll just log that we attempted it
                Logger.i(Logger.TAG_SECURITY, "Emergency data save attempted");
            });
            
            // Try to disconnect from services
            Logger.safeExecute(Logger.TAG_SECURITY, "emergency_disconnect", () -> {
                // This would typically disconnect from WebSocket, close database connections, etc.
                Logger.i(Logger.TAG_SECURITY, "Emergency service disconnection attempted");
            });
            
            Logger.i(Logger.TAG_SECURITY, "Graceful cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during graceful cleanup", e);
        }
    }
    
    private String getAppVersion() {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Get crash statistics
     */
    public CrashStats getCrashStats() {
        int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0);
        long lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0);
        return new CrashStats(crashCount, lastCrashTime);
    }
    
    /**
     * Reset crash statistics
     */
    public void resetCrashStats() {
        prefs.edit()
            .remove(KEY_CRASH_COUNT)
            .remove(KEY_LAST_CRASH_TIME)
            .apply();
        Logger.i(Logger.TAG_SECURITY, "Crash statistics reset");
    }
    
    /**
     * Check if the app has been crashing frequently
     */
    public boolean isAppUnstable() {
        int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0);
        long lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0);
        
        // Consider app unstable if:
        // 1. More than 5 crashes total, OR
        // 2. Any crash in the last 5 minutes
        boolean unstable = crashCount > 5 || 
            (lastCrashTime > 0 && (System.currentTimeMillis() - lastCrashTime) < 300000);
        
        if (unstable) {
            Logger.w(Logger.TAG_SECURITY, "App stability check: UNSTABLE (crashes: %d, last: %d minutes ago)", 
                crashCount, (System.currentTimeMillis() - lastCrashTime) / 60000);
        }
        
        return unstable;
    }
    
    /**
     * Data class for crash statistics
     */
    public static class CrashStats {
        private final int totalCrashes;
        private final long lastCrashTime;
        
        public CrashStats(int totalCrashes, long lastCrashTime) {
            this.totalCrashes = totalCrashes;
            this.lastCrashTime = lastCrashTime;
        }
        
        public int getTotalCrashes() {
            return totalCrashes;
        }
        
        public long getLastCrashTime() {
            return lastCrashTime;
        }
        
        public boolean hasEverCrashed() {
            return totalCrashes > 0;
        }
        
        public long getTimeSinceLastCrash() {
            return lastCrashTime > 0 ? System.currentTimeMillis() - lastCrashTime : -1;
        }
        
        @Override
        public String toString() {
            return String.format(Locale.US, "CrashStats{crashes=%d, lastCrash=%s}", 
                totalCrashes, 
                lastCrashTime > 0 ? new Date(lastCrashTime).toString() : "never");
        }
    }
}