package com.cosmic.gatherly.data.manager;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cosmic.gatherly.data.model.VoiceChannelState;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages voice channel connections and state
 */
public class VoiceChannelManager {
    private static final String TAG = "VoiceChannelManager";
    private static VoiceChannelManager instance;
    
    private Context context;
    private Map<String, VoiceChannelState> voiceChannelStates;
    private MutableLiveData<VoiceChannelState> currentVoiceChannelState;
    private String currentVoiceChannelId;

    private VoiceChannelManager(Context context) {
        this.context = context.getApplicationContext();
        this.voiceChannelStates = new HashMap<>();
        this.currentVoiceChannelState = new MutableLiveData<>();
    }

    public static synchronized VoiceChannelManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceChannelManager(context);
        }
        return instance;
    }

    /**
     * Connect to a voice channel
     */
    public void connectToVoiceChannel(String channelId, String channelName) {
        Log.d(TAG, "Connecting to voice channel: " + channelName + " (ID: " + channelId + ")");
        
        try {
            // Disconnect from current channel if connected
            if (currentVoiceChannelId != null && !currentVoiceChannelId.equals(channelId)) {
                disconnectFromVoiceChannel();
            }

            // Create or get voice channel state
            VoiceChannelState state = voiceChannelStates.get(channelId);
            if (state == null) {
                state = new VoiceChannelState(channelId);
                voiceChannelStates.put(channelId, state);
            }

            // Update connection state
            state.setConnected(true);
            state.setConnectionQuality(VoiceChannelState.ConnectionQuality.GOOD);
            currentVoiceChannelId = channelId;

            // Notify observers
            currentVoiceChannelState.setValue(state);
            
            Log.i(TAG, "Successfully connected to voice channel: " + channelName);
            
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to voice channel: " + channelName, e);
            
            // Create error state
            VoiceChannelState errorState = new VoiceChannelState(channelId);
            errorState.setConnected(false);
            errorState.setConnectionQuality(VoiceChannelState.ConnectionQuality.DISCONNECTED);
            currentVoiceChannelState.setValue(errorState);
        }
    }

    /**
     * Disconnect from current voice channel
     */
    public void disconnectFromVoiceChannel() {
        if (currentVoiceChannelId == null) {
            Log.d(TAG, "No voice channel to disconnect from");
            return;
        }

        Log.d(TAG, "Disconnecting from voice channel: " + currentVoiceChannelId);
        
        try {
            VoiceChannelState state = voiceChannelStates.get(currentVoiceChannelId);
            if (state != null) {
                state.setConnected(false);
                state.setConnectionQuality(VoiceChannelState.ConnectionQuality.DISCONNECTED);
                state.setMuted(false);
                state.setDeafened(false);
                state.setSpeaking(false);
                
                // Clear connected users (in a real implementation, this would be handled by the voice service)
                state.getConnectedUsers().clear();
            }

            currentVoiceChannelId = null;
            currentVoiceChannelState.setValue(null);
            
            Log.i(TAG, "Successfully disconnected from voice channel");
            
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting from voice channel", e);
        }
    }

    /**
     * Toggle mute state
     */
    public void toggleMute() {
        if (currentVoiceChannelId == null) {
            Log.w(TAG, "Cannot toggle mute - not connected to voice channel");
            return;
        }

        VoiceChannelState state = voiceChannelStates.get(currentVoiceChannelId);
        if (state != null && state.isConnected()) {
            boolean newMuteState = !state.isMuted();
            state.setMuted(newMuteState);
            currentVoiceChannelState.setValue(state);
            
            Log.d(TAG, "Toggled mute: " + (newMuteState ? "muted" : "unmuted"));
        }
    }

    /**
     * Toggle deafen state
     */
    public void toggleDeafen() {
        if (currentVoiceChannelId == null) {
            Log.w(TAG, "Cannot toggle deafen - not connected to voice channel");
            return;
        }

        VoiceChannelState state = voiceChannelStates.get(currentVoiceChannelId);
        if (state != null && state.isConnected()) {
            boolean newDeafenState = !state.isDeafened();
            state.setDeafened(newDeafenState);
            
            // If deafening, also mute
            if (newDeafenState) {
                state.setMuted(true);
            }
            
            currentVoiceChannelState.setValue(state);
            
            Log.d(TAG, "Toggled deafen: " + (newDeafenState ? "deafened" : "undeafened"));
        }
    }

    /**
     * Check if currently connected to a voice channel
     */
    public boolean isConnectedToVoiceChannel() {
        return currentVoiceChannelId != null && 
               voiceChannelStates.containsKey(currentVoiceChannelId) &&
               voiceChannelStates.get(currentVoiceChannelId).isConnected();
    }

    /**
     * Get current voice channel ID
     */
    public String getCurrentVoiceChannelId() {
        return currentVoiceChannelId;
    }

    /**
     * Get current voice channel state
     */
    public VoiceChannelState getCurrentVoiceChannelState() {
        if (currentVoiceChannelId != null) {
            return voiceChannelStates.get(currentVoiceChannelId);
        }
        return null;
    }

    /**
     * Get live data for current voice channel state
     */
    public LiveData<VoiceChannelState> getCurrentVoiceChannelStateLiveData() {
        return currentVoiceChannelState;
    }

    /**
     * Get voice channel state for a specific channel
     */
    public VoiceChannelState getVoiceChannelState(String channelId) {
        return voiceChannelStates.get(channelId);
    }

    /**
     * Check if a specific channel is the current voice channel
     */
    public boolean isCurrentVoiceChannel(String channelId) {
        return channelId != null && channelId.equals(currentVoiceChannelId);
    }
}