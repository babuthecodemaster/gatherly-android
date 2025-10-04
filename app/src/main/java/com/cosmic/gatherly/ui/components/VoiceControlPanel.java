package com.cosmic.gatherly.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.VoiceChannelManager;
import com.cosmic.gatherly.data.model.VoiceChannelState;

/**
 * Voice control panel component with mute, deafen, and disconnect buttons
 */
public class VoiceControlPanel extends LinearLayout {
    
    private ImageButton btnMute;
    private ImageButton btnDeafen;
    private ImageButton btnDisconnect;
    private View connectionQualityIndicator;
    
    private VoiceChannelManager voiceChannelManager;
    private VoiceChannelState currentState;
    
    public VoiceControlPanel(Context context) {
        super(context);
        init();
    }
    
    public VoiceControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public VoiceControlPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.voice_control_panel, this, true);
        
        btnMute = findViewById(R.id.btnMute);
        btnDeafen = findViewById(R.id.btnDeafen);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        connectionQualityIndicator = findViewById(R.id.connectionQualityIndicator);
        
        setupClickListeners();
        
        // Initially hidden until connected to voice channel
        setVisibility(GONE);
    }
    
    private void setupClickListeners() {
        btnMute.setOnClickListener(v -> {
            if (voiceChannelManager != null) {
                voiceChannelManager.toggleMute();
            }
        });
        
        btnDeafen.setOnClickListener(v -> {
            if (voiceChannelManager != null) {
                voiceChannelManager.toggleDeafen();
            }
        });
        
        btnDisconnect.setOnClickListener(v -> {
            if (voiceChannelManager != null) {
                voiceChannelManager.disconnectFromVoiceChannel();
            }
        });
    }
    
    /**
     * Initialize the voice control panel with VoiceChannelManager
     */
    public void initialize(VoiceChannelManager voiceChannelManager, LifecycleOwner lifecycleOwner) {
        this.voiceChannelManager = voiceChannelManager;
        
        // Observe voice channel state changes
        voiceChannelManager.getCurrentVoiceChannelStateLiveData().observe(lifecycleOwner, new Observer<VoiceChannelState>() {
            @Override
            public void onChanged(VoiceChannelState voiceChannelState) {
                updateUI(voiceChannelState);
            }
        });
    }
    
    /**
     * Update UI based on voice channel state
     */
    private void updateUI(VoiceChannelState state) {
        currentState = state;
        
        if (state == null || !state.isConnected()) {
            // Hide panel when not connected
            setVisibility(GONE);
            return;
        }
        
        // Show panel when connected
        setVisibility(VISIBLE);
        
        // Update mute button
        updateMuteButton(state.isMuted());
        
        // Update deafen button
        updateDeafenButton(state.isDeafened());
        
        // Update connection quality indicator
        updateConnectionQuality(state.getConnectionQuality());
    }
    
    /**
     * Update mute button appearance and functionality
     */
    private void updateMuteButton(boolean isMuted) {
        if (isMuted) {
            btnMute.setImageResource(R.drawable.ic_mic_off);
            btnMute.setSelected(true);
            btnMute.setContentDescription(getContext().getString(R.string.unmute_microphone));
        } else {
            btnMute.setImageResource(R.drawable.ic_mic);
            btnMute.setSelected(false);
            btnMute.setContentDescription(getContext().getString(R.string.mute_microphone));
        }
    }
    
    /**
     * Update deafen button appearance and functionality
     */
    private void updateDeafenButton(boolean isDeafened) {
        if (isDeafened) {
            btnDeafen.setImageResource(R.drawable.ic_headphones_off);
            btnDeafen.setSelected(true);
            btnDeafen.setContentDescription(getContext().getString(R.string.undeafen_audio));
        } else {
            btnDeafen.setImageResource(R.drawable.ic_headphones);
            btnDeafen.setSelected(false);
            btnDeafen.setContentDescription(getContext().getString(R.string.deafen_audio));
        }
    }
    
    /**
     * Update connection quality indicator
     */
    private void updateConnectionQuality(VoiceChannelState.ConnectionQuality quality) {
        switch (quality) {
            case EXCELLENT:
            case GOOD:
                connectionQualityIndicator.setSelected(true);
                connectionQualityIndicator.setActivated(false);
                break;
            case POOR:
                connectionQualityIndicator.setSelected(false);
                connectionQualityIndicator.setActivated(true);
                break;
            case DISCONNECTED:
            default:
                connectionQualityIndicator.setSelected(false);
                connectionQualityIndicator.setActivated(false);
                break;
        }
    }
    
    /**
     * Get current voice channel state
     */
    public VoiceChannelState getCurrentState() {
        return currentState;
    }
    
    /**
     * Check if currently connected to voice channel
     */
    public boolean isConnected() {
        return currentState != null && currentState.isConnected();
    }
}