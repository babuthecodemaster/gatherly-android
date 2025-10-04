package com.cosmic.gatherly;

import com.cosmic.gatherly.ui.adapters.ChannelAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for channel selection functionality
 * Verifies that channel highlighting and selection state management works correctly
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ChannelSelectionTest {

    private ChannelAdapter channelAdapter;
    private List<ChannelAdapter.ChannelItem> testChannels;

    @Before
    public void setUp() {
        // Create test channels
        testChannels = new ArrayList<>();
        
        // Add header
        testChannels.add(new ChannelAdapter.ChannelItem("TEXT CHANNELS", true));
        
        // Add text channels
        testChannels.add(new ChannelAdapter.ChannelItem("general", android.R.drawable.ic_menu_info_details, ChannelAdapter.ChannelItem.ChannelType.TEXT));
        testChannels.add(new ChannelAdapter.ChannelItem("announcements", android.R.drawable.ic_menu_info_details, ChannelAdapter.ChannelItem.ChannelType.TEXT));
        testChannels.add(new ChannelAdapter.ChannelItem("random", android.R.drawable.ic_menu_info_details, ChannelAdapter.ChannelItem.ChannelType.TEXT));
        
        // Create adapter with mock listener
        channelAdapter = new ChannelAdapter(testChannels, new ChannelAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(ChannelAdapter.ChannelItem channel) {
                // Mock implementation
            }

            @Override
            public void onChannelSettingsClick(String categoryName) {
                // Mock implementation
            }
        });
    }

    @Test
    public void testInitialChannelSelection() {
        // Initially no channel should be selected
        for (ChannelAdapter.ChannelItem item : testChannels) {
            if (!item.isHeader()) {
                assertFalse("Channel should not be selected initially", item.isSelected());
            }
        }
    }

    @Test
    public void testChannelSelectionUpdate() {
        // Select a channel
        channelAdapter.setSelectedChannel("general");
        
        // Verify only the selected channel is marked as selected
        for (ChannelAdapter.ChannelItem item : testChannels) {
            if (!item.isHeader()) {
                if ("general".equals(item.getName())) {
                    assertTrue("General channel should be selected", item.isSelected());
                } else {
                    assertFalse("Other channels should not be selected", item.isSelected());
                }
            }
        }
    }

    @Test
    public void testChannelSelectionSwitch() {
        // Select first channel
        channelAdapter.setSelectedChannel("general");
        
        // Verify general is selected
        ChannelAdapter.ChannelItem generalChannel = findChannelByName("general");
        assertNotNull("General channel should exist", generalChannel);
        assertTrue("General channel should be selected", generalChannel.isSelected());
        
        // Switch to another channel
        channelAdapter.setSelectedChannel("announcements");
        
        // Verify selection switched
        assertFalse("General channel should no longer be selected", generalChannel.isSelected());
        
        ChannelAdapter.ChannelItem announcementsChannel = findChannelByName("announcements");
        assertNotNull("Announcements channel should exist", announcementsChannel);
        assertTrue("Announcements channel should be selected", announcementsChannel.isSelected());
    }

    @Test
    public void testNonExistentChannelSelection() {
        // Try to select a channel that doesn't exist
        channelAdapter.setSelectedChannel("nonexistent");
        
        // Verify no channel is selected
        for (ChannelAdapter.ChannelItem item : testChannels) {
            if (!item.isHeader()) {
                assertFalse("No channel should be selected for nonexistent channel", item.isSelected());
            }
        }
    }

    @Test
    public void testHeaderItemsNotSelectable() {
        // Try to select a header item
        channelAdapter.setSelectedChannel("TEXT CHANNELS");
        
        // Verify no channel is selected (headers should not be selectable)
        for (ChannelAdapter.ChannelItem item : testChannels) {
            if (!item.isHeader()) {
                assertFalse("No channel should be selected when trying to select header", item.isSelected());
            }
        }
    }

    private ChannelAdapter.ChannelItem findChannelByName(String name) {
        for (ChannelAdapter.ChannelItem item : testChannels) {
            if (!item.isHeader() && name.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }
}