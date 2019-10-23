package com.omarea.gesture;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class ReciverLockHandler extends Handler {
    private View bar;

    ReciverLockHandler(View bar) {
        this.bar = bar;
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg != null) {
            if (msg.what == ReciverLock.EVENT_SCREEN_ON) {
                bar.setVisibility(View.VISIBLE);
            } else if (msg.what == ReciverLock.EVENT_SCREEN_OFF) {
                bar.setVisibility(View.GONE);
            }
        }
    }
}
