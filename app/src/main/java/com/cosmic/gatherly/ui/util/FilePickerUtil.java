package com.cosmic.gatherly.ui.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.cosmic.gatherly.data.util.Logger;

public class FilePickerUtil {
    private static final String TAG = "FilePickerUtil";
    
    public interface FilePickerCallback {
        void onFileSelected(Uri fileUri);
        void onError(String error);
        void onCancelled();
    }
    
    /**
     * Create an activity result launcher for file picking in an Activity
     */
    public static ActivityResultLauncher<String[]> createFilePickerLauncher(
            Activity activity, FilePickerCallback callback) {
        
        if (!(activity instanceof androidx.activity.ComponentActivity)) {
            throw new IllegalArgumentException("Activity must extend ComponentActivity");
        }
        
        androidx.activity.ComponentActivity componentActivity = 
            (androidx.activity.ComponentActivity) activity;
            
        return componentActivity.registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    Logger.d(TAG, "File selected: " + uri.toString());
                    callback.onFileSelected(uri);
                } else {
                    Logger.d(TAG, "File selection cancelled");
                    callback.onCancelled();
                }
            }
        );
    }
    
    /**
     * Create an activity result launcher for file picking in a Fragment
     */
    public static ActivityResultLauncher<String[]> createFilePickerLauncher(
            Fragment fragment, FilePickerCallback callback) {
            
        return fragment.registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    Logger.d(TAG, "File selected: " + uri.toString());
                    callback.onFileSelected(uri);
                } else {
                    Logger.d(TAG, "File selection cancelled");
                    callback.onCancelled();
                }
            }
        );
    }
    
    /**
     * Launch file picker with common file types
     */
    public static void pickFile(ActivityResultLauncher<String[]> launcher) {
        String[] mimeTypes = {
            "image/*",
            "video/*",
            "audio/*",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        
        try {
            launcher.launch(mimeTypes);
        } catch (Exception e) {
            Logger.e(TAG, "Error launching file picker", e);
        }
    }
    
    /**
     * Launch file picker for images only
     */
    public static void pickImage(ActivityResultLauncher<String[]> launcher) {
        String[] mimeTypes = {"image/*"};
        
        try {
            launcher.launch(mimeTypes);
        } catch (Exception e) {
            Logger.e(TAG, "Error launching image picker", e);
        }
    }
    
    /**
     * Launch file picker for documents only
     */
    public static void pickDocument(ActivityResultLauncher<String[]> launcher) {
        String[] mimeTypes = {
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        
        try {
            launcher.launch(mimeTypes);
        } catch (Exception e) {
            Logger.e(TAG, "Error launching document picker", e);
        }
    }
    
    /**
     * Create a legacy file picker intent (fallback method)
     */
    public static Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        String[] mimeTypes = {
            "image/*",
            "video/*",
            "audio/*",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        return Intent.createChooser(intent, "Select File");
    }
}