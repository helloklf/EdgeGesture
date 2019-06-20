package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public class Handlers {
    final static int GLOBAL_ACTION_BACK = AccessibilityService.GLOBAL_ACTION_BACK;
    final static int GLOBAL_ACTION_HOME = AccessibilityService.GLOBAL_ACTION_HOME;
    final static int GLOBAL_ACTION_RECENTS = AccessibilityService.GLOBAL_ACTION_RECENTS;
    final static int GLOBAL_ACTION_NOTIFICATIONS = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
    final static int GLOBAL_ACTION_QUICK_SETTINGS = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS;
    final static int GLOBAL_ACTION_POWER_DIALOG = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG;
    final static int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
    final static int GLOBAL_ACTION_LOCK_SCREEN = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN;
    final static int GLOBAL_ACTION_TAKE_SCREENSHOT = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;

    final static int VITUAL_ACTION_LAST_APP = 900001;
    final static int VITUAL_ACTION_OPEN_APP = 900002;

    static boolean isVitualAction(int aciont) {
        if (aciont == VITUAL_ACTION_LAST_APP) {
            return true;
        } else if (aciont == VITUAL_ACTION_OPEN_APP) {
            return true;
        }
        return false;
    }

    static void executeVitualAction(final AccessibilityService accessibilityService, final int action) {
        switch (action) {
            case VITUAL_ACTION_LAST_APP: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                        try {
                            Thread.sleep(200);
                        } catch (Exception ex) {
                        }
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    }
                }).start();
                break;
            }
            case GLOBAL_ACTION_TAKE_SCREENSHOT: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (Exception ex) {}
                        accessibilityService.performGlobalAction(action);
                    }
                }).start();
                break;
            }
            default:{
                accessibilityService.performGlobalAction(action);
                break;
            }
        }
    }

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
            add("工具面板");
            add("通知");
            add("快捷设置");
            add("电源弹窗");
        }};

        if (Build.VERSION.SDK_INT > 23) {
            options.add("分屏");
            options.add("上个应用");
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
            options.add(VITUAL_ACTION_LAST_APP);
        }
        if (Build.VERSION.SDK_INT > 27) {
            options.add(GLOBAL_ACTION_LOCK_SCREEN);
            options.add(GLOBAL_ACTION_TAKE_SCREENSHOT);
        }

        return options;
    }
}
