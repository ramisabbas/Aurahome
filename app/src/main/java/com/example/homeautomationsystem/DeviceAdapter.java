package com.example.homeautomationsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<DeviceItem> deviceList;
    private String selectedRoom; // Store selectedRoom in the adapter

    public DeviceAdapter(List<DeviceItem> deviceList, String selectedRoom) {
        this.deviceList = deviceList;
        this.selectedRoom = selectedRoom;
    }


    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceItem item = deviceList.get(position);

        holder.deviceName.setText(item.getName());
        holder.deviceIcon.setImageResource(item.getIconRes());
        holder.deviceSwitch.setChecked(item.isOn());

        // Handle switch toggle
        holder.deviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setOn(isChecked);

            // Determine device type (fan or light)
            String deviceType = item.getName().toLowerCase().contains("fan") ? "fans" : "lights";

            // Update Firebase directly in "fans" or "lights"
            FirebaseDatabase.getInstance().getReference("rooms")
                    .child(selectedRoom)
                    .child(deviceType)
                    .child(item.getName())
                    .setValue(isChecked);

            // Optionally update the Load table
            String normalizedName = item.getName().replaceAll("\\s+", ""); // e.g., "Fan 1" -> "Fan1"
            List<String> allowedLoadKeys = Arrays.asList("Fan1", "Light1", "Fan2", "Light2", "Fan3", "Light3");
            if (allowedLoadKeys.contains(normalizedName)) {
                FirebaseDatabase.getInstance().getReference("Load")
                        .child(normalizedName)
                        .setValue(isChecked);
            }

            // Log activity if turned on
            if (isChecked) {
                String action = selectedRoom + " " + item.getName().toLowerCase() + " turned on";
                Map<String, Object> activity = new HashMap<>();
                activity.put("room", selectedRoom);
                activity.put("description", action);
                activity.put("timestamp", ServerValue.TIMESTAMP);

                FirebaseDatabase.getInstance().getReference("RecentActivity")
                        .push()
                        .setValue(activity);
            }
        });

    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceIcon;
        TextView deviceName;
         Switch deviceSwitch;
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceSwitch = itemView.findViewById(R.id.device_switch);
        }
    }
}

