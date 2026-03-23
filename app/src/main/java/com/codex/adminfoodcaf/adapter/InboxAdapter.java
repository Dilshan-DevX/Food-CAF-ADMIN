package com.codex.adminfoodcaf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.model.User;

import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    private List<User> userList;
    private OnUserClickListener listener;

    public InboxAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName() != null ? user.getName() : "Unknown User");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");

        // Profile picture load — fallback to account_circle_24px
        holder.imgUserAvatar.clearColorFilter(); // gray tint clear
        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePicUrl())
                    .circleCrop()
                    .placeholder(R.drawable.account_circle_24px)
                    .error(R.drawable.account_circle_24px)
                    .into(holder.imgUserAvatar);
        } else {
            holder.imgUserAvatar.setImageResource(R.drawable.account_circle_24px);
        }

        // මුළු කාඩ් එකම ක්ලික් කළාම
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail;
        ImageView imgUserAvatar;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}