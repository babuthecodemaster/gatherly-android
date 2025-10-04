package com.cosmic.gatherly.ui.adapters;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.Mention;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for displaying mentions in a RecyclerView
 */
public class MentionAdapter extends RecyclerView.Adapter<MentionAdapter.MentionViewHolder> {
    
    private Context context;
    private List<Mention> mentions;
    private OnMentionClickListener listener;

    public interface OnMentionClickListener {
        void onMentionClick(Mention mention);
        void onJumpToMessage(Mention mention);
    }

    public MentionAdapter(Context context, List<Mention> mentions) {
        this.context = context;
        this.mentions = mentions != null ? mentions : new ArrayList<>();
    }

    public void setOnMentionClickListener(OnMentionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MentionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mention, parent, false);
        return new MentionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MentionViewHolder holder, int position) {
        Mention mention = mentions.get(position);
        holder.bind(mention);
    }

    @Override
    public int getItemCount() {
        return mentions.size();
    }

    /**
     * Update the mentions list
     */
    public void updateMentions(List<Mention> newMentions) {
        this.mentions.clear();
        if (newMentions != null) {
            this.mentions.addAll(newMentions);
        }
        notifyDataSetChanged();
    }

    /**
     * Add a new mention to the list
     */
    public void addMention(Mention mention) {
        mentions.add(0, mention); // Add to beginning
        notifyItemInserted(0);
    }

    /**
     * Remove a mention from the list
     */
    public void removeMention(int position) {
        if (position >= 0 && position < mentions.size()) {
            mentions.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all mentions
     */
    public void clearMentions() {
        int size = mentions.size();
        mentions.clear();
        notifyItemRangeRemoved(0, size);
    }

    class MentionViewHolder extends RecyclerView.ViewHolder {
        private View unreadIndicator;
        private TextView channelNameText;
        private TextView timeText;
        private ImageView authorAvatar;
        private TextView authorNameText;
        private TextView messageContentText;
        private ImageView jumpToMessageButton;

        public MentionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            channelNameText = itemView.findViewById(R.id.channelNameText);
            timeText = itemView.findViewById(R.id.timeText);
            authorAvatar = itemView.findViewById(R.id.authorAvatar);
            authorNameText = itemView.findViewById(R.id.authorNameText);
            messageContentText = itemView.findViewById(R.id.messageContentText);
            jumpToMessageButton = itemView.findViewById(R.id.jumpToMessageButton);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMentionClick(mentions.get(position));
                }
            });

            jumpToMessageButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onJumpToMessage(mentions.get(position));
                }
            });
        }

        public void bind(Mention mention) {
            try {
                // Show/hide unread indicator
                unreadIndicator.setVisibility(mention.isRead() ? View.INVISIBLE : View.VISIBLE);

                // Set channel name
                channelNameText.setText(mention.getChannelName());

                // Set time
                timeText.setText(mention.getFormattedTime());

                // Set author name (mock data for now)
                String authorName = getMockAuthorName(mention.getMentionedByUserId());
                authorNameText.setText(authorName);

                // Set author avatar color based on user ID
                int avatarColor = getAvatarColor(mention.getMentionedByUserId());
                authorAvatar.setColorFilter(avatarColor);

                // Set message content with highlighted mentions
                SpannableString spannableContent = highlightMentions(mention.getMessageContent());
                messageContentText.setText(spannableContent);

                // Update item background based on read status
                if (mention.isRead()) {
                    itemView.setAlpha(0.7f);
                } else {
                    itemView.setAlpha(1.0f);
                }

            } catch (Exception e) {
                android.util.Log.e("MentionAdapter", "Error binding mention", e);
            }
        }

        /**
         * Highlight @mentions in the message content
         */
        private SpannableString highlightMentions(String content) {
            SpannableString spannable = new SpannableString(content);
            
            try {
                Pattern mentionPattern = Pattern.compile("@(\\w+)");
                Matcher matcher = mentionPattern.matcher(content);
                
                int accentColor = ContextCompat.getColor(context, R.color.cosmic_accent);
                
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    spannable.setSpan(new ForegroundColorSpan(accentColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Exception e) {
                android.util.Log.e("MentionAdapter", "Error highlighting mentions", e);
            }
            
            return spannable;
        }

        /**
         * Get mock author name based on user ID
         */
        private String getMockAuthorName(String userId) {
            if (userId == null) return "Unknown User";
            
            switch (userId) {
                case "user_cosmic_explorer":
                    return "CosmicExplorer";
                case "user_stardust":
                    return "StarDust";
                case "user_nebula_ninja":
                    return "NebulaNinja";
                case "user_galaxy_gamer":
                    return "GalaxyGamer";
                default:
                    return userId.replace("user_", "").replace("_", "");
            }
        }

        /**
         * Get avatar color based on user ID
         */
        private int getAvatarColor(String userId) {
            if (userId == null) return ContextCompat.getColor(context, R.color.cosmic_gray);
            
            // Generate consistent colors based on user ID hash
            int hash = userId.hashCode();
            int[] colors = {
                ContextCompat.getColor(context, R.color.cosmic_accent),
                0xFF00D166, // Green
                0xFF5865F2, // Blue
                0xFFEB459E, // Pink
                0xFFFEE75C, // Yellow
                0xFF9B59B6, // Purple
                0xFFE67E22, // Orange
                0xFF1ABC9C  // Teal
            };
            
            return colors[Math.abs(hash) % colors.length];
        }
    }
}