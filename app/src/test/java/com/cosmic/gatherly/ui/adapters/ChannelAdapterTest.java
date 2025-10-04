package com.cosmic.gatherly.ui.adapters;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ApplicationProvider;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.VoiceChannelManager;
import com.cosmic.gatherly.ui.adapters.ChannelAdapter.ChannelItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ChannelAdapter - testing channel selection state management
 * and voice channel behavior separation
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ChannelAdapterTest {

    @Mock
    private ChannelAdapter.OnChannelClickListener mockListener;
    
    @Mock
    private VoiceChannelManager mockVoiceChannelManager;

    private ChannelAdapter adapter;
    private List<ChannelItem> testChannels;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Create test channel data
        testChannels = createTestChannelData();
        
        // Initialize adapter
        adapter = new ChannelAdapter(testChannels, mockListener, mockVoiceChannelManager);
    }

    private List<ChannelItem> createTestChannelData() {
        List<ChannelItem> channels = new ArrayList<>();
        
        // Add header
        channels.add(new ChannelItem("TEXT CHANNELS", true));
        
        // Add text channels
        channels.add(new ChannelItem("text1", "general", R.drawable.ic_hashtag, ChannelItem.ChannelType.TEXT));
        channels.add(new ChannelItem("text2", "random", R.drawable.ic_hashtag, ChannelItem.ChannelType.TEXT));
        channels.add(new ChannelItem("text3", "announcements", R.drawable.ic_hashtag, ChannelItem.ChannelType.TEXT));
        
        // Add voice header
        channels.add(new ChannelItem("VOICE CHANNELS", true));
        
        // Add voice channels
        channels.add(new ChannelItem("voice1", "General Voice", R.drawable.ic_volume_up, ChannelItem.ChannelType.VOICE));
        channels.add(new ChannelItem("voice2", "Gaming Voice", R.drawable.ic_volume_up, ChannelItem.ChannelType.VOICE));
        
        return channels;
    }

    @Test
    public void testChannelSelectionStateManagement() {
        // Test initial state - no channels selected
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && channel.getType() == ChannelItem.ChannelType.TEXT) {
                assertFalse("Text channel should not be selected initially", channel.isSelected());
            }
        }

        // Test selecting a text channel
        adapter.setSelectedTextChannel("general");
        
        // Verify only the selected channel is marked as selected
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && channel.getType() == ChannelItem.ChannelType.TEXT) {
                if ("general".equals(channel.getName())) {
                    assertTrue("Selected text channel should be marked as selected", channel.isSelected());
                } else {
                    assertFalse("Non-selected text channels should not be selected", channel.isSelected());
                }
            }
        }

        // Test switching to another text channel
        adapter.setSelectedTextChannel("random");
        
        // Verify selection switched correctly
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && channel.getType() == ChannelItem.ChannelType.TEXT) {
                if ("random".equals(channel.getName())) {
                    assertTrue("Newly selected text channel should be marked as selected", channel.isSelected());
                } else {
                    assertFalse("Previously selected channel should no longer be selected", channel.isSelected());
                }
            }
        }
    }

    @Test
    public void testVoiceChannelBehaviorSeparation() {
        // Setup mock voice channel manager
        when(mockVoiceChannelManager.getCurrentVoiceChannelId()).thenReturn("voice1");
        when(mockVoiceChannelManager.isCurrentVoiceChannel("voice1")).thenReturn(true);
        when(mockVoiceChannelManager.isCurrentVoiceChannel("voice2")).thenReturn(false);

        // Test voice channel state update
        adapter.updateVoiceChannelStates();

        // Verify voice channels have correct connection state
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && channel.getType() == ChannelItem.ChannelType.VOICE) {
                if ("voice1".equals(channel.getId())) {
                    assertTrue("Connected voice channel should show as connected", channel.isVoiceConnected());
                } else {
                    assertFalse("Non-connected voice channels should not show as connected", channel.isVoiceConnected());
                }
            }
        }

        // Test that voice channel selection doesn't affect text channel selection
        adapter.setSelectedTextChannel("general");
        adapter.updateVoiceChannelStates();

        // Verify text channel selection is preserved
        ChannelItem generalChannel = findChannelByName("general");
        assertNotNull("General channel should exist", generalChannel);
        assertTrue("Text channel selection should be preserved", generalChannel.isSelected());

        // Verify voice channel connection is preserved
        ChannelItem voiceChannel = findChannelById("voice1");
        assertNotNull("Voice channel should exist", voiceChannel);
        assertTrue("Voice channel connection should be preserved", voiceChannel.isVoiceConnected());
    }

    @Test
    public void testChannelClickHandlers() {
        // Create a mock ViewGroup for testing ViewHolder creation
        ViewGroup mockParent = mock(ViewGroup.class);
        LayoutInflater mockInflater = mock(LayoutInflater.class);
        View mockView = mock(View.class);
        
        when(mockParent.getContext()).thenReturn(context);
        when(LayoutInflater.from(context)).thenReturn(mockInflater);
        when(mockInflater.inflate(anyInt(), eq(mockParent), eq(false))).thenReturn(mockView);

        // Test text channel click
        ChannelItem textChannel = findChannelByName("general");
        assertNotNull("Text channel should exist", textChannel);

        // Simulate text channel click through adapter
        adapter.getOnChannelClickListener().onTextChannelClick(textChannel);
        
        // Verify text channel click listener was called
        verify(mockListener, times(1)).onTextChannelClick(textChannel);
        verify(mockListener, never()).onVoiceChannelClick(any());

        // Test voice channel click
        ChannelItem voiceChannel = findChannelById("voice1");
        assertNotNull("Voice channel should exist", voiceChannel);

        // Simulate voice channel click through adapter
        adapter.getOnChannelClickListener().onVoiceChannelClick(voiceChannel);
        
        // Verify voice channel click listener was called
        verify(mockListener, times(1)).onVoiceChannelClick(voiceChannel);
        verify(mockListener, times(1)).onTextChannelClick(any()); // From previous test
    }

    @Test
    public void testAdapterBasicFunctionality() {
        // Test item count
        assertEquals("Adapter should return correct item count", testChannels.size(), adapter.getItemCount());

        // Test view types
        assertEquals("Header should return header view type", 0, adapter.getItemViewType(0)); // First item is header
        assertEquals("Channel should return channel view type", 1, adapter.getItemViewType(1)); // Second item is channel
    }

    @Test
    public void testVoiceChannelStateUpdates() {
        // Test initial state
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && channel.getType() == ChannelItem.ChannelType.VOICE) {
                assertFalse("Voice channels should not be connected initially", channel.isVoiceConnected());
            }
        }

        // Mock voice channel manager to return different states
        when(mockVoiceChannelManager.getCurrentVoiceChannelId()).thenReturn("voice2");
        when(mockVoiceChannelManager.isCurrentVoiceChannel("voice1")).thenReturn(false);
        when(mockVoiceChannelManager.isCurrentVoiceChannel("voice2")).thenReturn(true);

        // Update voice channel states
        adapter.updateVoiceChannelStates();

        // Verify states updated correctly
        ChannelItem voice1 = findChannelById("voice1");
        ChannelItem voice2 = findChannelById("voice2");
        
        assertNotNull("Voice channel 1 should exist", voice1);
        assertNotNull("Voice channel 2 should exist", voice2);
        
        assertFalse("Voice channel 1 should not be connected", voice1.isVoiceConnected());
        assertTrue("Voice channel 2 should be connected", voice2.isVoiceConnected());
    }

    @Test
    public void testChannelItemProperties() {
        ChannelItem textChannel = new ChannelItem("test_id", "test_name", R.drawable.ic_hashtag, ChannelItem.ChannelType.TEXT);
        
        // Test initial properties
        assertEquals("Channel ID should match", "test_id", textChannel.getId());
        assertEquals("Channel name should match", "test_name", textChannel.getName());
        assertEquals("Channel icon should match", R.drawable.ic_hashtag, textChannel.getIconResource());
        assertEquals("Channel type should match", ChannelItem.ChannelType.TEXT, textChannel.getType());
        assertFalse("Channel should not be header", textChannel.isHeader());
        assertFalse("Channel should not be selected initially", textChannel.isSelected());
        assertFalse("Channel should not be voice connected initially", textChannel.isVoiceConnected());

        // Test setters
        textChannel.setSelected(true);
        assertTrue("Channel should be selected after setting", textChannel.isSelected());

        textChannel.setVoiceConnected(true);
        assertTrue("Channel should be voice connected after setting", textChannel.isVoiceConnected());
    }

    // Helper methods
    private ChannelItem findChannelByName(String name) {
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && name.equals(channel.getName())) {
                return channel;
            }
        }
        return null;
    }

    private ChannelItem findChannelById(String id) {
        for (ChannelItem channel : testChannels) {
            if (!channel.isHeader() && id.equals(channel.getId())) {
                return channel;
            }
        }
        return null;
    }

    // Mock interface to access protected methods for testing
    private interface TestChannelAdapter {
        ChannelAdapter.OnChannelClickListener getOnChannelClickListener();
    }

    // Create test adapter that exposes protected methods
    private class TestableChannelAdapter extends ChannelAdapter implements TestChannelAdapter {
        public TestableChannelAdapter(List<ChannelItem> items, OnChannelClickListener listener, VoiceChannelManager voiceChannelManager) {
            super(items, listener, voiceChannelManager);
        }

        @Override
        public OnChannelClickListener getOnChannelClickListener() {
            return mockListener;
        }
    }
}