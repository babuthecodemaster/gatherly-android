package com.cosmic.gatherly.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.SearchResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    
    private List<SearchResult> searchResults;
    private String searchQuery;
    private OnSearchResultClickListener clickListener;
    private Context context;

    public interface OnSearchResultClickListener {
        void onSearchResultClick(SearchResult result);
    }

    public SearchResultAdapter(Context context) {
        this.context = context;
        this.searchResults = new ArrayList<>();
        this.searchQuery = "";
    }

    public void setSearchResults(List<SearchResult> results, String query) {
        this.searchResults = results != null ? results : new ArrayList<>();
        this.searchQuery = query != null ? query.toLowerCase() : "";
        notifyDataSetChanged();
    }

    public void setOnSearchResultClickListener(OnSearchResultClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);
        holder.bind(result);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    class SearchResultViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorAvatar;
        private TextView authorName;
        private TextView messageTime;
        private TextView messageContent;
        private TextView channelName;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            authorAvatar = itemView.findViewById(R.id.authorAvatar);
            authorName = itemView.findViewById(R.id.authorName);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageContent = itemView.findViewById(R.id.messageContent);
            channelName = itemView.findViewById(R.id.channelName);

            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onSearchResultClick(searchResults.get(getAdapterPosition()));
                }
            });
        }

        public void bind(SearchResult result) {
            // Set author name
            authorName.setText(result.getAuthorName());
            
            // Set author avatar
            if (result.getAvatarResource() != 0) {
                authorAvatar.setImageResource(result.getAvatarResource());
            } else {
                authorAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Set author color if available
            if (result.getAuthorColor() != 0) {
                authorAvatar.setColorFilter(result.getAuthorColor());
            }

            // Set timestamp
            messageTime.setText(formatTimestamp(result.getTimestamp()));

            // Set message content with search highlighting
            messageContent.setText(highlightSearchTerm(result.getContent(), searchQuery));

            // Set channel name
            channelName.setText(result.getChannelName());
        }

        private String formatTimestamp(Date timestamp) {
            if (timestamp == null) {
                return context.getString(R.string.time_now);
            }

            long now = System.currentTimeMillis();
            long messageTime = timestamp.getTime();
            long diff = now - messageTime;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return context.getString(R.string.time_now);
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return context.getString(R.string.time_minutes_ago, minutes);
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return context.getString(R.string.time_hours_ago, hours);
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return context.getString(R.string.time_days_ago, days);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(timestamp);
            }
        }

        private SpannableString highlightSearchTerm(String text, String searchTerm) {
            SpannableString spannableString = new SpannableString(text);
            
            if (searchTerm.isEmpty() || text.isEmpty()) {
                return spannableString;
            }

            String lowerText = text.toLowerCase();
            String lowerSearchTerm = searchTerm.toLowerCase();
            
            int startIndex = 0;
            while (startIndex < lowerText.length()) {
                int index = lowerText.indexOf(lowerSearchTerm, startIndex);
                if (index == -1) {
                    break;
                }
                
                // Highlight the search term
                int highlightColor = ContextCompat.getColor(context, R.color.cosmic_accent);
                BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(highlightColor);
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                
                spannableString.setSpan(backgroundSpan, index, index + searchTerm.length(), 
                                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(boldSpan, index, index + searchTerm.length(), 
                                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                startIndex = index + searchTerm.length();
            }
            
            return spannableString;
        }
    }
}