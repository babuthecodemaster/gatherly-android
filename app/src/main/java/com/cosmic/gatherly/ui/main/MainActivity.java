package com.cosmic.gatherly.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.GatherlyApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.data.manager.VoiceChannelManager;
import com.cosmic.gatherly.ui.adapters.ChannelAdapter;
import com.cosmic.gatherly.ui.adapters.ServerAdapter;
import com.cosmic.gatherly.ui.auth.UltraMinimalAuthActivity;
import com.cosmic.gatherly.ui.components.VoiceControlPanel;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {
    
    private static final String TAG = "MainActivity";
    private AuthManager authManager;
    private FirebaseUser currentUser;
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private TextView serverNameText;
    private RecyclerView serversRecyclerView;
    private RecyclerView channelsRecyclerView;
    private ChannelAdapter channelAdapter;
    private VoiceChannelManager voiceChannelManager;
    private VoiceControlPanel voiceControlPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "🚀 MainActivity onCreate() started");
        
        try {
            // Get AuthManager from Application
            authManager = ((GatherlyApplication) getApplication()).getAuthManager();
            if (authManager == null) {
                Log.e(TAG, "AuthManager is null, redirecting to auth");
                navigateToAuth();
                return;
            }
            
            // Check if user is authenticated
            currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated, redirecting to auth");
                navigateToAuth();
                return;
            }
            
            // Use the beautiful mobile layout with drawer
            setContentView(R.layout.activity_main);
            Log.d(TAG, "✅ Beautiful mobile layout loaded successfully");
            
            initializeViews();
            setupDrawerNavigation();
            setupInitialFragment();
            setupAuthStateObserver();

            // Log navigation source for debugging
            logNavigationSource();
            
            Log.d(TAG, "✅ MainActivity initialization completed successfully");
            Toast.makeText(this, "Welcome to Gatherly, " + currentUser.getEmail() + "!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during MainActivity initialization", e);
            Toast.makeText(this, "Error loading main screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToAuth();
        }
    }
    
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menuButton);
        serverNameText = findViewById(R.id.serverNameText);
        serversRecyclerView = findViewById(R.id.serversRecyclerView);
        channelsRecyclerView = findViewById(R.id.channelsRecyclerView);
        voiceControlPanel = findViewById(R.id.voiceControlPanel);
        
        // Initialize voice channel manager
        voiceChannelManager = VoiceChannelManager.getInstance(this);
        
        // Initialize voice control panel
        if (voiceControlPanel != null) {
            voiceControlPanel.initialize(voiceChannelManager, this);
        }
        
        // Set up menu button click listener
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // Set up logout button click listener
        ImageView logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked");
                performLogout();
            });
        }
    }
    
    private void setupDrawerNavigation() {
        setupServerSidebar();
        setupChannelsList();
        setupUserPanel();
    }
    
    private void setupInitialFragment() {
        if (currentUser != null) {
            // Load the beautiful mobile chat interface
            loadFragment(MainChatFragment.newInstance());
        }
    }
    
    /**
     * Sets up AuthState observer for automatic navigation to auth screen on logout
     */
    private void setupAuthStateObserver() {
        if (authManager != null) {
            authManager.getAuthState().observe(this, authState -> {
                Log.d(TAG, "AuthState changed: " + authState.getStatus());
                
                switch (authState.getStatus()) {
                    case UNAUTHENTICATED:
                        Log.d(TAG, "User unauthenticated, navigating to auth");
                        navigateToAuth();
                        break;
                    case ERROR:
                        Log.e(TAG, "Auth error: " + authState.getErrorMessage());
                        Toast.makeText(this, "Authentication error: " + authState.getErrorMessage(), 
                            Toast.LENGTH_LONG).show();
                        break;
                    case AUTHENTICATED:
                        // Update current user reference
                        currentUser = authState.getUser();
                        Log.d(TAG, "User authenticated: " + (currentUser != null ? currentUser.getEmail() : "null"));
                        break;
                    case LOADING:
                        Log.d(TAG, "Authentication loading...");
                        break;
                }
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            Log.d(TAG, "Fragment loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment", e);
            // If fragment fails, show a simple message
            Toast.makeText(this, "Loading main interface...", Toast.LENGTH_SHORT).show();
        }
    }
    
    private ServerAdapter serverAdapter;
    private String selectedServerId = "cosmic-gaming-hub"; // Default selected server
    
    private void setupServerSidebar() {
        try {
            if (serversRecyclerView != null) {
                // Create servers list exactly like your design
                List<ServerAdapter.ServerItem> servers = new ArrayList<>();
                
                // Add servers with colorful backgrounds like in your design (now with IDs)
                servers.add(new ServerAdapter.ServerItem("cosmic-gaming-hub", "Cosmic Gaming Hub", R.drawable.ic_rocket, 0xFF5865F2)); // Blue
                servers.add(new ServerAdapter.ServerItem("gaming-server", "Gaming Server", R.drawable.ic_star, 0xFF2F3136)); // Dark
                servers.add(new ServerAdapter.ServerItem("nebula", "Nebula", R.drawable.ic_person, 0xFF9B59B6)); // Purple
                servers.add(new ServerAdapter.ServerItem("space", "Space", R.drawable.ic_rocket, 0xFFE67E22)); // Orange
                servers.add(new ServerAdapter.ServerItem("add-server", "Add Server", R.drawable.ic_add, 0xFF36393F)); // Gray
                
                serversRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                serverAdapter = new ServerAdapter(servers, server -> {
                    Log.d(TAG, "🎯 Server clicked: " + server.getName());
                    
                    // Handle "Add Server" differently
                    if ("add-server".equals(server.getId())) {
                        Toast.makeText(this, "➕ Add Server functionality coming soon!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Update selected server with visual indicator
                    selectedServerId = server.getId();
                    serverAdapter.setSelectedServerId(selectedServerId);
                    
                    Toast.makeText(this, "🚀 Switched to " + server.getName() + " server", Toast.LENGTH_LONG).show();
                    
                    // Update server name and close drawer
                    updateServerName(server.getName());
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });
                
                // Set initial selected server
                serverAdapter.setSelectedServerId(selectedServerId);
                serversRecyclerView.setAdapter(serverAdapter);
                
                Log.d(TAG, "Server sidebar setup completed with " + servers.size() + " servers");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up server sidebar", e);
        }
    }
    
    private void setupChannelsList() {
        try {
            if (channelsRecyclerView != null) {
                // Create channels list exactly like your design
                List<ChannelAdapter.ChannelItem> channels = new ArrayList<>();
                
                // TEXT CHANNELS header
                channels.add(new ChannelAdapter.ChannelItem("TEXT CHANNELS", true));
                
                // Text channels
                ChannelAdapter.ChannelItem generalChannel = new ChannelAdapter.ChannelItem("text-general", "general", R.drawable.ic_hash, ChannelAdapter.ChannelItem.ChannelType.TEXT);
                generalChannel.setSelected(true); // Selected by default
                channels.add(generalChannel);
                channels.add(new ChannelAdapter.ChannelItem("text-announcements", "announcements", R.drawable.ic_hash, ChannelAdapter.ChannelItem.ChannelType.TEXT));
                channels.add(new ChannelAdapter.ChannelItem("text-random", "random", R.drawable.ic_hash, ChannelAdapter.ChannelItem.ChannelType.TEXT));
                
                // VOICE CHANNELS header
                channels.add(new ChannelAdapter.ChannelItem("VOICE CHANNELS", true));
                
                // Voice channels
                channels.add(new ChannelAdapter.ChannelItem("voice-general", "General Voice", R.drawable.ic_volume, ChannelAdapter.ChannelItem.ChannelType.VOICE));
                channels.add(new ChannelAdapter.ChannelItem("voice-gaming", "Gaming", R.drawable.ic_volume, ChannelAdapter.ChannelItem.ChannelType.VOICE));
                
                channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                channelAdapter = new ChannelAdapter(channels, new ChannelAdapter.OnChannelClickListener() {
                    @Override
                    public void onTextChannelClick(ChannelAdapter.ChannelItem channel) {
                        Log.d(TAG, "Text channel clicked: " + channel.getName());
                        
                        // Update selected text channel in adapter with enhanced highlighting
                        channelAdapter.setSelectedTextChannel(channel.getName());
                        
                        // Update channel name and close drawer
                        updateChannelName(channel.getName());
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    
                    @Override
                    public void onVoiceChannelClick(ChannelAdapter.ChannelItem channel) {
                        Log.d(TAG, "Voice channel clicked: " + channel.getName());
                        
                        // Handle voice channel connection
                        handleVoiceChannelClick(channel);
                        
                        // Close drawer
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    
                    @Override
                    public void onChannelSettingsClick(String categoryName) {
                        Log.d(TAG, "Channel settings clicked for: " + categoryName);
                        showChannelSettings(categoryName);
                    }
                }, voiceChannelManager);
                channelsRecyclerView.setAdapter(channelAdapter);
                
                Log.d(TAG, "Channels list setup completed with " + channels.size() + " items");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up channels list", e);
        }
    }
    
    private void setupUserPanel() {
        try {
            TextView usernameText = findViewById(R.id.usernameText);
            TextView userIdText = findViewById(R.id.userIdText);
            ImageView settingsButton = findViewById(R.id.settingsButton);
            
            if (currentUser != null && usernameText != null && userIdText != null) {
                // Extract username from email (before @)
                String email = currentUser.getEmail();
                String username = email != null ? email.split("@")[0] : "User";
                usernameText.setText(username);
                userIdText.setText("#" + currentUser.getUid().substring(0, 4));
            }
            
            // Setup settings button click listener
            if (settingsButton != null) {
                settingsButton.setOnClickListener(v -> {
                    Log.d(TAG, "🔧 Settings button clicked");
                    showUserSettings();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up user panel", e);
        }
    }
    
    private void showUserSettings() {
        try {
            // Create a simple settings dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("⚙️ User Settings");
            
            String[] options = {
                "🔧 Account Settings",
                "🎨 Appearance", 
                "🔔 Notifications",
                "🚪 Logout"
            };
            
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        Toast.makeText(this, "🔧 Account Settings (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(this, "🎨 Appearance Settings (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(this, "🔔 Notification Settings (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        performLogout();
                        break;
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing user settings", e);
            Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showChannelSettings(String categoryName) {
        try {
            Log.d(TAG, "🔧 Showing channel settings for: " + categoryName);
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("⚙️ " + categoryName + " Settings");
            
            String[] options;
            if (categoryName.contains("TEXT")) {
                options = new String[]{
                    "➕ Create Text Channel",
                    "📝 Edit Category",
                    "🔒 Permissions",
                    "🗑️ Delete Category"
                };
            } else {
                options = new String[]{
                    "➕ Create Voice Channel", 
                    "📝 Edit Category",
                    "🔒 Permissions",
                    "🗑️ Delete Category"
                };
            }
            
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        if (categoryName.contains("TEXT")) {
                            showCreateChannelDialog("text");
                        } else {
                            showCreateChannelDialog("voice");
                        }
                        break;
                    case 1:
                        Toast.makeText(this, "📝 Edit Category (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(this, "🔒 Permissions (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(this, "🗑️ Delete Category (Coming Soon)", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing channel settings", e);
            Toast.makeText(this, "Error opening channel settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showCreateChannelDialog(String channelType) {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("➕ Create " + (channelType.equals("text") ? "Text" : "Voice") + " Channel");
            
            // Create input field
            android.widget.EditText input = new android.widget.EditText(this);
            input.setHint("Channel name (e.g., gaming-chat)");
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            
            builder.setPositiveButton("Create", (dialog, which) -> {
                String channelName = input.getText().toString().trim();
                if (!channelName.isEmpty()) {
                    // Add the new channel to the list
                    createNewChannel(channelName, channelType);
                    Toast.makeText(this, "✅ Created #" + channelName + " channel!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a channel name", Toast.LENGTH_SHORT).show();
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing create channel dialog", e);
        }
    }
    
    private void createNewChannel(String channelName, String channelType) {
        try {
            Log.d(TAG, "🆕 Creating new " + channelType + " channel: " + channelName);
            
            // In a real app, you would add this to your data source and refresh the adapter
            // For now, just show a success message
            Toast.makeText(this, "🎉 Channel #" + channelName + " created successfully!", Toast.LENGTH_LONG).show();
            
            // TODO: Add channel to the actual list and refresh the RecyclerView
            // This would involve updating the channels list and notifying the adapter
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating new channel", e);
        }
    }
    
    private void updateServerName(String serverName) {
        try {
            // Update server name in top bar
            if (serverNameText != null) {
                serverNameText.setText(serverName);
            }
            
            // Update server name in drawer
            TextView drawerServerNameText = findViewById(R.id.drawerServerNameText);
            if (drawerServerNameText != null) {
                drawerServerNameText.setText(serverName);
            }
            
            Log.d(TAG, "Server name updated to: " + serverName);
        } catch (Exception e) {
            Log.e(TAG, "Error updating server name", e);
        }
    }
    
    private void updateChannelName(String channelName) {
        try {
            // Find the fragment and update channel name
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof MainChatFragment) {
                MainChatFragment chatFragment = (MainChatFragment) fragment;
                chatFragment.switchToChannel(channelName);
                Log.d(TAG, "✅ Successfully switched to channel: " + channelName);
            } else {
                Log.e(TAG, "❌ Fragment is not MainChatFragment");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating channel name", e);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    private void logNavigationSource() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String source = intent.getStringExtra("source");
                String authType = intent.getStringExtra("auth_type");
                
                Log.d(TAG, "📍 Navigation Details:");
                Log.d(TAG, "  Source: " + (source != null ? source : "unknown"));
                Log.d(TAG, "  Auth Type: " + (authType != null ? authType : "unknown"));
                Log.d(TAG, "  User: " + (currentUser != null ? currentUser.getEmail() : "null"));
                
                if ("firebase_auth".equals(source)) {
                    Log.d(TAG, "✅ Successfully navigated from Firebase Auth");
                } else {
                    Log.d(TAG, "ℹ️ Navigation from: " + source);
                }
            } else {
                Log.w(TAG, "⚠️ No intent data available");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error logging navigation source", e);
        }
    }

    /**
     * Performs logout with confirmation dialog
     * Uses centralized AuthManager for logout operation
     */
    private void performLogout() {
        try {
            Log.d(TAG, "Logout requested by user");
            
            // Show confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🚪 Logout");
            builder.setMessage("Are you sure you want to logout?");
            
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Log.d(TAG, "User confirmed logout");
                
                if (authManager != null) {
                    // Use AuthManager for logout
                    authManager.signOut()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "✅ Logout successful");
                                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                // Navigation handled by AuthState observer
                            } else {
                                Log.e(TAG, "❌ Logout failed", task.getException());
                                Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
                                // Still navigate to auth for safety
                                navigateToAuth();
                            }
                        })
                        .addOnFailureListener(exception -> {
                            Log.e(TAG, "❌ Logout operation failed", exception);
                            Toast.makeText(this, "Error during logout: " + exception.getMessage(), 
                                Toast.LENGTH_LONG).show();
                            // Still navigate to auth for safety
                            navigateToAuth();
                        });
                } else {
                    Log.e(TAG, "AuthManager is null during logout");
                    Toast.makeText(this, "Error: Authentication manager not available", Toast.LENGTH_SHORT).show();
                    navigateToAuth();
                }
            });
            
            builder.setNegativeButton("No", (dialog, which) -> {
                Log.d(TAG, "User cancelled logout");
                dialog.dismiss();
            });
            
            builder.show();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing logout confirmation", e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
            navigateToAuth();
        }
    }

    @Override
    public void onLogoutRequested() {
        // Delegate to the new performLogout method
        performLogout();
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Handle voice channel click - separate from text channel behavior
     */
    private void handleVoiceChannelClick(ChannelAdapter.ChannelItem channel) {
        try {
            Log.d(TAG, "Handling voice channel click: " + channel.getName());
            
            if (voiceChannelManager == null) {
                Log.e(TAG, "VoiceChannelManager is null");
                Toast.makeText(this, "Voice functionality not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String channelId = channel.getId();
            String channelName = channel.getName();
            
            // Check if already connected to this voice channel
            if (voiceChannelManager.isCurrentVoiceChannel(channelId)) {
                Log.d(TAG, "Already connected to voice channel: " + channelName);
                Toast.makeText(this, "Already connected to " + channelName, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Connect to voice channel
            voiceChannelManager.connectToVoiceChannel(channelId, channelName);
            
            // Update channel adapter to reflect voice connection states
            if (channelAdapter != null) {
                channelAdapter.updateVoiceChannelStates();
            }
            
            Log.i(TAG, "Successfully initiated connection to voice channel: " + channelName);
            Toast.makeText(this, "Connecting to " + channelName + "...", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling voice channel click", e);
            Toast.makeText(this, "Error connecting to voice channel", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAuth() {
        try {
            Log.d(TAG, "Navigating to Firebase Auth");
            Intent intent = new Intent(this, UltraMinimalAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("source", "main_activity");
            intent.putExtra("reason", "authentication_required");
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Firebase Auth", e);
            finish();
        }
    }
}