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
import com.cosmic.gatherly.data.manager.VoiceChannelManager;
import com.cosmic.gatherly.data.model.Member;
import com.cosmic.gatherly.data.model.VoiceChannelState;
import com.cosmic.gatherly.ui.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.List;

public class MembersDialog extends Dialog implements MemberAdapter.OnMemberClickListener {
    
    private ImageView closeMembersButton;
    private TextView channelNameText;
    private RecyclerView membersRecyclerView;
    private LinearLayout voiceControlsLayout;
    private ImageView muteButton;
    private ImageView deafenButton;
    private ImageView disconnectButton;
    
    private MemberAdapter memberAdapter;
    private VoiceChannelManager voiceChannelManager;
    private String currentChannelId;
    private String currentChannelName;
    private OnMembersDialogListener listener;
    
    public interface OnMembersDialogListener {
        void onMemberSelected(Member member);
        void onVoiceControlAction(String action);
    }

    public MembersDialog(@NonNull Context context) {
        super(context, R.style.Theme_Gatherly_Dialog_FullScreen);
        this.voiceChannelManager = VoiceChannelManager.getInstance(context);
    }

    public void setCurrentChannel(String channelId, String channelName) {
        this.currentChannelId = channelId;
        this.currentChannelName = channelName;
    }

    public void setOnMembersDialogListener(OnMembersDialogListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make dialog slide in from right
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_members);
        
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.END;
            window.setAttributes(params);
            
            // Add slide animation
            window.setWindowAnimations(R.style.SlideInFromRight);
        }
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadMembers();
        updateVoiceControls();
    }

    private void initializeViews() {
        closeMembersButton = findViewById(R.id.closeMembersButton);
        channelNameText = findViewById(R.id.channelNameText);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        voiceControlsLayout = findViewById(R.id.voiceControlsLayout);
        muteButton = findViewById(R.id.muteButton);
        deafenButton = findViewById(R.id.deafenButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        
        // Set channel name
        if (currentChannelName != null) {
            channelNameText.setText(currentChannelName);
        }
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter(getContext());
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        membersRecyclerView.setAdapter(memberAdapter);
        
        memberAdapter.setOnMemberClickListener(this);
    }

    private void setupClickListeners() {
        closeMembersButton.setOnClickListener(v -> dismiss());
        
        muteButton.setOnClickListener(v -> {
            voiceChannelManager.toggleMute();
            updateVoiceControls();
            if (listener != null) {
                listener.onVoiceControlAction("mute");
            }
        });
        
        deafenButton.setOnClickListener(v -> {
            voiceChannelManager.toggleDeafen();
            updateVoiceControls();
            if (listener != null) {
                listener.onVoiceControlAction("deafen");
            }
        });
        
        disconnectButton.setOnClickListener(v -> {
            voiceChannelManager.disconnectFromVoiceChannel();
            updateVoiceControls();
            if (listener != null) {
                listener.onVoiceControlAction("disconnect");
            }
        });
    }

    private void loadMembers() {
        List<Member> members = getSampleMembers();
        memberAdapter.setMembers(members);
    }

    private List<Member> getSampleMembers() {
        List<Member> members = new ArrayList<>();
        
        // Sample members for the channel
        members.add(new Member(
            "1", "CosmicExplorer", "Coding nebula shaders", "OWNER", true,
            R.drawable.ic_person, 0xFF00D166
        ));
        
        members.add(new Member(
            "2", "StarDust", "Playing Cosmic Quest", "ADMIN", true,
            R.drawable.ic_star, 0xFF5865F2
        ));
        
        members.add(new Member(
            "3", "NebulaNinja", "Away", "MOD", true,
            R.drawable.ic_person, 0xFFFEE75C
        ));
        
        members.add(new Member(
            "4", "GalaxyGamer", "Online", "MEMBER", true,
            R.drawable.ic_person, 0xFF00D166
        ));
        
        members.add(new Member(
            "5", "ChillGamer", "Offline", "MEMBER", false,
            R.drawable.ic_person, 0xFF95A5A6
        ));
        
        // Set voice channel status for some members
        if (voiceChannelManager.isConnectedToVoiceChannel()) {
            members.get(0).setInVoiceChannel(true);
            members.get(1).setInVoiceChannel(true);
            members.get(1).setSpeaking(true);
        }
        
        return members;
    }

    private void updateVoiceControls() {
        VoiceChannelState voiceState = voiceChannelManager.getCurrentVoiceChannelState();
        
        if (voiceState != null && voiceState.isConnected()) {
            voiceControlsLayout.setVisibility(View.VISIBLE);
            
            // Update mute button
            if (voiceState.isMuted()) {
                muteButton.setImageResource(R.drawable.ic_mic_off);
                muteButton.setColorFilter(getContext().getColor(R.color.cosmic_button_danger));
            } else {
                muteButton.setImageResource(R.drawable.ic_mic);
                muteButton.setColorFilter(getContext().getColor(R.color.cosmic_gray));
            }
            
            // Update deafen button
            if (voiceState.isDeafened()) {
                deafenButton.setImageResource(R.drawable.ic_headphones_off);
                deafenButton.setColorFilter(getContext().getColor(R.color.cosmic_button_danger));
            } else {
                deafenButton.setImageResource(R.drawable.ic_headphones);
                deafenButton.setColorFilter(getContext().getColor(R.color.cosmic_gray));
            }
        } else {
            voiceControlsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMemberClick(Member member) {
        android.util.Log.d("MembersDialog", "Member clicked: " + member.getName());
        
        if (listener != null) {
            listener.onMemberSelected(member);
        }
        
        // Show member info
        String memberInfo = member.getName() + "\n" +
                           "Status: " + (member.isOnline() ? "Online" : "Offline") + "\n" +
                           "Role: " + (member.getRole() != null ? member.getRole() : "Member");
        
        if (member.isInVoiceChannel()) {
            memberInfo += "\nIn voice channel" + (member.isSpeaking() ? " (Speaking)" : "");
        }
        
        android.widget.Toast.makeText(getContext(), memberInfo, android.widget.Toast.LENGTH_SHORT).show();
    }
}