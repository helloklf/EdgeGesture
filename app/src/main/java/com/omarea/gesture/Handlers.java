package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;

import java.util.ArrayList;

public class Handlers {
    private final static int GLOBAL_ACTION_BACK = AccessibilityService.GLOBAL_ACTION_BACK;
    private final static int GLOBAL_ACTION_HOME = AccessibilityService.GLOBAL_ACTION_HOME;
    private final static int GLOBAL_ACTION_RECENTS = AccessibilityService.GLOBAL_ACTION_RECENTS;
    private final static int GLOBAL_ACTION_NOTIFICATIONS = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
    private final static int GLOBAL_ACTION_QUICK_SETTINGS = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS;
    private final static int GLOBAL_ACTION_POWER_DIALOG = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG;
    private final static int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
    private final static int GLOBAL_ACTION_LOCK_SCREEN = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN;
    private final static int GLOBAL_ACTION_TAKE_SCREENSHOT = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;

    static String getOption(int value) {
        String[] options = getOptions();
        ArrayList<Integer> values = getValues();
        return options[((ArrayList) values).indexOf(value)];
    }

    static String[] getOptions() {
        ArrayList<String> options = new ArrayList<String>(){{
            add("返回");
            add("首页");
            add("任务");
            add("通知");
            add("快捷设置");
            add("电源弹窗");
        }};

        if (Build.VERSION.SDK_INT > 23) {
            options.add("分屏");
        }
        if (Build.VERSION.SDK_INT > 27) {
            options.add("锁屏");
            options.add("截图");
        }

        return options.toArray(new String[0]);
    }

    static ArrayList<Integer> getValues() {
        ArrayList<Integer> options = new ArrayList<Integer>(){{
            add(GLOBAL_ACTION_BACK);
            add(GLOBAL_ACTION_HOME);
            add(GLOBAL_ACTION_RECENTS);
            add(GLOBAL_ACTION_NOTIFICATIONS);
            add(GLOBAL_ACTION_QUICK_SETTINGS);
            add(GLOBAL_ACTION_POWER_DIALOG);
        }};

        if (Build.VERSION.SDK_INT > 23) {
            options.add(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
        }
        if (Build.VERSION.SDK_INT > 27) {
            options.add(GLOBAL_ACTION_LOCK_SCREEN);
            options.add(GLOBAL_ACTION_TAKE_SCREENSHOT);
        }

        return options;
    }
}
