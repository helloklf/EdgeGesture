package com.omarea.gesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

/**
 * 监听屏幕开关事件
 * Created by Hello on 2018/01/23.
 */

class ReciverLock extends BroadcastReceiver {
    public static int EVENT_SCREEN_OFF = 8;
    public static int EVENT_SCREEN_ON = 10;

    private Handler callbacks;

    public ReciverLock(Handler callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onReceive(Context p0, Intent p1) {
        if (p1 == null) {
            return;
        }

        String action = p1.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                try {
                    callbacks.sendMessage(callbacks.obtainMessage(EVENT_SCREEN_OFF));
                } catch (Exception ignored) {
                }
            } else if (action.equals(Intent.ACTION_USER_PRESENT) || action.equals(Intent.ACTION_USER_UNLOCKED) || action.equals(Intent.ACTION_SCREEN_ON)) {
                try {
                    callbacks.sendMessage(callbacks.obtainMessage(EVENT_SCREEN_ON));
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static ReciverLock reciver = null;

    public static ReciverLock autoRegister(Context context, Handler callbacks) {
        if (reciver != null) {
            unRegister(context);
        }

        reciver = new ReciverLock(callbacks);
        Context bc = context.getApplicationContext();

        bc.registerReceiver(reciver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bc.registerReceiver(reciver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        }
        bc.registerReceiver(reciver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        bc.registerReceiver(reciver, new IntentFilter(Intent.ACTION_USER_PRESENT));

        return reciver;
    }

    public static void unRegister(Context context) {
        if (reciver == null) {
            return;
        }
        context.getApplicationContext().unregisterReceiver(reciver);
        reciver = null;
    }
}
