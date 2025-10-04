package com.cosmic.gatherly.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.VoiceChannelManager;
import com.cosmic.gatherly.data.model.VoiceChannelState;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHANNEL = 1;
    
    private List<ChannelItem> items;
    private OnChannelClickListener listener;
    private VoiceChannelManager voiceChannelManager;
    
    public interface OnChannelClickListener {
        void onTextChannelClick(ChannelItem channel);
        void onVoiceChannelClick(ChannelItem channel);
        void onChannelSettingsClick(String categoryName);
    }
    
    public ChannelAdapter(List<ChannelItem> items, OnChannelClickListener listener, VoiceChannelManager voiceChannelManager) {
        this.items = items;
        this.listener = listener;
        this.voiceChannelManager = voiceChannelManager;
    }
    
    /**
     * Updates the selected text channel and refreshes the UI
     * Voice channels are handled separately and don't affect text channel selection
     * @param selectedChannelName The name of the text channel to select
     */
    public void setSelectedTextChannel(String selectedChannelName) {
        // Clear previous text channel selection only
        for (ChannelItem item : items) {
            if (!item.isHeader() && item.getType() == ChannelItem.ChannelType.TEXT) {
                item.setSelected(false);
            }
        }
        
        // Set new text channel selection
        for (ChannelItem item : items) {
            if (!item.isHeader() && 
                item.getType() == ChannelItem.ChannelType.TEXT && 
                item.getName().equals(selectedChannelName)) {
                item.setSelected(true);
                break;
            }
        }
        
        // Notify adapter to refresh views
        notifyDataSetChanged();
    }

    /**
     * Updates voice channel connection states based on VoiceChannelManager
     */
    public void updateVoiceChannelStates() {
        if (voiceChannelManager == null) return;
        
        boolean hasChanges = false;
        String currentVoiceChannelId = voiceChannelManager.getCurrentVoiceChannelId();
        
        for (ChannelItem item : items) {
            if (!item.isHeader() && item.getType() == ChannelItem.ChannelType.VOICE) {
                boolean wasConnected = item.isVoiceConnected();
                boolean isConnected = voiceChannelManager.isCurrentVoiceChannel(item.getId());
                
                if (wasConnected != isConnected) {
                    item.setVoiceConnected(isConnected);
                    hasChanges = true;
                }
            }
        }
        
        if (hasChanges) {
            notifyDataSetChanged();
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader() ? TYPE_HEADER : TYPE_CHANNEL;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel, parent, false);
            return new ChannelViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChannelItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof ChannelViewHolder) {
            ((ChannelViewHolder) holder).bind(item);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerText;
        private ImageView settingsIcon;
        
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
            settingsIcon = itemView.findViewById(R.id.settingsIcon);
            
            // Setup settings button click listener
            if (settingsIcon != null) {
                settingsIcon.setOnClickListener(v -> {
                    if (listener != null) {
                        ChannelItem item = items.get(getAdapterPosition());
                        listener.onChannelSettingsClick(item.getName());
                    }
                });
            }
        }
        
        public void bind(ChannelItem item) {
            headerText.setText(item.getName());
        }
    }
    
    class ChannelViewHolder extends RecyclerView.ViewHolder {
        private ImageView channelIcon;
        private TextView channelName;
        private View selectionBorder;
        
        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            channelIcon = itemView.findViewById(R.id.channelIcon);
            channelName = itemView.findViewById(R.id.channelName);
            selectionBorder = itemView.findViewById(R.id.selectionBorder);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && !items.get(getAdapterPosition()).isHeader()) {
                    ChannelItem item = items.get(getAdapterPosition());
                    
                    // Handle different channel types with separate click handlers
                    if (item.getType() == ChannelItem.ChannelType.VOICE) {
                        listener.onVoiceChannelClick(item);
                    } else {
                        listener.onTextChannelClick(item);
                    }
                }
            });
        }
        
        public void bind(ChannelItem item) {
            channelIcon.setImageResource(item.getIconResource());
            channelName.setText(item.getName());
            
            // Handle different styling for text channels vs voice channels
            if (item.getType() == ChannelItem.ChannelType.VOICE) {
                // Voice channel styling - show connection state instead of selection
                if (item.isVoiceConnected()) {
                    // Show connection indicator for voice channels
                    selectionBorder.setVisibility(View.VISIBLE);
                    itemView.setBackgroundColor(itemView.getContext().getColor(R.color.cosmic_surface));
                    channelName.setTextColor(itemView.getContext().getColor(R.color.cosmic_text_primary));
                    channelIcon.setColorFilter(itemView.getContext().getColor(R.color.cosmic_green));
                } else {
                    // Voice channel not connected
                    selectionBorder.setVisibility(View.GONE);
                    itemView.setBackgroundColor(itemView.getContext().getColor(android.R.color.transparent));
                    channelName.setTextColor(itemView.getContext().getColor(R.color.cosmic_text_secondary));
                    channelIcon.setColorFilter(itemView.getContext().getColor(R.color.cosmic_gray));
                }
            } else {
                // Text channel styling - show selection state
                if (item.isSelected()) {
                    // Show selection border
                    selectionBorder.setVisibility(View.VISIBLE);
                    
                    // Set background color for selected state
                    itemView.setBackgroundColor(itemView.getContext().getColor(R.color.cosmic_surface));
                    
                    // Update text color for selected state
                    channelName.setTextColor(itemView.getContext().getColor(R.color.cosmic_text_primary));
                    
                    // Update icon tint for selected state
                    channelIcon.setColorFilter(itemView.getContext().getColor(R.color.cosmic_blue));
                } else {
                    // Hide selection border
                    selectionBorder.setVisibility(View.GONE);
                    
                    // Reset background color
                    itemView.setBackgroundColor(itemView.getContext().getColor(android.R.color.transparent));
                    
                    // Reset text color for unselected state
                    channelName.setTextColor(itemView.getContext().getColor(R.color.cosmic_text_secondary));
                    
                    // Reset icon tint for unselected state
                    channelIcon.setColorFilter(itemView.getContext().getColor(R.color.cosmic_gray));
                }
            }
        }
    }
    
    public static class ChannelItem {
        private String id;
        private String name;
        private int iconResource;
        private boolean isHeader;
        private boolean isSelected;
        private boolean isVoiceConnected;
        private ChannelType type;
        
        public enum ChannelType {
            TEXT, VOICE
        }
        
        public ChannelItem(String name, boolean isHeader) {
            this.name = name;
            this.isHeader = isHeader;
        }
        
        public ChannelItem(String id, String name, int iconResource, ChannelType type) {
            this.id = id;
            this.name = name;
            this.iconResource = iconResource;
            this.type = type;
            this.isHeader = false;
            this.isVoiceConnected = false;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getIconResource() { return iconResource; }
        public boolean isHeader() { return isHeader; }
        public boolean isSelected() { return isSelected; }
        public boolean isVoiceConnected() { return isVoiceConnected; }
        public ChannelType getType() { return type; }
        
        // Setters
        public void setSelected(boolean selected) { this.isSelected = selected; }
        public void setVoiceConnected(boolean voiceConnected) { this.isVoiceConnected = voiceConnected; }
    }
}