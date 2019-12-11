package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class ReceiverLockHandler extends Handler {
    private View bar;
    private AccessibilityService accessibilityService;

    ReceiverLockHandler(View bar, AccessibilityService accessibilityService) {
        this.bar = bar;
        this.accessibilityService = accessibilityService;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            if (bar.isAttachedToWindow()) {
                if (msg != null) {
                    if (msg.what == ReceiverLock.EVENT_SCREEN_ON) {
                        bar.setVisibility(View.VISIBLE);
                    } else if (msg.what == ReceiverLock.EVENT_SCREEN_OFF && new ScreenState(bar.getContext()).isScreenLocked()) {
                        // 为啥已经监听到息屏了还要手动判断屏幕状态呢？
                        // 因为，丢特么的的，在某些手机上，
                        // 使用电源键快速息屏，并用指纹立即解锁（息屏时间不超过5秒）的话，息屏广播会在解锁广播之后发送

                        bar.setVisibility(View.GONE);
                    }
                }
            } else {
                ReceiverLock.unRegister(accessibilityService);
            }
        } catch (Exception ignored) {
        }
    }
}
