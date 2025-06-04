package com.example.homeautomationsystem;

public class DeviceItem {
    private String name;
    private int iconRes;
    private boolean isOn;

    public DeviceItem(String name, int iconRes, boolean isOn) {
        this.name = name;
        this.iconRes = iconRes;
        this.isOn = isOn;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for iconRes
    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    // Getter and Setter for isOn
    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }
}
