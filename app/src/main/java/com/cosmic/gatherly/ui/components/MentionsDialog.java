package com.cosmic.gatherly.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.MentionsManager;
import com.cosmic.gatherly.data.model.Mention;
import com.cosmic.gatherly.ui.adapters.MentionAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Dialog for displaying user mentions
 */
public class MentionsDialog extends Dialog implements 
    MentionAdapter.OnMentionClickListener, 
    MentionsManager.OnMentionsUpdateListener {
    
    private ImageView closeMentionsButton;
    private TextView unreadCountText;
    private MaterialButton markAllReadButton;
    private MaterialButton clearAllButton;
    private RecyclerView mentionsRecyclerView;
    private LinearLayout emptyStateLayout;
    
    private MentionAdapter mentionAdapter;
    private MentionsManager mentionsManager;
    private OnMentionsDialogListener listener;
    
    public interface OnMentionsDialogListener {
        void onMentionSelected(Mention mention);
        void onJumpToMessage(Mention mention);
    }

    public MentionsDialog(@NonNull Context context) {
        super(context, R.style.Theme_Gatherly_Dialog_FullScreen);
        this.mentionsManager = MentionsManager.getInstance(context);
    }

    public void setOnMentionsDialogListener(OnMentionsDialogListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make dialog slide in from right
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_mentions);
        
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.85);
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.END;
            window.setAttributes(params);
            
            // Add slide animation
            window.setWindowAnimations(R.style.SlideInFromRight);
        }
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadMentions();
        
        // Register for mentions updates
        mentionsManager.addListener(this);
    }

    private void initializeViews() {
        closeMentionsButton = findViewById(R.id.closeMentionsButton);
        unreadCountText = findViewById(R.id.unreadCountText);
        markAllReadButton = findViewById(R.id.markAllReadButton);
        clearAllButton = findViewById(R.id.clearAllButton);
        mentionsRecyclerView = findViewById(R.id.mentionsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        try {
            mentionAdapter = new MentionAdapter(getContext(), mentionsManager.getAllMentions());
            mentionAdapter.setOnMentionClickListener(this);
            
            mentionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mentionsRecyclerView.setAdapter(mentionAdapter);
            
            android.util.Log.d("MentionsDialog", "RecyclerView setup completed");
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error setting up RecyclerView", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Close button
            closeMentionsButton.setOnClickListener(v -> dismiss());
            
            // Mark all read button
            markAllReadButton.setOnClickListener(v -> {
                android.util.Log.d("MentionsDialog", "Mark all read clicked");
                mentionsManager.markAllMentionsAsRead();
                android.widget.Toast.makeText(getContext(), "All mentions marked as read", android.widget.Toast.LENGTH_SHORT).show();
            });
            
            // Clear all button
            clearAllButton.setOnClickListener(v -> {
                android.util.Log.d("MentionsDialog", "Clear all clicked");
                
                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Clear All Mentions")
                    .setMessage("Are you sure you want to clear all mentions? This action cannot be undone.")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        mentionsManager.clearAllMentions();
                        android.widget.Toast.makeText(getContext(), "All mentions cleared", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
            
            android.util.Log.d("MentionsDialog", "Click listeners setup completed");
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error setting up click listeners", e);
        }
    }

    private void loadMentions() {
        try {
            List<Mention> mentions = mentionsManager.getAllMentions();
            updateMentionsDisplay(mentions);
            updateUnreadCount(mentionsManager.getUnreadMentionsCount());
            
            android.util.Log.d("MentionsDialog", "Loaded " + mentions.size() + " mentions");
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error loading mentions", e);
        }
    }

    private void updateMentionsDisplay(List<Mention> mentions) {
        try {
            if (mentions.isEmpty()) {
                // Show empty state
                mentionsRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
                markAllReadButton.setEnabled(false);
                clearAllButton.setEnabled(false);
            } else {
                // Show mentions list
                mentionsRecyclerView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                markAllReadButton.setEnabled(true);
                clearAllButton.setEnabled(true);
                
                // Update adapter
                if (mentionAdapter != null) {
                    mentionAdapter.updateMentions(mentions);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error updating mentions display", e);
        }
    }

    private void updateUnreadCount(int unreadCount) {
        try {
            if (unreadCount > 0) {
                unreadCountText.setText(String.valueOf(unreadCount));
                unreadCountText.setVisibility(View.VISIBLE);
            } else {
                unreadCountText.setVisibility(View.GONE);
            }
            
            // Update mark all read button state
            markAllReadButton.setEnabled(unreadCount > 0);
            
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error updating unread count", e);
        }
    }

    // MentionAdapter.OnMentionClickListener implementation
    @Override
    public void onMentionClick(Mention mention) {
        try {
            android.util.Log.d("MentionsDialog", "Mention clicked: " + mention.getId());
            
            // Mark mention as read
            mentionsManager.markMentionAsRead(mention.getId());
            
            // Notify listener
            if (listener != null) {
                listener.onMentionSelected(mention);
            }
            
            // Optionally dismiss dialog
            dismiss();
            
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error handling mention click", e);
        }
    }

    @Override
    public void onJumpToMessage(Mention mention) {
        try {
            android.util.Log.d("MentionsDialog", "Jump to message clicked: " + mention.getMessageId());
            
            // Mark mention as read
            mentionsManager.markMentionAsRead(mention.getId());
            
            // Notify listener
            if (listener != null) {
                listener.onJumpToMessage(mention);
            }
            
            // Dismiss dialog
            dismiss();
            
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error handling jump to message", e);
        }
    }

    // MentionsManager.OnMentionsUpdateListener implementation
    @Override
    public void onMentionsUpdated(List<Mention> mentions) {
        try {
            // Update UI on main thread
            if (getContext() instanceof android.app.Activity) {
                ((android.app.Activity) getContext()).runOnUiThread(() -> {
                    updateMentionsDisplay(mentions);
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error handling mentions update", e);
        }
    }

    @Override
    public void onUnreadCountChanged(int unreadCount) {
        try {
            // Update UI on main thread
            if (getContext() instanceof android.app.Activity) {
                ((android.app.Activity) getContext()).runOnUiThread(() -> {
                    updateUnreadCount(unreadCount);
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error handling unread count change", e);
        }
    }

    @Override
    public void dismiss() {
        try {
            // Unregister from mentions updates
            if (mentionsManager != null) {
                mentionsManager.removeListener(this);
            }
            
            super.dismiss();
        } catch (Exception e) {
            android.util.Log.e("MentionsDialog", "Error dismissing dialog", e);
        }
    }
}