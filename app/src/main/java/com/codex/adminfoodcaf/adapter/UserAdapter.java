package com.codex.adminfoodcaf.adapter;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> fullList;
    private List<User> showList;

    public UserAdapter(List<User> userList) {
        this.fullList = new ArrayList<>(userList);
        this.showList = new ArrayList<>(userList);
    }

    public void filter(String query) {
        showList.clear();
        if (query == null || query.isEmpty()) {
            showList.addAll(fullList);
        } else {
            String q = query.toLowerCase().trim();
            for (User u : fullList) {
                boolean nameMatch = u.getName() != null && u.getName().toLowerCase().contains(q);
                boolean emailMatch = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                if (nameMatch || emailMatch)
                    showList.add(u);
            }

            showList.sort((u1, u2) -> {
                boolean u1Starts = (u1.getName() != null && u1.getName().toLowerCase().startsWith(q))
                                || (u1.getEmail() != null && u1.getEmail().toLowerCase().startsWith(q));
                boolean u2Starts = (u2.getName() != null && u2.getName().toLowerCase().startsWith(q))
                                || (u2.getEmail() != null && u2.getEmail().toLowerCase().startsWith(q));
                
                if (u1Starts && !u2Starts) return -1;
                if (!u1Starts && u2Starts) return 1;
                return 0;
            });
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = showList.get(position);

        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
        holder.tvPhone.setText(user.getMobileNum() != null ? user.getMobileNum() : "-");
        holder.tvAddress.setText(user.getAddress() != null ? user.getAddress() : "-");

        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
            Glide.with(holder.imgProfile.getContext())
                    .load(user.getProfilePicUrl())
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .centerCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        setStatusChip(holder.chipStatus, holder.btnToggleStatus, user.isStatus());

        holder.btnViewProfile.setOnClickListener(v -> showUserProfileDialog(v, user));

        holder.btnToggleStatus.setOnClickListener(v -> {
            boolean currentStatus = user.isStatus();
            boolean newStatus = !currentStatus;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Change Status")
                    .setMessage("Set user status to \"" + (newStatus ? "Active" : "Suspended") + "\"?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUId())
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    user.setStatus(newStatus);
                                    notifyItemChanged(holder.getAdapterPosition());
                                    Toast.makeText(v.getContext(),
                                            "Status updated to " + newStatus,
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(),
                                        "Failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setStatusChip(Chip chip, MaterialButton btn, boolean isActive) {
        if (isActive) {
            chip.setText("Active");
            chip.setChipBackgroundColorResource(android.R.color.holo_green_light);
            chip.setTextColor(Color.WHITE);
            btn.setText("Suspend");
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#F44336")));
        } else {
            chip.setText("Suspended");
            chip.setChipBackgroundColorResource(android.R.color.holo_red_light);
            chip.setTextColor(Color.WHITE);
            btn.setText("Activate");
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#6DBE82")));
        }
    }

    private void showUserProfileDialog(View anchor, User user) {
        View dialogView = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_user_profile, null);

        ImageView imgProfile = dialogView.findViewById(R.id.dialogImgProfile);
        TextView tvName = dialogView.findViewById(R.id.dialogTvName);
        TextView tvEmail = dialogView.findViewById(R.id.dialogTvEmail);
        TextView tvPhone = dialogView.findViewById(R.id.dialogTvPhone);
        TextView tvAddress = dialogView.findViewById(R.id.dialogTvAddress);
        TextView tvUid = dialogView.findViewById(R.id.dialogTvUid);
        Chip chipStatus = dialogView.findViewById(R.id.dialogChipStatus);

        tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
        tvPhone.setText(user.getMobileNum() != null ? user.getMobileNum() : "-");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "-");
        tvUid.setText(user.getUId() != null ? user.getUId() : "-");

        boolean isActive = user.isStatus();
        chipStatus.setText(isActive ? "Active" : "Suspended");
        chipStatus.setChipBackgroundColorResource(
                isActive ? android.R.color.holo_green_light : android.R.color.holo_red_light);
        chipStatus.setTextColor(Color.WHITE);

        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
            Glide.with(anchor.getContext())
                    .load(user.getProfilePicUrl())
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .centerCrop()
                    .into(imgProfile);
        }

        AlertDialog dialog = new AlertDialog.Builder(anchor.getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.dialogBtnClose).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvName, tvEmail, tvPhone, tvAddress;
        Chip chipStatus;
        MaterialButton btnViewProfile, btnToggleStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgUserProfile);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            tvAddress = itemView.findViewById(R.id.tvUserAddress);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
        }
    }
}