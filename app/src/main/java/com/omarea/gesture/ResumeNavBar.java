package com.omarea.gesture;

import android.content.ContentResolver;
import android.provider.Settings;

public class ResumeNavBar {
    private ContentResolver cr = null;

    ResumeNavBar(ContentResolver contentResolver) {
        this.cr = contentResolver;
    }

    void run() {
        try {
            Settings.Global.putInt(cr, "navigation_bar_gesture_disabled_by_policy", 0); // oneui 策略取消强制禁用手势（因为锁屏唤醒后底部会触摸失灵，需要重新开关）
        } catch (Exception ignored) {
        }
    }
}