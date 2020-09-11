package com.omarea.gesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.omarea.gesture.util.GlobalState;

public class BatteryReceiver extends BroadcastReceiver {
    private boolean powerConnected = false;
    public BatteryReceiver(){}

    public BatteryReceiver(Context context) {
        try {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            GlobalState.batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                powerConnected = notCharging(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS));
            }
        } catch (Exception ignored) {
        }
    }

    private boolean notCharging(int state) {
        return state != BatteryManager.BATTERY_STATUS_NOT_CHARGING && state != BatteryManager.BATTERY_STATUS_DISCHARGING;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        int capacity = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED: {
                powerConnected = notCharging(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
                break;
            }
            case Intent.ACTION_POWER_DISCONNECTED: {
                powerConnected = false;
                break;
            }
            case Intent.ACTION_POWER_CONNECTED: {
                powerConnected = true;
                break;
            }
        }
        if (capacity != GlobalState.batteryCapacity) {
            GlobalState.batteryCapacity = capacity;
            if (GlobalState.updateBar != null) {
                GlobalState.updateBar.run();
            }
        }
    }
}
