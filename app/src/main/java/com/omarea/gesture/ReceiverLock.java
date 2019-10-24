package com.omarea.gesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * 监听屏幕开关事件
 * Created by Hello on 2018/01/23.
 */

class ReceiverLock extends BroadcastReceiver {
    public static int EVENT_SCREEN_OFF = 8;
    public static int EVENT_SCREEN_ON = 10;

    private Handler callbacks;

    public ReceiverLock(Handler callbacks) {
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
                    Log.d("ReceiverLockHandler", "锁屏");
                    callbacks.sendMessage(callbacks.obtainMessage(EVENT_SCREEN_OFF));
                } catch (Exception ignored) {
                }
            // } else if (action.equals(Intent.ACTION_USER_PRESENT) || action.equals(Intent.ACTION_USER_UNLOCKED) || action.equals(Intent.ACTION_SCREEN_ON)) {
            } else if (action.equals(Intent.ACTION_USER_PRESENT) || action.equals(Intent.ACTION_USER_UNLOCKED)) {
                try {
                    Log.d("ReceiverLockHandler", "解锁");
                    callbacks.sendMessage(callbacks.obtainMessage(EVENT_SCREEN_ON));
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static ReceiverLock receiver = null;

    public static ReceiverLock autoRegister(Context context, Handler callbacks) {
        if (receiver != null) {
            unRegister(context);
        }

        receiver = new ReceiverLock(callbacks);
        Context bc = context.getApplicationContext();

        bc.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bc.registerReceiver(receiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        }
        bc.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        bc.registerReceiver(receiver, new IntentFilter(Intent.ACTION_USER_PRESENT));

        return receiver;
    }

    public static void unRegister(Context context) {
        if (receiver == null) {
            return;
        }
        context.getApplicationContext().unregisterReceiver(receiver);
        receiver = null;
    }
}
