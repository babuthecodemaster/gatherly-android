package com.cosmic.gatherly.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.cosmic.gatherly.R;

/**
 * Dialog to show file upload progress
 */
public class FileUploadProgressDialog extends Dialog {
    private TextView fileNameText;
    private TextView fileSizeText;
    private TextView progressText;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button cancelButton;
    private Button retryButton;
    
    private String fileName = "";
    private String fileSize = "";
    private int progress = 0;
    private String status = "";
    private boolean isError = false;
    private OnCancelListener cancelListener;
    private OnRetryListener retryListener;
    
    public interface OnCancelListener {
        void onCancel();
    }
    
    public interface OnRetryListener {
        void onRetry();
    }
    
    public FileUploadProgressDialog(@NonNull Context context) {
        super(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_file_upload_progress);
        
        initializeViews();
        setupClickListeners();
        updateUI();
        
        setCancelable(false); // Prevent dismissing by touching outside
    }
    
    private void initializeViews() {
        fileNameText = findViewById(R.id.fileNameText);
        fileSizeText = findViewById(R.id.fileSizeText);
        progressText = findViewById(R.id.progressText);
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        cancelButton = findViewById(R.id.cancelButton);
        retryButton = findViewById(R.id.retryButton);
    }
    
    private void setupClickListeners() {
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onCancel();
                }
                dismiss();
            });
        }
        
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                if (retryListener != null) {
                    retryListener.onRetry();
                }
                // Reset to uploading state
                showUploading();
            });
        }
    }
    
    private void updateUI() {
        if (fileNameText != null) {
            fileNameText.setText(fileName);
        }
        if (fileSizeText != null) {
            fileSizeText.setText(fileSize);
        }
        if (progressText != null) {
            progressText.setText(progress + "%");
        }
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (fileNameText != null) {
            fileNameText.setText(fileName);
        }
    }
    
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
        if (fileSizeText != null) {
            fileSizeText.setText(fileSize);
        }
    }
    
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        if (progressText != null) {
            progressText.setText(this.progress + "%");
        }
        if (progressBar != null) {
            progressBar.setProgress(this.progress);
        }
    }
    
    public void setOnCancelListener(OnCancelListener listener) {
        this.cancelListener = listener;
    }
    
    public void setStatus(String status) {
        this.status = status;
        if (statusText != null) {
            statusText.setText(status);
        }
    }
    
    public void showError(String errorMessage) {
        isError = true;
        setStatus(errorMessage);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
        }
        
        if (cancelButton != null) {
            cancelButton.setText("Close");
        }
    }
    
    public void showUploading() {
        isError = false;
        setStatus("Uploading...");
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }
        
        if (cancelButton != null) {
            cancelButton.setText("Cancel");
        }
    }
    
    public void showRetrying(int attemptNumber) {
        setStatus("Retrying upload... (Attempt " + attemptNumber + ")");
        showUploading();
    }
    
    public void setOnRetryListener(OnRetryListener listener) {
        this.retryListener = listener;
    }
}
