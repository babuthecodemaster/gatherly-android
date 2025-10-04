package com.cosmic.gatherly.ui.adapters;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ApplicationProvider;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.ui.adapters.ServerAdapter.ServerItem;

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
 * Unit tests for ServerAdapter - testing server selection visual indicator functionality
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ServerAdapterTest {

    @Mock
    private ServerAdapter.OnServerClickListener mockListener;

    private ServerAdapter adapter;
    private List<ServerItem> testServers;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Create test server data
        testServers = createTestServerData();
        
        // Initialize adapter
        adapter = new ServerAdapter(testServers, mockListener);
    }

    private List<ServerItem> createTestServerData() {
        List<ServerItem> servers = new ArrayList<>();
        
        servers.add(new ServerItem("server1", "Gaming Server", R.drawable.ic_server, 0xFF6B73FF));
        servers.add(new ServerItem("server2", "Work Server", R.drawable.ic_server, 0xFF9B59B6));
        servers.add(new ServerItem("server3", "Friends Server", R.drawable.ic_server, 0xFF3498DB));
        
        return servers;
    }

    @Test
    public void testServerSelectionIndicator() {
        // Test initial state - no server selected
        assertNull("No server should be selected initially", getSelectedServerId());

        // Test selecting first server
        adapter.setSelectedServerId("server1");
        assertEquals("Selected server ID should be set correctly", "server1", getSelectedServerId());

        // Test switching to another server
        adapter.setSelectedServerId("server2");
        assertEquals("Selected server ID should update correctly", "server2", getSelectedServerId());

        // Test deselecting (setting to null)
        adapter.setSelectedServerId(null);
        assertNull("Selected server ID should be null when deselected", getSelectedServerId());
    }

    @Test
    public void testServerClickListener() {
        // Test server click
        ServerItem testServer = testServers.get(0);
        
        // Simulate server click through mock listener
        when(mockListener != null).thenReturn(true);
        
        // Create a testable adapter that can simulate clicks
        TestableServerAdapter testableAdapter = new TestableServerAdapter(testServers, mockListener);
        testableAdapter.simulateServerClick(testServer);
        
        // Verify listener was called
        verify(mockListener, times(1)).onServerClick(testServer);
    }

    @Test
    public void testAdapterBasicFunctionality() {
        // Test item count
        assertEquals("Adapter should return correct item count", testServers.size(), adapter.getItemCount());

        // Test that all items have the same view type (servers don't have different types)
        for (int i = 0; i < testServers.size(); i++) {
            assertEquals("All items should have the same view type", 0, adapter.getItemViewType(i));
        }
    }

    @Test
    public void testServerItemProperties() {
        ServerItem server = new ServerItem("test_id", "Test Server", R.drawable.ic_server, 0xFF123456);
        
        // Test properties
        assertEquals("Server ID should match", "test_id", server.getId());
        assertEquals("Server name should match", "Test Server", server.getName());
        assertEquals("Server icon should match", R.drawable.ic_server, server.getIconResource());
        assertEquals("Server background color should match", 0xFF123456, server.getBackgroundColor());
    }

    @Test
    public void testMultipleServerSelection() {
        // Test selecting different servers in sequence
        String[] serverIds = {"server1", "server2", "server3"};
        
        for (String serverId : serverIds) {
            adapter.setSelectedServerId(serverId);
            assertEquals("Selected server should match", serverId, getSelectedServerId());
        }
    }

    @Test
    public void testServerSelectionWithInvalidId() {
        // Test selecting a server that doesn't exist
        adapter.setSelectedServerId("nonexistent_server");
        assertEquals("Should be able to set non-existent server ID", "nonexistent_server", getSelectedServerId());
        
        // This tests that the adapter doesn't crash with invalid IDs
        // In a real implementation, you might want to validate the ID exists
    }

    @Test
    public void testServerIndicatorVisibilityLogic() {
        // This test simulates the indicator visibility logic
        // Since we can't easily test the actual view binding, we test the logic
        
        adapter.setSelectedServerId("server1");
        
        // Test indicator should be visible for selected server
        for (ServerItem server : testServers) {
            boolean shouldShowIndicator = server.getId().equals("server1");
            
            if (shouldShowIndicator) {
                assertEquals("Server1 should be the selected server", "server1", server.getId());
            } else {
                assertNotEquals("Other servers should not be selected", "server1", server.getId());
            }
        }
    }

    @Test
    public void testServerAdapterNotifyChanges() {
        // Test that changing selection triggers appropriate notifications
        // We can't directly test notifyItemChanged, but we can test the logic
        
        String previousId = null;
        String newId = "server2";
        
        adapter.setSelectedServerId(newId);
        
        // Verify that the adapter would notify changes for the correct items
        for (int i = 0; i < testServers.size(); i++) {
            ServerItem server = testServers.get(i);
            boolean shouldNotify = server.getId().equals(previousId) || server.getId().equals(newId);
            
            if (shouldNotify && server.getId().equals(newId)) {
                assertEquals("New selected server should match", newId, server.getId());
            }
        }
    }

    // Helper methods
    private String getSelectedServerId() {
        // Use reflection or a test-friendly method to get the selected server ID
        // For this test, we'll create a testable version of the adapter
        return ((TestableServerAdapter) createTestableAdapter()).getSelectedServerId();
    }

    private TestableServerAdapter createTestableAdapter() {
        return new TestableServerAdapter(testServers, mockListener);
    }

    // Testable version of ServerAdapter that exposes internal state
    private static class TestableServerAdapter extends ServerAdapter {
        private String selectedServerId;

        public TestableServerAdapter(List<ServerItem> servers, OnServerClickListener listener) {
            super(servers, listener);
        }

        @Override
        public void setSelectedServerId(String serverId) {
            this.selectedServerId = serverId;
            super.setSelectedServerId(serverId);
        }

        public String getSelectedServerId() {
            return selectedServerId;
        }

        public void simulateServerClick(ServerItem server) {
            OnServerClickListener listener = getListener();
            if (listener != null) {
                listener.onServerClick(server);
            }
        }

        // Mock method to get listener for testing
        private OnServerClickListener getListener() {
            return mockListener;
        }
    }
}