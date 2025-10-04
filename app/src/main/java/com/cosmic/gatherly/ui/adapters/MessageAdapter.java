package com.cosmic.gatherly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.service.FileDownloadManager;
import com.cosmic.gatherly.data.util.Logger;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    
    private List<MessageItem> messages;
    private Context context;
    private FileDownloadManager downloadManager;
    
    public MessageAdapter(Context context, List<MessageItem> messages) {
        this.context = context;
        this.messages = messages;
        this.downloadManager = new FileDownloadManager(context);
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageItem message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    /**
     * Add a new message to the chat
     */
    public void addMessage(MessageItem message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    /**
     * Clear all messages
     */
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView userAvatar;
        private TextView username;
        private TextView timestamp;
        private TextView messageText;
        private TextView reactions;
        private LinearLayout attachmentsContainer;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            username = itemView.findViewById(R.id.username);
            timestamp = itemView.findViewById(R.id.timestamp);
            messageText = itemView.findViewById(R.id.messageText);
            reactions = itemView.findViewById(R.id.reactions);
            attachmentsContainer = itemView.findViewById(R.id.attachmentsContainer);
        }
        
        public void bind(MessageItem message) {
            userAvatar.setImageResource(message.getAvatarResource());
            username.setText(message.getUsername());
            username.setTextColor(message.getUsernameColor());
            timestamp.setText(message.getTimestamp());
            messageText.setText(message.getMessage());
            
            // Handle file attachments
            bindFileAttachments(message.getFileAttachments());
            
            androidx.cardview.widget.CardView reactionsCard = itemView.findViewById(R.id.reactionsCard);
            if (message.getReactions() != null && !message.getReactions().isEmpty()) {
                reactions.setText(message.getReactions());
                if (reactionsCard != null) {
                    reactionsCard.setVisibility(View.VISIBLE);
                } else {
                    reactions.setVisibility(View.VISIBLE);
                }
            } else {
                if (reactionsCard != null) {
                    reactionsCard.setVisibility(View.GONE);
                } else {
                    reactions.setVisibility(View.GONE);
                }
            }
        }
        
        private void bindFileAttachments(List<FileAttachment> attachments) {
            // Clear existing attachments
            attachmentsContainer.removeAllViews();
            
            if (attachments == null || attachments.isEmpty()) {
                attachmentsContainer.setVisibility(View.GONE);
                return;
            }
            
            attachmentsContainer.setVisibility(View.VISIBLE);
            
            for (FileAttachment attachment : attachments) {
                View attachmentView = createAttachmentView(attachment);
                if (attachmentView != null) {
                    attachmentsContainer.addView(attachmentView);
                }
            }
        }
        
        private View createAttachmentView(FileAttachment attachment) {
            LayoutInflater inflater = LayoutInflater.from(context);
            
            if (attachment.isImage()) {
                return createImageAttachmentView(inflater, attachment);
            } else {
                return createFileAttachmentView(inflater, attachment);
            }
        }
        
        private View createImageAttachmentView(LayoutInflater inflater, FileAttachment attachment) {
            View view = inflater.inflate(R.layout.item_image_attachment, attachmentsContainer, false);
            
            ImageView imagePreview = view.findViewById(R.id.imagePreview);
            TextView imageName = view.findViewById(R.id.imageName);
            TextView imageSize = view.findViewById(R.id.imageSize);
            ImageView downloadButton = view.findViewById(R.id.downloadButton);
            
            // Load image with Glide
            Glide.with(context)
                .load(attachment.getUrl())
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(imagePreview);
            
            imageName.setText(attachment.getOriginalFileName());
            imageSize.setText(attachment.getFormattedFileSize());
            
            // Set click listeners
            view.setOnClickListener(v -> openImageFullscreen(attachment));
            downloadButton.setOnClickListener(v -> downloadFile(attachment));
            
            return view;
        }
        
        private View createFileAttachmentView(LayoutInflater inflater, FileAttachment attachment) {
            View view = inflater.inflate(R.layout.item_file_attachment, attachmentsContainer, false);
            
            ImageView fileIcon = view.findViewById(R.id.fileIcon);
            TextView fileName = view.findViewById(R.id.fileName);
            TextView fileSize = view.findViewById(R.id.fileSize);
            ImageView downloadButton = view.findViewById(R.id.downloadButton);
            
            // Set appropriate icon based on file type
            int iconResource = getFileIcon(attachment.getFileType());
            fileIcon.setImageResource(iconResource);
            
            fileName.setText(attachment.getOriginalFileName());
            fileSize.setText(attachment.getFormattedFileSize());
            
            // Set click listeners
            view.setOnClickListener(v -> openFile(attachment));
            downloadButton.setOnClickListener(v -> downloadFile(attachment));
            
            return view;
        }
        
        private int getFileIcon(String fileType) {
            if (fileType == null) return R.drawable.ic_file;
            
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return R.drawable.ic_pdf;
                case "document":
                    return R.drawable.ic_document;
                case "video":
                    return R.drawable.ic_video;
                case "audio":
                    return R.drawable.ic_audio;
                case "text":
                    return R.drawable.ic_text;
                default:
                    return R.drawable.ic_file;
            }
        }
        
        private void openImageFullscreen(FileAttachment attachment) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(attachment.getUrl()), attachment.getMimeType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Logger.e(TAG, "Error opening image", e);
                Toast.makeText(context, "Cannot open image", Toast.LENGTH_SHORT).show();
            }
        }
        
        private void openFile(FileAttachment attachment) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(attachment.getUrl()), attachment.getMimeType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Logger.e(TAG, "Error opening file", e);
                Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show();
            }
        }
        
        private void downloadFile(FileAttachment attachment) {
            try {
                long downloadId = downloadManager.downloadFile(attachment);
                
                if (downloadId != -1) {
                    Toast.makeText(context, "Downloading " + attachment.getOriginalFileName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error downloading file", e);
                Toast.makeText(context, "Cannot download file", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public static class MessageItem {
        private String username;
        private String message;
        private String timestamp;
        private String reactions;
        private int avatarResource;
        private int usernameColor;
        private List<FileAttachment> fileAttachments;
        
        public MessageItem(String username, String message, String timestamp, int avatarResource, int usernameColor) {
            this.username = username;
            this.message = message;
            this.timestamp = timestamp;
            this.avatarResource = avatarResource;
            this.usernameColor = usernameColor;
        }
        
        public MessageItem(String username, String message, String timestamp, String reactions, int avatarResource, int usernameColor) {
            this(username, message, timestamp, avatarResource, usernameColor);
            this.reactions = reactions;
        }
        
        public MessageItem(String username, String message, String timestamp, int avatarResource, int usernameColor, List<FileAttachment> fileAttachments) {
            this(username, message, timestamp, avatarResource, usernameColor);
            this.fileAttachments = fileAttachments;
        }
        
        public MessageItem(String username, String message, String timestamp, String reactions, int avatarResource, int usernameColor, List<FileAttachment> fileAttachments) {
            this(username, message, timestamp, reactions, avatarResource, usernameColor);
            this.fileAttachments = fileAttachments;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public String getReactions() { return reactions; }
        public int getAvatarResource() { return avatarResource; }
        public int getUsernameColor() { return usernameColor; }
        public List<FileAttachment> getFileAttachments() { return fileAttachments; }
        
        // Setters
        public void setFileAttachments(List<FileAttachment> fileAttachments) {
            this.fileAttachments = fileAttachments;
        }
    }
}