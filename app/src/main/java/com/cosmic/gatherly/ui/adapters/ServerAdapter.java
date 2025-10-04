package com.cosmic.gatherly.ui.adapters;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cosmic.gatherly.R;
import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
    
    private List<ServerItem> servers;
    private OnServerClickListener listener;
    private String selectedServerId = null;
    
    public interface OnServerClickListener {
        void onServerClick(ServerItem server);
    }
    
    public ServerAdapter(List<ServerItem> servers, OnServerClickListener listener) {
        this.servers = servers;
        this.listener = listener;
    }
    
    public void setSelectedServerId(String serverId) {
        String previousSelectedId = this.selectedServerId;
        this.selectedServerId = serverId;
        
        // Notify changes for previous and current selected items
        for (int i = 0; i < servers.size(); i++) {
            ServerItem server = servers.get(i);
            if (server.getId().equals(previousSelectedId) || server.getId().equals(serverId)) {
                notifyItemChanged(i);
            }
        }
    }
    
    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        ServerItem server = servers.get(position);
        holder.bind(server);
    }
    
    @Override
    public int getItemCount() {
        return servers.size();
    }
    
    class ServerViewHolder extends RecyclerView.ViewHolder {
        private ImageView serverIcon;
        private androidx.cardview.widget.CardView serverCard;
        private View serverIndicator;
        
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            serverIcon = itemView.findViewById(R.id.serverIcon);
            serverCard = itemView.findViewById(R.id.serverCard);
            serverIndicator = itemView.findViewById(R.id.serverIndicator);
            
            // Set click listener on both the item view and the card
            View.OnClickListener clickListener = v -> {
                android.util.Log.d("ServerAdapter", "🎯 Server item clicked!");
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < servers.size()) {
                        ServerItem server = servers.get(position);
                        android.util.Log.d("ServerAdapter", "🚀 Calling listener for server: " + server.getName());
                        listener.onServerClick(server);
                    }
                } else {
                    android.util.Log.e("ServerAdapter", "❌ Listener is null!");
                }
            };
            
            itemView.setOnClickListener(clickListener);
            if (serverCard != null) {
                serverCard.setOnClickListener(clickListener);
            }
        }
        
        public void bind(ServerItem server) {
            serverIcon.setImageResource(server.getIconResource());
            if (serverCard != null) {
                serverCard.setCardBackgroundColor(server.getBackgroundColor());
            }
            
            // Handle server indicator visibility with smooth animation
            boolean isSelected = server.getId().equals(selectedServerId);
            animateIndicatorVisibility(isSelected);
        }
        
        private void animateIndicatorVisibility(boolean isVisible) {
            if (serverIndicator == null) return;
            
            float targetAlpha = isVisible ? 1.0f : 0.0f;
            int targetVisibility = isVisible ? View.VISIBLE : View.GONE;
            
            // Only animate if the visibility is actually changing
            if ((serverIndicator.getAlpha() == 1.0f && isVisible) || 
                (serverIndicator.getAlpha() == 0.0f && !isVisible)) {
                return;
            }
            
            // Set visibility to VISIBLE before starting animation if showing
            if (isVisible) {
                serverIndicator.setVisibility(View.VISIBLE);
            }
            
            // Create smooth fade animation
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(serverIndicator, "alpha", targetAlpha);
            alphaAnimator.setDuration(200); // 200ms smooth transition
            alphaAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Set visibility to GONE after fade out animation completes
                    if (!isVisible) {
                        serverIndicator.setVisibility(View.GONE);
                    }
                }
            });
            alphaAnimator.start();
        }
    }
    
    public static class ServerItem {
        private String id;
        private String name;
        private int iconResource;
        private int backgroundColor;
        
        public ServerItem(String id, String name, int iconResource, int backgroundColor) {
            this.id = id;
            this.name = name;
            this.iconResource = iconResource;
            this.backgroundColor = backgroundColor;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public int getIconResource() { return iconResource; }
        public int getBackgroundColor() { return backgroundColor; }
    }
}