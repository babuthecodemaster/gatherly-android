package com.cosmic.gatherly.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.manager.SearchManager;
import com.cosmic.gatherly.data.model.SearchResult;
import com.cosmic.gatherly.ui.adapters.SearchResultAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SearchDialog extends Dialog implements SearchManager.SearchCallback {
    
    private TextInputEditText searchEditText;
    private ImageView closeSearchButton;
    private LinearLayout searchResultsHeader;
    private TextView searchResultsCount;
    private TextView searchInChannel;
    private RecyclerView searchResultsRecyclerView;
    private LinearLayout noResultsView;
    private LinearLayout searchLoadingView;
    
    private SearchResultAdapter searchResultAdapter;
    private SearchManager searchManager;
    private String currentChannelId;
    private String currentChannelName;
    private OnSearchResultSelectedListener listener;
    
    public interface OnSearchResultSelectedListener {
        void onSearchResultSelected(SearchResult result);
    }

    public SearchDialog(@NonNull Context context) {
        super(context, R.style.Theme_Gatherly_Dialog_FullScreen);
        this.searchManager = SearchManager.getInstance(context);
    }

    public void setCurrentChannel(String channelId, String channelName) {
        this.currentChannelId = channelId;
        this.currentChannelName = channelName;
    }

    public void setOnSearchResultSelectedListener(OnSearchResultSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make dialog full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search);
        
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
                           WindowManager.LayoutParams.MATCH_PARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        
        initializeViews();
        setupRecyclerView();
        setupSearchInput();
        setupClickListeners();
        
        // Show keyboard and focus on search input
        showKeyboard();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        closeSearchButton = findViewById(R.id.closeSearchButton);
        searchResultsHeader = findViewById(R.id.searchResultsHeader);
        searchResultsCount = findViewById(R.id.searchResultsCount);
        searchInChannel = findViewById(R.id.searchInChannel);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        noResultsView = findViewById(R.id.noResultsView);
        searchLoadingView = findViewById(R.id.searchLoadingView);
        
        // Set channel name if available
        if (currentChannelName != null) {
            searchInChannel.setText(getContext().getString(R.string.search_in_channel, currentChannelName));
        }
    }

    private void setupRecyclerView() {
        searchResultAdapter = new SearchResultAdapter(getContext());
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchResultAdapter);
        
        searchResultAdapter.setOnSearchResultClickListener(result -> {
            if (listener != null) {
                listener.onSearchResultSelected(result);
            }
            dismiss();
        });
    }

    private void setupSearchInput() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showNoResults();
                } else {
                    performSearch(query);
                }
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        closeSearchButton.setOnClickListener(v -> dismiss());
        
        // Handle back button
        setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                dismiss();
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        showLoading();
        searchManager.searchMessages(query, currentChannelId, this);
    }

    private void showLoading() {
        searchLoadingView.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.GONE);
        noResultsView.setVisibility(View.GONE);
    }

    private void showResults(List<SearchResult> results, String query) {
        if (results.isEmpty()) {
            showNoResults();
            return;
        }

        searchResultAdapter.setSearchResults(results, query);
        searchResultsCount.setText(getContext().getString(R.string.search_results_count, results.size()));
        
        searchLoadingView.setVisibility(View.GONE);
        noResultsView.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoResults() {
        searchLoadingView.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.GONE);
        noResultsView.setVisibility(View.VISIBLE);
    }
    
    private void showSearchError(String errorMessage) {
        searchLoadingView.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        searchResultsHeader.setVisibility(View.GONE);
        
        // Show error in no results view
        noResultsView.setVisibility(View.VISIBLE);
        TextView noResultsText = noResultsView.findViewById(R.id.noResultsText);
        if (noResultsText != null) {
            noResultsText.setText(errorMessage);
        }
        
        // Add retry button if not already present
        Button retryButton = noResultsView.findViewById(R.id.retrySearchButton);
        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
            retryButton.setOnClickListener(v -> {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
            });
        }
    }

    private void showKeyboard() {
        if (searchEditText != null) {
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public void onSearchResults(List<SearchResult> results) {
        String query = searchEditText.getText().toString().trim();
        showResults(results, query);
    }

    @Override
    public void onSearchError(String error) {
        android.util.Log.e("SearchDialog", "Search error: " + error);
        showSearchError(error);
    }
    
    @Override
    public void onSearchStarted() {
        showLoading();
    }
    
    @Override
    public void onSearchRetrying(int attemptNumber) {
        // Update loading message to show retry
        if (searchLoadingView != null && searchLoadingView.getVisibility() == View.VISIBLE) {
            TextView loadingText = searchLoadingView.findViewById(R.id.loadingText);
            if (loadingText != null) {
                loadingText.setText(getContext().getString(R.string.search_retrying, attemptNumber));
            }
        }
    }

    @Override
    public void dismiss() {
        // Hide keyboard before dismissing
        if (searchEditText != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        }
        super.dismiss();
    }
}