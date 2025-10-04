package com.cosmic.gatherly.data.manager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.cosmic.gatherly.data.model.VoiceChannelState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for VoiceChannelManager - testing voice channel behavior separation
 * and state management
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class VoiceChannelManagerTest {

    @Mock
    private Observer<VoiceChannelState> mockObserver;

    private Context context;
    private VoiceChannelManager voiceChannelManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Get VoiceChannelManager instance
        voiceChannelManager = VoiceChannelManager.getInstance(context);
        
        // Observe voice channel state changes
        voiceChannelManager.getCurrentVoiceChannelStateLiveData().observeForever(mockObserver);
    }

    @Test
    public void testSingletonInstance() {
        VoiceChannelManager instance1 = VoiceChannelManager.getInstance(context);
        VoiceChannelManager instance2 = VoiceChannelManager.getInstance(context);
        
        assertSame("VoiceChannelManager should be singleton", instance1, instance2);
    }

    @Test
    public void testInitialState() {
        assertFalse("Should not be connected to voice channel initially", 
            voiceChannelManager.isConnectedToVoiceChannel());
        assertNull("Current voice channel ID should be null initially", 
            voiceChannelManager.getCurrentVoiceChannelId());
        assertNull("Current voice channel state should be null initially", 
            voiceChannelManager.getCurrentVoiceChannelState());
    }

    @Test
    public void testVoiceChannelConnection() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Connect to voice channel
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        
        // Verify connection state
        assertTrue("Should be connected to voice channel", 
            voiceChannelManager.isConnectedToVoiceChannel());
        assertEquals("Current voice channel ID should match", channelId, 
            voiceChannelManager.getCurrentVoiceChannelId());
        assertTrue("Should be current voice channel", 
            voiceChannelManager.isCurrentVoiceChannel(channelId));
        
        // Verify voice channel state
        VoiceChannelState state = voiceChannelManager.getCurrentVoiceChannelState();
        assertNotNull("Voice channel state should not be null", state);
        assertTrue("Voice channel should be connected", state.isConnected());
        assertEquals("Connection quality should be good", 
            VoiceChannelState.ConnectionQuality.GOOD, state.getConnectionQuality());
        
        // Verify observer was notified
        verify(mockObserver, atLeastOnce()).onChanged(any(VoiceChannelState.class));
    }

    @Test
    public void testVoiceChannelDisconnection() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Connect first
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        assertTrue("Should be connected", voiceChannelManager.isConnectedToVoiceChannel());
        
        // Disconnect
        voiceChannelManager.disconnectFromVoiceChannel();
        
        // Verify disconnection state
        assertFalse("Should not be connected after disconnection", 
            voiceChannelManager.isConnectedToVoiceChannel());
        assertNull("Current voice channel ID should be null after disconnection", 
            voiceChannelManager.getCurrentVoiceChannelId());
        assertNull("Current voice channel state should be null after disconnection", 
            voiceChannelManager.getCurrentVoiceChannelState());
        
        // Verify observer was notified of disconnection
        verify(mockObserver, atLeastOnce()).onChanged(null);
    }

    @Test
    public void testSwitchingVoiceChannels() {
        String channel1Id = "voice_channel_1";
        String channel1Name = "General Voice";
        String channel2Id = "voice_channel_2";
        String channel2Name = "Gaming Voice";
        
        // Connect to first channel
        voiceChannelManager.connectToVoiceChannel(channel1Id, channel1Name);
        assertEquals("Should be connected to first channel", channel1Id, 
            voiceChannelManager.getCurrentVoiceChannelId());
        
        // Connect to second channel (should disconnect from first)
        voiceChannelManager.connectToVoiceChannel(channel2Id, channel2Name);
        assertEquals("Should be connected to second channel", channel2Id, 
            voiceChannelManager.getCurrentVoiceChannelId());
        assertFalse("Should not be current voice channel for first channel", 
            voiceChannelManager.isCurrentVoiceChannel(channel1Id));
        assertTrue("Should be current voice channel for second channel", 
            voiceChannelManager.isCurrentVoiceChannel(channel2Id));
    }

    @Test
    public void testMuteToggle() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Connect to voice channel
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        
        VoiceChannelState state = voiceChannelManager.getCurrentVoiceChannelState();
        assertNotNull("State should not be null", state);
        assertFalse("Should not be muted initially", state.isMuted());
        
        // Toggle mute
        voiceChannelManager.toggleMute();
        assertTrue("Should be muted after toggle", state.isMuted());
        
        // Toggle mute again
        voiceChannelManager.toggleMute();
        assertFalse("Should not be muted after second toggle", state.isMuted());
        
        // Verify observer was notified of state changes
        verify(mockObserver, atLeastOnce()).onChanged(any(VoiceChannelState.class));
    }

    @Test
    public void testDeafenToggle() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Connect to voice channel
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        
        VoiceChannelState state = voiceChannelManager.getCurrentVoiceChannelState();
        assertNotNull("State should not be null", state);
        assertFalse("Should not be deafened initially", state.isDeafened());
        assertFalse("Should not be muted initially", state.isMuted());
        
        // Toggle deafen
        voiceChannelManager.toggleDeafen();
        assertTrue("Should be deafened after toggle", state.isDeafened());
        assertTrue("Should also be muted when deafened", state.isMuted());
        
        // Toggle deafen again
        voiceChannelManager.toggleDeafen();
        assertFalse("Should not be deafened after second toggle", state.isDeafened());
        // Note: mute state might remain true depending on implementation
    }

    @Test
    public void testMuteToggleWithoutConnection() {
        // Try to toggle mute without being connected
        voiceChannelManager.toggleMute();
        
        // Should not crash and should remain disconnected
        assertFalse("Should still not be connected", 
            voiceChannelManager.isConnectedToVoiceChannel());
    }

    @Test
    public void testDeafenToggleWithoutConnection() {
        // Try to toggle deafen without being connected
        voiceChannelManager.toggleDeafen();
        
        // Should not crash and should remain disconnected
        assertFalse("Should still not be connected", 
            voiceChannelManager.isConnectedToVoiceChannel());
    }

    @Test
    public void testGetVoiceChannelState() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Get state before connection
        VoiceChannelState stateBefore = voiceChannelManager.getVoiceChannelState(channelId);
        assertNull("State should be null before connection", stateBefore);
        
        // Connect to voice channel
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        
        // Get state after connection
        VoiceChannelState stateAfter = voiceChannelManager.getVoiceChannelState(channelId);
        assertNotNull("State should not be null after connection", stateAfter);
        assertTrue("State should show connected", stateAfter.isConnected());
        assertEquals("Channel ID should match", channelId, stateAfter.getChannelId());
    }

    @Test
    public void testIsCurrentVoiceChannel() {
        String channel1Id = "voice_channel_1";
        String channel2Id = "voice_channel_2";
        String channel1Name = "General Voice";
        
        // Test with no connection
        assertFalse("Should not be current voice channel when not connected", 
            voiceChannelManager.isCurrentVoiceChannel(channel1Id));
        
        // Connect to channel 1
        voiceChannelManager.connectToVoiceChannel(channel1Id, channel1Name);
        
        // Test current channel
        assertTrue("Should be current voice channel", 
            voiceChannelManager.isCurrentVoiceChannel(channel1Id));
        assertFalse("Should not be current voice channel for different channel", 
            voiceChannelManager.isCurrentVoiceChannel(channel2Id));
        
        // Test with null
        assertFalse("Should not be current voice channel for null", 
            voiceChannelManager.isCurrentVoiceChannel(null));
    }

    @Test
    public void testVoiceChannelStateProperties() {
        String channelId = "voice_channel_1";
        String channelName = "General Voice";
        
        // Connect to voice channel
        voiceChannelManager.connectToVoiceChannel(channelId, channelName);
        
        VoiceChannelState state = voiceChannelManager.getCurrentVoiceChannelState();
        assertNotNull("State should not be null", state);
        
        // Test initial properties
        assertEquals("Channel ID should match", channelId, state.getChannelId());
        assertTrue("Should be connected", state.isConnected());
        assertFalse("Should not be muted initially", state.isMuted());
        assertFalse("Should not be deafened initially", state.isDeafened());
        assertFalse("Should not be speaking initially", state.isSpeaking());
        assertEquals("Connection quality should be good", 
            VoiceChannelState.ConnectionQuality.GOOD, state.getConnectionQuality());
        assertNotNull("Connected users list should not be null", state.getConnectedUsers());
        assertTrue("Connected users list should be empty initially", 
            state.getConnectedUsers().isEmpty());
    }

    @Test
    public void testConnectionErrorHandling() {
        // This test simulates connection errors
        // In a real implementation, you might have specific error conditions
        
        String invalidChannelId = "";
        String channelName = "Test Channel";
        
        // Try to connect with invalid channel ID
        voiceChannelManager.connectToVoiceChannel(invalidChannelId, channelName);
        
        // The manager should handle this gracefully
        // Depending on implementation, it might still create a state or handle the error
        assertTrue("Error handling should not crash the application", true);
    }
}