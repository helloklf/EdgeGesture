package com.omarea.gesture;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class ReceiverLockHandler extends Handler {
    private View bar;

    ReceiverLockHandler(View bar) {
        this.bar = bar;
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg != null) {
            if (msg.what == ReceiverLock.EVENT_SCREEN_ON) {
                bar.setVisibility(View.VISIBLE);
            } else if (msg.what == ReceiverLock.EVENT_SCREEN_OFF) {
                bar.setVisibility(View.GONE);
            }
        }
    }
}
