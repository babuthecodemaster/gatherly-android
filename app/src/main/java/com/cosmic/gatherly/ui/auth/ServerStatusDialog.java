package com.cosmic.gatherly.ui.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.data.util.Logger;

/**
 * Dialog to show server connectivity status and diagnostics
 */
public class ServerStatusDialog extends Dialog {
    private static final String TAG = "ServerStatusDialog";
    
    private TextView statusText;
    private TextView diagnosticsText;
    private ProgressBar progressBar;
    private Button checkButton;
    private Button closeButton;
    
    private AuthRepository authRepository;
    
    public ServerStatusDialog(Context context, AuthRepository authRepository) {
        super(context);
        this.authRepository = authRepository;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_server_status);
        
        initViews();
        setupClickListeners();
        
        // Automatically check server status when dialog opens
        checkServerStatus();
    }
    
    private void initViews() {
        statusText = findViewById(R.id.tv_server_status);
        diagnosticsText = findViewById(R.id.tv_diagnostics);
        progressBar = findViewById(R.id.progress_bar);
        checkButton = findViewById(R.id.btn_check_server);
        closeButton = findViewById(R.id.btn_close);
        
        setTitle("Server Status");
    }
    
    private void setupClickListeners() {
        checkButton.setOnClickListener(v -> checkServerStatus());
        closeButton.setOnClickListener(v -> dismiss());
    }
    
    private void checkServerStatus() {
        Logger.d(Logger.TAG_UI, "Checking server status from dialog");
        
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        checkButton.setEnabled(false);
        statusText.setText("Checking server connectivity...");
        diagnosticsText.setText("");
        
        // Check server connectivity
        authRepository.checkServerConnectivity((isConnected, message) -> {
            // Update status
            statusText.setText(isConnected ? "✅ Server Connected" : "❌ Server Disconnected");
            statusText.append("\n" + message);
            
            // Get detailed diagnostics
            authRepository.getServerDiagnostics(diagnostics -> {
                // Hide loading state
                progressBar.setVisibility(View.GONE);
                checkButton.setEnabled(true);
                
                // Show diagnostics
                diagnosticsText.setText(diagnostics);
                
                Logger.d(Logger.TAG_UI, "Server status check completed: %s", isConnected ? "Connected" : "Disconnected");
            });
        });
    }
}