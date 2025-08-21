package com.cosmic.gatherly.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.User;

public class MainChatFragment extends Fragment {
    
    private MainActivityCallback mainCallback;
    private RecyclerView channelsRecyclerView;
    private RecyclerView messagesRecyclerView;
    private RecyclerView membersRecyclerView;

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

    private void initializeViews(View view) {
        channelsRecyclerView = view.findViewById(R.id.channelsRecyclerView);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView);
    }

    private void setupRecyclerViews() {
        // Setup channels RecyclerView
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Setup messages RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Setup members RecyclerView
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadInitialData() {
        User currentUser = mainCallback.getCurrentUser();
        if (currentUser != null) {
            // Load user's servers, channels, and messages
            loadUserServers();
        }
    }

    private void loadUserServers() {
        // This would typically load servers from the repository
        // For now, we'll implement the basic structure
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainCallback = null;
    }
}