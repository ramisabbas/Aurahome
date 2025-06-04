package com.example.homeautomationsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user data
        holder.nameTextView.setText("Name: " + user.getName());
        holder.emailTextView.setText("Email: " + user.getEmail());
        holder.purposeTextView.setText("Purpose: " + user.getPurpose());
        holder.organizationTextView.setText("Organization: " + (user.getOrganization().isEmpty() ? "N/A" : user.getOrganization()));
        holder.statusTextView.setText("Status: " + user.getStatus());

        // Disable buttons if status is not inactive
        if (!"inactive".equals(user.getStatus())) {
            holder.acceptButton.setEnabled(false);
            holder.rejectButton.setEnabled(false);
            holder.acceptButton.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.darker_gray));
            holder.rejectButton.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.darker_gray));
        } else {
            holder.acceptButton.setEnabled(true);
            holder.rejectButton.setEnabled(true);
            holder.acceptButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.accept_button_color));
            holder.rejectButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.reject_button_color));
        }

        // Handle Accept button click
        holder.acceptButton.setOnClickListener(v -> {
            updateUserStatus(user.getUid(), "active", position);
        });

        // Handle Reject button click
        holder.rejectButton.setOnClickListener(v -> {
            updateUserStatus(user.getUid(), "rejected", position);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void updateUserStatus(String uid, String status, int position) {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.get(position).setStatus(status);
                        notifyItemChanged(position);
                        Toast.makeText(context, "User " + (status.equals("active") ? "accepted" : "rejected"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update status: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, purposeTextView, organizationTextView, statusTextView;
        Button acceptButton, rejectButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
            purposeTextView = itemView.findViewById(R.id.user_purpose);
            organizationTextView = itemView.findViewById(R.id.user_organization);
            statusTextView = itemView.findViewById(R.id.user_status);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}