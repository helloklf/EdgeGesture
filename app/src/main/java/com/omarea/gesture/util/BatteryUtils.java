package com.omarea.gesture.util;

import android.content.Context;
import android.os.BatteryManager;

public class BatteryUtils {
    private BatteryManager batteryManager;

    public BatteryUtils(Context context) {
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    public int getCapacity() {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
