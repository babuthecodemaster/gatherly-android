package com.cosmic.gatherly.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    
    private List<Member> members;
    private OnMemberClickListener clickListener;
    private Context context;

    public interface OnMemberClickListener {
        void onMemberClick(Member member);
    }

    public MemberAdapter(Context context) {
        this.context = context;
        this.members = new ArrayList<>();
    }

    public void setMembers(List<Member> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView memberAvatar;
        private View statusIndicator;
        private TextView memberName;
        private TextView memberStatus;
        private TextView roleBadge;
        private ImageView voiceIndicator;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberAvatar = itemView.findViewById(R.id.memberAvatar);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            memberName = itemView.findViewById(R.id.memberName);
            memberStatus = itemView.findViewById(R.id.memberStatus);
            roleBadge = itemView.findViewById(R.id.roleBadge);
            voiceIndicator = itemView.findViewById(R.id.voiceIndicator);

            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onMemberClick(members.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Member member) {
            // Set member name
            memberName.setText(member.getName());
            
            // Set member avatar
            if (member.getAvatarResource() != 0) {
                memberAvatar.setImageResource(member.getAvatarResource());
            } else {
                memberAvatar.setImageResource(R.drawable.ic_person);
            }

            // Set status indicator color based on online status
            if (member.isOnline()) {
                if (member.getStatusColor() != 0) {
                    statusIndicator.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(member.getStatusColor()));
                } else {
                    statusIndicator.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(context.getColor(R.color.cosmic_green)));
                }
                statusIndicator.setVisibility(View.VISIBLE);
            } else {
                statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(context.getColor(R.color.cosmic_gray)));
                statusIndicator.setVisibility(View.VISIBLE);
            }

            // Set member status text
            if (member.getStatus() != null && !member.getStatus().isEmpty() && 
                !member.getStatus().equals("Online") && !member.getStatus().equals("Offline")) {
                memberStatus.setText(member.getStatus());
                memberStatus.setVisibility(View.VISIBLE);
            } else {
                memberStatus.setVisibility(View.GONE);
            }

            // Set role badge
            if (member.getRole() != null && !member.getRole().isEmpty() && 
                !member.getRole().equals("MEMBER")) {
                roleBadge.setText(member.getRole());
                roleBadge.setVisibility(View.VISIBLE);
                
                // Set role color
                switch (member.getRole().toUpperCase()) {
                    case "OWNER":
                        roleBadge.setTextColor(context.getColor(R.color.cosmic_accent));
                        break;
                    case "ADMIN":
                        roleBadge.setTextColor(context.getColor(R.color.cosmic_button_danger));
                        break;
                    case "MOD":
                    case "MODERATOR":
                        roleBadge.setTextColor(context.getColor(R.color.cosmic_blue));
                        break;
                    default:
                        roleBadge.setTextColor(context.getColor(R.color.cosmic_gray));
                        break;
                }
            } else {
                roleBadge.setVisibility(View.GONE);
            }

            // Set voice indicator
            if (member.isInVoiceChannel()) {
                voiceIndicator.setVisibility(View.VISIBLE);
                if (member.isSpeaking()) {
                    voiceIndicator.setColorFilter(context.getColor(R.color.cosmic_green));
                } else {
                    voiceIndicator.setColorFilter(context.getColor(R.color.cosmic_gray));
                }
            } else {
                voiceIndicator.setVisibility(View.GONE);
            }

            // Adjust member name color based on online status
            if (member.isOnline()) {
                memberName.setTextColor(context.getColor(R.color.cosmic_white));
            } else {
                memberName.setTextColor(context.getColor(R.color.cosmic_gray));
            }
        }
    }
}