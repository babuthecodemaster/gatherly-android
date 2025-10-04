package com.cosmic.gatherly.ui.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.SearchManager;
import com.cosmic.gatherly.data.model.SearchResult;
import com.cosmic.gatherly.ui.adapters.ChannelAdapter;
import com.cosmic.gatherly.ui.adapters.MessageAdapter;
import com.cosmic.gatherly.ui.components.SearchDialog;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainChatFragment extends Fragment {
    
    private MainActivityCallback mainCallback;
    private RecyclerView channelsRecyclerView;
    private RecyclerView messagesRecyclerView;
    private RecyclerView membersRecyclerView;
    
    // Search functionality
    private ImageView searchButton;
    private SearchDialog searchDialog;
    private SearchManager searchManager;
    private String currentChannelId = "general";
    private String currentChannelName = "general";
    
    // Members functionality
    private ImageView membersButton;
    private boolean showMembersSidebar = false;
    private com.cosmic.gatherly.ui.components.MembersDialog membersDialog;
    
    // Mentions functionality
    private ImageView mentionsButton;
    private TextView mentionsNotificationBadge;
    private com.cosmic.gatherly.ui.components.MentionsDialog mentionsDialog;
    private com.cosmic.gatherly.data.manager.MentionsManager mentionsManager;
    
    // File upload functionality
    private ImageView attachmentButton;
    private static final int FILE_PICKER_REQUEST_CODE = 1001;
    private static final long MAX_FILE_SIZE_MB = 100; // 100MB limit
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private boolean isUploading = false;
    private com.cosmic.gatherly.ui.components.FileUploadProgressDialog uploadProgressDialog;
    private com.cosmic.gatherly.data.service.MessageService messageService;

    public static MainChatFragment newInstance() {
        return new MainChatFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivityCallback) {
            mainCallback = (MainActivityCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MainActivityCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_chat, container, false);
        
        initializeViews(view);
        setupRecyclerViews();
        loadInitialData();
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize services
        messageService = new com.cosmic.gatherly.data.service.MessageService(getContext());
        mentionsManager = com.cosmic.gatherly.data.manager.MentionsManager.getInstance(getContext());
        
        // Setup message input after view is fully created
        setupMessageInput();
        setupSearchFunctionality();
        setupMembersFunctionality();
        setupMentionsFunctionality();
        setupFileUploadFunctionality();
        setupKeyboardShortcuts();
    }

    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        searchButton = view.findViewById(R.id.searchButton);
        membersButton = view.findViewById(R.id.membersButton);
        mentionsButton = view.findViewById(R.id.mentionsButton);
        mentionsNotificationBadge = view.findViewById(R.id.mentionsNotificationBadge);
        attachmentButton = view.findViewById(R.id.attachmentButton);
        // Channels are now in the drawer, not in the fragment
    }

    private void setupRecyclerViews() {
        // Setup messages RecyclerView
        if (messagesRecyclerView != null) {
            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    private void loadInitialData() {
        FirebaseUser currentUser = mainCallback.getCurrentUser();
        if (currentUser != null) {
            // Load the beautiful mobile chat interface
            setupMessagesList();
            loadSampleData();
        }
    }
    

    
    private void updateChannelHeader(String channelName) {
        try {
            View view = getView();
            if (view != null) {
                TextView channelNameText = view.findViewById(R.id.channelNameText);
                if (channelNameText != null) {
                    channelNameText.setText(channelName);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "Error updating channel header", e);
        }
    }
    
    private void setupMessagesList() {
        try {
            android.util.Log.d("MainChatFragment", "Setting up messages list");
            
            // Create messages exactly like your design
            List<MessageAdapter.MessageItem> messages = new ArrayList<>();
            
            messages.add(new MessageAdapter.MessageItem(
                "CosmicExplorer", 
                "Welcome to the Cosmic Gaming Hub! 🚀", 
                "over 1 year ago",
                R.drawable.ic_person,
                0xFF00D166 // Green color
            ));
            
            messages.add(new MessageAdapter.MessageItem(
                "StarDust", 
                "This place looks amazing! The cosmic theme is perfect ✨", 
                "over 1 year ago",
                "🚀 2", // Reactions
                R.drawable.ic_star,
                0xFF5865F2 // Blue color
            ));
            
            messages.add(new MessageAdapter.MessageItem(
                "NebulaNinja", 
                "Anyone up for some cosmic gaming tonight? 🎮", 
                "over 1 year ago",
                "🎮 1   🚀 1", // Multiple reactions
                R.drawable.ic_person,
                0xFFEB459E // Pink color
            ));
            
            messages.add(new MessageAdapter.MessageItem(
                "GalaxyGamer", 
                "I'm in! What game are we playing? 🎯", 
                "over 1 year ago",
                "🎮 2   🚀 3", // Multiple reactions
                R.drawable.ic_person,
                0xFFFEE75C // Yellow color
            ));
            
            if (messagesRecyclerView != null) {
                messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                MessageAdapter messageAdapter = new MessageAdapter(getContext(), messages);
                messagesRecyclerView.setAdapter(messageAdapter);
                android.util.Log.d("MainChatFragment", "Messages adapter set with " + messages.size() + " messages");
            } else {
                android.util.Log.e("MainChatFragment", "messagesRecyclerView is null!");
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "Error setting up messages list", e);
        }
    }
    

    
    private void setupMessageInput() {
        try {
            android.util.Log.d("MainChatFragment", "🔧 Setting up message input");
            
            View view = getView();
            if (view == null) {
                android.util.Log.e("MainChatFragment", "❌ View is null in setupMessageInput!");
                return;
            }
            
            com.google.android.material.textfield.TextInputEditText messageInput = view.findViewById(R.id.messageEditText);
            com.google.android.material.floatingactionbutton.FloatingActionButton sendButton = view.findViewById(R.id.sendButton);
            
            android.util.Log.d("MainChatFragment", "🔍 messageInput: " + (messageInput != null ? "found" : "null"));
            android.util.Log.d("MainChatFragment", "🔍 sendButton: " + (sendButton != null ? "found" : "null"));
            
            if (sendButton != null && messageInput != null) {
                // Set up send button click listener with debugging
                sendButton.setOnClickListener(v -> {
                    android.util.Log.d("MainChatFragment", "🎯 Send button clicked!");
                    String message = messageInput.getText().toString().trim();
                    android.util.Log.d("MainChatFragment", "📝 Message text: '" + message + "'");
                    
                    if (!message.isEmpty()) {
                        android.util.Log.d("MainChatFragment", "✅ Sending message: " + message);
                        // Add message to chat
                        addNewMessage("You", message);
                        messageInput.setText("");
                        android.widget.Toast.makeText(getContext(), "Message sent! 💬", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.w("MainChatFragment", "⚠️ Message is empty, not sending");
                        android.widget.Toast.makeText(getContext(), "Please type a message first", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                
                // Set up Enter key listener for sending messages
                messageInput.setOnEditorActionListener((v, actionId, event) -> {
                    android.util.Log.d("MainChatFragment", "⌨️ Editor action: " + actionId);
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND || 
                        (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                        String message = messageInput.getText().toString().trim();
                        if (!message.isEmpty()) {
                            android.util.Log.d("MainChatFragment", "✅ Sending message via Enter: " + message);
                            addNewMessage("You", message);
                            messageInput.setText("");
                            android.widget.Toast.makeText(getContext(), "Message sent! 💬", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    return false;
                });
                
                android.util.Log.d("MainChatFragment", "✅ Message input setup completed successfully");
            } else {
                android.util.Log.e("MainChatFragment", "❌ Send button or message input is null!");
                if (sendButton == null) android.util.Log.e("MainChatFragment", "❌ sendButton is null");
                if (messageInput == null) android.util.Log.e("MainChatFragment", "❌ messageInput is null");
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up message input", e);
        }
    }
    
    private void addNewMessage(String username, String message) {
        try {
            android.util.Log.d("MainChatFragment", "🔄 Adding new message: " + username + ": " + message);
            
            if (messagesRecyclerView == null) {
                android.util.Log.e("MainChatFragment", "❌ messagesRecyclerView is null!");
                return;
            }
            
            if (!(messagesRecyclerView.getAdapter() instanceof MessageAdapter)) {
                android.util.Log.e("MainChatFragment", "❌ Adapter is not MessageAdapter!");
                return;
            }
            
            MessageAdapter adapter = (MessageAdapter) messagesRecyclerView.getAdapter();
            android.util.Log.d("MainChatFragment", "📋 Current message count: " + adapter.getItemCount());
            
            // Create new message item
            MessageAdapter.MessageItem newMessage = new MessageAdapter.MessageItem(
                username,
                message,
                "now",
                R.drawable.ic_person,
                0xFF4ECDC4 // Cosmic blue color for user messages
            );
            
            // Add message to adapter
            adapter.addMessage(newMessage);
            android.util.Log.d("MainChatFragment", "📋 New message count: " + adapter.getItemCount());
            
            // Scroll to bottom to show new message
            messagesRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
            
            // Add message to search index
            addMessageToSearchIndex(username, message);
            
            // Parse and add mentions from the message
            parseMentionsFromMessage(message, username);
            
            android.util.Log.d("MainChatFragment", "✅ New message added successfully: " + username + ": " + message);
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error adding new message", e);
        }
    }
    

    
    private void loadSampleData() {
        // Update channel name in header
        try {
            View view = getView();
            if (view != null) {
                TextView channelNameText = view.findViewById(R.id.channelNameText);
                if (channelNameText != null) {
                    channelNameText.setText("general");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "Error loading sample data", e);
        }
    }
    
    /**
     * Switch to a different channel and load its messages
     */
    public void switchToChannel(String channelName) {
        try {
            android.util.Log.d("MainChatFragment", "🔄 Switching to channel: " + channelName);
            
            // Update current channel information
            currentChannelName = channelName;
            currentChannelId = channelName.toLowerCase();
            
            // Update channel name in header
            updateChannelHeader(channelName);
            
            // Load different messages based on channel
            loadChannelMessages(channelName);
            
            android.util.Log.d("MainChatFragment", "✅ Successfully switched to channel: " + channelName);
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error switching to channel: " + channelName, e);
        }
    }
    
    /**
     * Load messages for a specific channel
     */
    private void loadChannelMessages(String channelName) {
        try {
            if (messagesRecyclerView == null || messagesRecyclerView.getAdapter() == null) {
                android.util.Log.e("MainChatFragment", "❌ Cannot load messages - RecyclerView or adapter is null");
                return;
            }
            
            MessageAdapter adapter = (MessageAdapter) messagesRecyclerView.getAdapter();
            
            // Clear existing messages
            adapter.clearMessages();
            
            // Load different messages based on channel
            List<MessageAdapter.MessageItem> channelMessages = getMessagesForChannel(channelName);
            
            // Add messages to adapter
            for (MessageAdapter.MessageItem message : channelMessages) {
                adapter.addMessage(message);
            }
            
            android.util.Log.d("MainChatFragment", "📋 Loaded " + channelMessages.size() + " messages for channel: " + channelName);
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error loading channel messages", e);
        }
    }
    
    /**
     * Get sample messages for different channels
     */
    private List<MessageAdapter.MessageItem> getMessagesForChannel(String channelName) {
        List<MessageAdapter.MessageItem> messages = new ArrayList<>();
        
        switch (channelName.toLowerCase()) {
            case "general":
                messages.add(new MessageAdapter.MessageItem(
                    "CosmicExplorer", 
                    "Welcome to the Cosmic Gaming Hub! 🚀", 
                    "over 1 year ago",
                    R.drawable.ic_person,
                    0xFF00D166
                ));
                messages.add(new MessageAdapter.MessageItem(
                    "StarDust", 
                    "This place looks amazing! The cosmic theme is perfect ✨", 
                    "over 1 year ago",
                    "🚀 2",
                    R.drawable.ic_star,
                    0xFF5865F2
                ));
                break;
                
            case "announcements":
                messages.add(new MessageAdapter.MessageItem(
                    "Admin", 
                    "📢 Welcome to the announcements channel!", 
                    "2 days ago",
                    R.drawable.ic_person,
                    0xFFFF6B6B
                ));
                messages.add(new MessageAdapter.MessageItem(
                    "Admin", 
                    "🎉 New features coming soon! Stay tuned for updates.", 
                    "1 day ago",
                    "🎉 5   👀 3",
                    R.drawable.ic_person,
                    0xFFFF6B6B
                ));
                break;
                
            case "random":
                messages.add(new MessageAdapter.MessageItem(
                    "RandomUser", 
                    "Just sharing some random thoughts here! 🤔", 
                    "3 hours ago",
                    R.drawable.ic_person,
                    0xFFFEE75C
                ));
                messages.add(new MessageAdapter.MessageItem(
                    "ChillGamer", 
                    "Anyone else love the cosmic vibes of this app? 🌌", 
                    "2 hours ago",
                    "🌌 4   ❤️ 2",
                    R.drawable.ic_person,
                    0xFF9B59B6
                ));
                break;
                
            default:
                // Default messages for unknown channels
                messages.add(new MessageAdapter.MessageItem(
                    "System", 
                    "Welcome to #" + channelName + "! 👋", 
                    "now",
                    R.drawable.ic_person,
                    0xFF4ECDC4
                ));
                break;
        }
        
        return messages;
    }
    
    /**
     * Setup search functionality
     */
    private void setupSearchFunctionality() {
        try {
            android.util.Log.d("MainChatFragment", "🔍 Setting up search functionality");
            
            // Initialize search manager
            searchManager = SearchManager.getInstance(getContext());
            
            // Setup search button click listener
            if (searchButton != null) {
                searchButton.setOnClickListener(v -> openSearchDialog());
                android.util.Log.d("MainChatFragment", "✅ Search button click listener set");
            } else {
                android.util.Log.e("MainChatFragment", "❌ Search button is null!");
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up search functionality", e);
        }
    }
    
    /**
     * Setup members functionality
     */
    private void setupMembersFunctionality() {
        try {
            android.util.Log.d("MainChatFragment", "👥 Setting up members functionality");
            
            // Setup members button click listener
            if (membersButton != null) {
                membersButton.setOnClickListener(v -> toggleMembersSidebar());
                android.util.Log.d("MainChatFragment", "✅ Members button click listener set");
            } else {
                android.util.Log.e("MainChatFragment", "❌ Members button is null!");
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up members functionality", e);
        }
    }
    
    /**
     * Setup mentions functionality
     */
    private void setupMentionsFunctionality() {
        try {
            android.util.Log.d("MainChatFragment", "📢 Setting up mentions functionality");
            
            // Setup mentions button click listener
            if (mentionsButton != null) {
                mentionsButton.setOnClickListener(v -> openMentionsDialog());
                android.util.Log.d("MainChatFragment", "✅ Mentions button click listener set");
            } else {
                android.util.Log.e("MainChatFragment", "❌ Mentions button is null!");
            }
            
            // Setup mentions manager listener
            if (mentionsManager != null) {
                mentionsManager.addListener(new com.cosmic.gatherly.data.manager.MentionsManager.OnMentionsUpdateListener() {
                    @Override
                    public void onMentionsUpdated(java.util.List<com.cosmic.gatherly.data.model.Mention> mentions) {
                        // Update UI if needed
                    }

                    @Override
                    public void onUnreadCountChanged(int unreadCount) {
                        updateMentionsNotificationBadge(unreadCount);
                    }
                });
                
                // Initialize notification badge
                updateMentionsNotificationBadge(mentionsManager.getUnreadMentionsCount());
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up mentions functionality", e);
        }
    }
    
    /**
     * Open the mentions dialog
     */
    private void openMentionsDialog() {
        try {
            android.util.Log.d("MainChatFragment", "📢 Opening mentions dialog");
            
            if (getContext() == null) {
                android.util.Log.e("MainChatFragment", "❌ Context is null, cannot open mentions dialog");
                return;
            }
            
            // Create and configure mentions dialog
            mentionsDialog = new com.cosmic.gatherly.ui.components.MentionsDialog(getContext());
            
            // Set mentions dialog listener
            mentionsDialog.setOnMentionsDialogListener(new com.cosmic.gatherly.ui.components.MentionsDialog.OnMentionsDialogListener() {
                @Override
                public void onMentionSelected(com.cosmic.gatherly.data.model.Mention mention) {
                    android.util.Log.d("MainChatFragment", "🎯 Mention selected: " + mention.getId());
                    
                    // Switch to the channel containing the mention if different
                    if (!mention.getChannelId().equals(currentChannelId)) {
                        switchToChannel(mention.getChannelName());
                    }
                    
                    android.widget.Toast.makeText(getContext(), 
                        "Jumped to mention in #" + mention.getChannelName(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onJumpToMessage(com.cosmic.gatherly.data.model.Mention mention) {
                    android.util.Log.d("MainChatFragment", "🎯 Jump to message: " + mention.getMessageId());
                    
                    // Switch to the channel containing the message if different
                    if (!mention.getChannelId().equals(currentChannelId)) {
                        switchToChannel(mention.getChannelName());
                    }
                    
                    android.widget.Toast.makeText(getContext(), 
                        "Jumped to message in #" + mention.getChannelName(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });
            
            // Show the dialog
            mentionsDialog.show();
            
            android.util.Log.d("MainChatFragment", "✅ Mentions dialog opened successfully");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error opening mentions dialog", e);
            android.widget.Toast.makeText(getContext(), "Failed to open mentions", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update the mentions notification badge
     */
    private void updateMentionsNotificationBadge(int unreadCount) {
        try {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (mentionsNotificationBadge != null) {
                        if (unreadCount > 0) {
                            mentionsNotificationBadge.setText(String.valueOf(unreadCount));
                            mentionsNotificationBadge.setVisibility(View.VISIBLE);
                            android.util.Log.d("MainChatFragment", "📢 Updated mentions badge: " + unreadCount);
                        } else {
                            mentionsNotificationBadge.setVisibility(View.GONE);
                            android.util.Log.d("MainChatFragment", "📢 Hidden mentions badge");
                        }
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error updating mentions notification badge", e);
        }
    }

    /**
     * Setup file upload functionality
     */
    private void setupFileUploadFunctionality() {
        try {
            android.util.Log.d("MainChatFragment", "📎 Setting up file upload functionality");
            
            // Setup attachment button click listener
            if (attachmentButton != null) {
                attachmentButton.setOnClickListener(v -> openFilePickerDialog());
                android.util.Log.d("MainChatFragment", "✅ Attachment button click listener set");
            } else {
                android.util.Log.e("MainChatFragment", "❌ Attachment button is null!");
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up file upload functionality", e);
        }
    }
    
    /**
     * Open file picker dialog to select files for upload
     */
    private void openFilePickerDialog() {
        try {
            android.util.Log.d("MainChatFragment", "📎 Opening file picker dialog");
            
            if (isUploading) {
                Toast.makeText(getContext(), "File upload in progress, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create intent to pick files
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all file types
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // Single file selection for now
            
            // Add MIME type filters for common file types
            String[] mimeTypes = {
                "image/*", // Images
                "video/*", // Videos
                "audio/*", // Audio files
                "application/pdf", // PDF files
                "text/*", // Text files
                "application/msword", // Word documents
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // Word .docx
                "application/vnd.ms-excel", // Excel files
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Excel .xlsx
                "application/zip", // ZIP files
                "application/x-rar-compressed" // RAR files
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            
            startActivityForResult(Intent.createChooser(intent, "Select file to upload"), FILE_PICKER_REQUEST_CODE);
            
            android.util.Log.d("MainChatFragment", "✅ File picker dialog opened");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error opening file picker dialog", e);
            Toast.makeText(getContext(), "Failed to open file picker", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle file selection result from file picker
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                handleFileSelection(fileUri);
            } else {
                android.util.Log.w("MainChatFragment", "⚠️ No file selected");
                Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Handle selected file and validate it before upload
     */
    private void handleFileSelection(Uri fileUri) {
        try {
            android.util.Log.d("MainChatFragment", "📎 Handling file selection: " + fileUri.toString());
            
            if (getContext() == null) {
                android.util.Log.e("MainChatFragment", "❌ Context is null");
                return;
            }
            
            // Get file information
            FileInfo fileInfo = getFileInfo(fileUri);
            if (fileInfo == null) {
                Toast.makeText(getContext(), "Failed to read file information", Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("MainChatFragment", "📄 File info - Name: " + fileInfo.name + ", Size: " + fileInfo.size + " bytes, Type: " + fileInfo.mimeType);
            
            // Validate file
            String validationError = validateFile(fileInfo);
            if (validationError != null) {
                Toast.makeText(getContext(), validationError, Toast.LENGTH_LONG).show();
                return;
            }
            
            // Start file upload
            startFileUpload(fileUri, fileInfo);
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error handling file selection", e);
            Toast.makeText(getContext(), "Failed to process selected file", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get file information from URI
     */
    private FileInfo getFileInfo(Uri fileUri) {
        try {
            if (getContext() == null) return null;
            
            FileInfo fileInfo = new FileInfo();
            
            // Get file name and size using ContentResolver
            try (Cursor cursor = getContext().getContentResolver().query(fileUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Get file name
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileInfo.name = cursor.getString(nameIndex);
                    }
                    
                    // Get file size
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        fileInfo.size = cursor.getLong(sizeIndex);
                    }
                }
            }
            
            // Get MIME type
            fileInfo.mimeType = getContext().getContentResolver().getType(fileUri);
            if (fileInfo.mimeType == null) {
                // Fallback to extension-based MIME type detection
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                if (extension != null) {
                    fileInfo.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                }
            }
            
            // Set default values if not found
            if (fileInfo.name == null || fileInfo.name.isEmpty()) {
                fileInfo.name = "unknown_file";
            }
            if (fileInfo.mimeType == null) {
                fileInfo.mimeType = "application/octet-stream";
            }
            
            fileInfo.uri = fileUri;
            return fileInfo;
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error getting file info", e);
            return null;
        }
    }
    
    /**
     * Validate selected file against size and type restrictions
     */
    private String validateFile(FileInfo fileInfo) {
        // Check file size
        if (fileInfo.size > MAX_FILE_SIZE_BYTES) {
            return String.format("File size exceeds limit. Maximum allowed: %dMB, File size: %.1fMB", 
                MAX_FILE_SIZE_MB, fileInfo.size / (1024.0 * 1024.0));
        }
        
        // Check for empty files
        if (fileInfo.size <= 0) {
            return "Cannot upload empty files";
        }
        
        // Check file name
        if (fileInfo.name == null || fileInfo.name.trim().isEmpty()) {
            return "Invalid file name";
        }
        
        // Check for potentially dangerous file types (basic security)
        String fileName = fileInfo.name.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".scr", ".pif", ".com", ".jar"};
        for (String ext : dangerousExtensions) {
            if (fileName.endsWith(ext)) {
                return "File type not allowed for security reasons: " + ext;
            }
        }
        
        return null; // File is valid
    }
    
    /**
     * Start the file upload process
     */
    private void startFileUpload(Uri fileUri, FileInfo fileInfo) {
        try {
            android.util.Log.d("MainChatFragment", "🚀 Starting file upload: " + fileInfo.name);
            
            isUploading = true;
            
            // Show upload progress dialog
            showUploadProgressDialog(fileInfo);
            
            // Use actual file upload service
            uploadFileToServer(fileUri, fileInfo);
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error starting file upload", e);
            isUploading = false;
            Toast.makeText(getContext(), "Failed to start file upload", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show upload progress dialog
     */
    private void showUploadProgressDialog(FileInfo fileInfo) {
        try {
            if (getContext() == null) return;
            
            uploadProgressDialog = new com.cosmic.gatherly.ui.components.FileUploadProgressDialog(getContext());
            uploadProgressDialog.setFileName(fileInfo.name);
            uploadProgressDialog.setFileSize(formatFileSize(fileInfo.size));
            uploadProgressDialog.setOnCancelListener(() -> {
                android.util.Log.d("MainChatFragment", "❌ File upload cancelled by user");
                cancelFileUpload();
            });
            uploadProgressDialog.show();
            
            android.util.Log.d("MainChatFragment", "📊 Upload progress dialog shown");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error showing upload progress dialog", e);
        }
    }
    
    /**
     * Upload file to server using FileUploadService
     */
    private void uploadFileToServer(Uri fileUri, FileInfo fileInfo) {
        try {
            android.util.Log.d("MainChatFragment", "📤 Uploading file to server...");
            
            if (getContext() == null) {
                onFileUploadError("Context is null");
                return;
            }
            
            // Create file upload service
            com.cosmic.gatherly.data.service.FileUploadService uploadService = 
                new com.cosmic.gatherly.data.service.FileUploadService(getContext());
            
            // Upload file with callback
            uploadService.uploadFile(fileUri, currentChannelId, new com.cosmic.gatherly.data.service.FileUploadService.FileUploadCallback() {
                @Override
                public void onSuccess(com.cosmic.gatherly.data.model.FileAttachment attachment) {
                    android.util.Log.d("MainChatFragment", "✅ File upload successful: " + attachment.getOriginalFileName());
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            onFileUploadSuccess(attachment);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("MainChatFragment", "❌ File upload failed: " + error);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            onFileUploadError(error);
                        });
                    }
                }
                
                @Override
                public void onProgress(int progress) {
                    android.util.Log.d("MainChatFragment", "📊 Upload progress: " + progress + "%");
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (uploadProgressDialog != null) {
                                uploadProgressDialog.setProgress(progress);
                            }
                        });
                    }
                }
            });
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error uploading file to server", e);
            onFileUploadError("Upload failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle successful file upload
     */
    private void onFileUploadSuccess(com.cosmic.gatherly.data.model.FileAttachment fileAttachment) {
        try {
            android.util.Log.d("MainChatFragment", "✅ File upload successful: " + fileAttachment.getOriginalFileName());
            
            isUploading = false;
            
            // Hide progress dialog
            if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
                uploadProgressDialog.dismiss();
            }
            
            // Add file attachment message to chat
            addFileAttachmentMessage(fileAttachment);
            
            // Show success message
            Toast.makeText(getContext(), "File uploaded successfully: " + fileAttachment.getOriginalFileName(), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error handling upload success", e);
        }
    }
    
    /**
     * Handle file upload error
     */
    private void onFileUploadError(String errorMessage) {
        try {
            android.util.Log.e("MainChatFragment", "❌ File upload error: " + errorMessage);
            
            isUploading = false;
            
            // Hide progress dialog
            if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
                uploadProgressDialog.dismiss();
            }
            
            // Show error message
            Toast.makeText(getContext(), "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error handling upload error", e);
        }
    }
    
    /**
     * Cancel ongoing file upload
     */
    private void cancelFileUpload() {
        try {
            android.util.Log.d("MainChatFragment", "❌ Cancelling file upload");
            
            isUploading = false;
            
            // Hide progress dialog
            if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
                uploadProgressDialog.dismiss();
            }
            
            Toast.makeText(getContext(), "File upload cancelled", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error cancelling upload", e);
        }
    }
    
    /**
     * Add file attachment message to chat
     */
    private void addFileAttachmentMessage(com.cosmic.gatherly.data.model.FileAttachment fileAttachment) {
        try {
            android.util.Log.d("MainChatFragment", "📎 Adding file attachment message: " + fileAttachment.getOriginalFileName());
            
            // Create attachment message content
            String attachmentMessage = String.format("📎 Uploaded file: %s (%s)", 
                fileAttachment.getOriginalFileName(), fileAttachment.getFormattedFileSize());
            
            // Add message to chat
            addNewMessage("You", attachmentMessage);
            
            // Add to search index
            addMessageToSearchIndex("You", attachmentMessage);
            
            android.util.Log.d("MainChatFragment", "✅ File attachment message added");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error adding file attachment message", e);
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * File information holder class
     */
    private static class FileInfo {
        String name;
        long size;
        String mimeType;
        Uri uri;
    }

    /**
     * Setup keyboard shortcuts for search (Ctrl+K)
     */
    private void setupKeyboardShortcuts() {
        try {
            View view = getView();
            if (view != null) {
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                view.setOnKeyListener((v, keyCode, event) -> {
                    // Check for Ctrl+K combination
                    if (event.getAction() == KeyEvent.ACTION_DOWN && 
                        event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_K) {
                        android.util.Log.d("MainChatFragment", "⌨️ Ctrl+K pressed - opening search");
                        openSearchDialog();
                        return true;
                    }
                    return false;
                });
                android.util.Log.d("MainChatFragment", "✅ Keyboard shortcuts setup completed");
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error setting up keyboard shortcuts", e);
        }
    }
    
    /**
     * Open the search dialog
     */
    private void openSearchDialog() {
        try {
            android.util.Log.d("MainChatFragment", "🔍 Opening search dialog");
            
            if (getContext() == null) {
                android.util.Log.e("MainChatFragment", "❌ Context is null, cannot open search dialog");
                return;
            }
            
            // Create and configure search dialog
            searchDialog = new SearchDialog(getContext());
            searchDialog.setCurrentChannel(currentChannelId, currentChannelName);
            
            // Set search result selection listener
            searchDialog.setOnSearchResultSelectedListener(result -> {
                android.util.Log.d("MainChatFragment", "🎯 Search result selected: " + result.getContent());
                
                // Switch to the channel containing the message if different
                if (!result.getChannelId().equals(currentChannelId)) {
                    switchToChannel(result.getChannelName());
                }
                
                // Optionally scroll to the specific message or highlight it
                // This would require additional implementation to find and highlight the message
                android.widget.Toast.makeText(getContext(), 
                    "Found in #" + result.getChannelName() + ": " + result.getContent(), 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
            
            // Show the dialog
            searchDialog.show();
            
            android.util.Log.d("MainChatFragment", "✅ Search dialog opened successfully");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error opening search dialog", e);
            android.widget.Toast.makeText(getContext(), "Failed to open search", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Add a new message to the search index when sent
     */
    private void addMessageToSearchIndex(String username, String message) {
        try {
            if (searchManager != null) {
                SearchResult searchResult = new SearchResult(
                    java.util.UUID.randomUUID().toString(),
                    message,
                    username,
                    currentChannelName,
                    currentChannelId,
                    new java.util.Date(),
                    R.drawable.ic_person,
                    0xFF4ECDC4
                );
                searchManager.addMessage(searchResult);
                android.util.Log.d("MainChatFragment", "📝 Added message to search index: " + message);
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error adding message to search index", e);
        }
    }
    
    /**
     * Parse mentions from a new message and add them to the mentions manager
     */
    private void parseMentionsFromMessage(String messageContent, String authorUsername) {
        try {
            if (mentionsManager == null) {
                android.util.Log.w("MainChatFragment", "⚠️ MentionsManager is null, cannot parse mentions");
                return;
            }
            
            // Generate a mock message ID
            String messageId = java.util.UUID.randomUUID().toString();
            
            // Get mock author ID based on username
            String authorId = getMockUserIdByUsername(authorUsername);
            
            // Parse mentions from message content
            java.util.List<com.cosmic.gatherly.data.model.Mention> mentions = 
                mentionsManager.parseMentionsFromMessage(
                    messageContent, 
                    messageId, 
                    authorId, 
                    currentChannelId, 
                    currentChannelName
                );
            
            // Add mentions to manager
            if (!mentions.isEmpty()) {
                mentionsManager.addMentions(mentions);
                android.util.Log.d("MainChatFragment", "📢 Added " + mentions.size() + " mentions from message");
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error parsing mentions from message", e);
        }
    }
    
    /**
     * Get mock user ID by username (in real app, this would query the database)
     */
    private String getMockUserIdByUsername(String username) {
        if (username == null) return "unknown_user";
        
        switch (username.toLowerCase()) {
            case "cosmicexplorer":
                return "user_cosmic_explorer";
            case "stardust":
                return "user_stardust";
            case "nebulaninja":
                return "user_nebula_ninja";
            case "galaxygamer":
                return "user_galaxy_gamer";
            case "you":
                return "current_user";
            default:
                return "user_" + username.toLowerCase().replace(" ", "_");
        }
    }

    /**
     * Toggle the members sidebar visibility
     */
    private void toggleMembersSidebar() {
        try {
            android.util.Log.d("MainChatFragment", "👥 Toggling members sidebar");
            
            showMembersSidebar = !showMembersSidebar;
            
            if (showMembersSidebar) {
                openMembersSidebar();
            } else {
                closeMembersSidebar();
            }
            
            // Update button appearance
            updateMembersButtonAppearance();
            
            android.util.Log.d("MainChatFragment", "✅ Members sidebar toggled: " + (showMembersSidebar ? "shown" : "hidden"));
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error toggling members sidebar", e);
        }
    }
    
    /**
     * Open the members sidebar
     */
    private void openMembersSidebar() {
        try {
            android.util.Log.d("MainChatFragment", "👥 Opening members sidebar");
            
            if (getContext() == null) {
                android.util.Log.e("MainChatFragment", "❌ Context is null, cannot open members sidebar");
                return;
            }
            
            // Create and configure members dialog
            membersDialog = new com.cosmic.gatherly.ui.components.MembersDialog(getContext());
            membersDialog.setCurrentChannel(currentChannelId, currentChannelName);
            
            // Set member selection listener
            membersDialog.setOnMembersDialogListener(new com.cosmic.gatherly.ui.components.MembersDialog.OnMembersDialogListener() {
                @Override
                public void onMemberSelected(com.cosmic.gatherly.data.model.Member member) {
                    android.util.Log.d("MainChatFragment", "🎯 Member selected: " + member.getName());
                    android.widget.Toast.makeText(getContext(), 
                        "Selected member: " + member.getName(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVoiceControlAction(String action) {
                    android.util.Log.d("MainChatFragment", "🎤 Voice control action: " + action);
                    android.widget.Toast.makeText(getContext(), 
                        "Voice action: " + action, 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });
            
            // Show the dialog
            membersDialog.show();
            
            android.util.Log.d("MainChatFragment", "✅ Members sidebar opened");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error opening members sidebar", e);
        }
    }
    
    /**
     * Close the members sidebar
     */
    private void closeMembersSidebar() {
        try {
            android.util.Log.d("MainChatFragment", "👥 Closing members sidebar");
            
            // Close the members dialog if it's open
            if (membersDialog != null && membersDialog.isShowing()) {
                membersDialog.dismiss();
            }
            
            android.util.Log.d("MainChatFragment", "✅ Members sidebar closed");
            
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error closing members sidebar", e);
        }
    }
    
    /**
     * Update the members button appearance based on sidebar state
     */
    private void updateMembersButtonAppearance() {
        try {
            if (membersButton != null && getContext() != null) {
                if (showMembersSidebar) {
                    // Highlight button when sidebar is open
                    membersButton.setColorFilter(getContext().getColor(R.color.cosmic_accent));
                    membersButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getContext().getColor(R.color.cosmic_surface)));
                } else {
                    // Reset button appearance when sidebar is closed
                    membersButton.setColorFilter(getContext().getColor(R.color.cosmic_gray));
                    membersButton.setBackgroundTintList(null);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainChatFragment", "❌ Error updating members button appearance", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainCallback = null;
        
        // Clean up search dialog
        if (searchDialog != null && searchDialog.isShowing()) {
            searchDialog.dismiss();
        }
        
        // Clean up members dialog
        if (membersDialog != null && membersDialog.isShowing()) {
            membersDialog.dismiss();
        }
        
        // Clean up mentions dialog
        if (mentionsDialog != null && mentionsDialog.isShowing()) {
            mentionsDialog.dismiss();
        }
        
        // Clean up mentions manager listener
        if (mentionsManager != null) {
            // Note: In a real app, you'd want to remove the specific listener
            // For now, we'll leave it as the manager handles cleanup
        }
        
        // Clean up file upload dialog
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
        
        // Cancel any ongoing uploads
        if (isUploading) {
            isUploading = false;
        }
    }
}