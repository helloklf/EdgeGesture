package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.omarea.gesture.shell.KeepShellPublic;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class Handlers {
    private static SharedPreferences config;
    final static int GLOBAL_ACTION_NONE = 0;
    final static int GLOBAL_ACTION_BACK = AccessibilityService.GLOBAL_ACTION_BACK;
    final static int GLOBAL_ACTION_HOME = AccessibilityService.GLOBAL_ACTION_HOME;
    final static int GLOBAL_ACTION_RECENTS = AccessibilityService.GLOBAL_ACTION_RECENTS;
    final static int GLOBAL_ACTION_NOTIFICATIONS = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
    final static int GLOBAL_ACTION_QUICK_SETTINGS = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS;
    final static int GLOBAL_ACTION_POWER_DIALOG = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG;
    final static int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
    final static int GLOBAL_ACTION_LOCK_SCREEN = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN;
    final static int GLOBAL_ACTION_TAKE_SCREENSHOT = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;

    final static int VITUAL_ACTION_NEXT_APP = 900000;
    final static int VITUAL_ACTION_PREV_APP = 900001;
    final static int VITUAL_ACTION_XIAO_AI = 900002;
    final static int VITUAL_ACTION_SWITCH_APP = 900005;

    private static boolean isXiaomi = Build.MANUFACTURER.equals("Xiaomi") && Build.BRAND.equals("Xiaomi");

    private final static String[] options = new ArrayList<String>() {{
        add("无动作");
        add("返回");
        add("首页");
        add("任务");
        add("通知");
        add("快捷设置");
        add("电源弹窗");
        add("上个应用");
        add("下个应用");

        if (isXiaomi) {
            add("小爱同学（需要ROOT）");
        }

        if (Build.VERSION.SDK_INT > 23) {
            add("分屏");
            add("上个应用(原生)");
        }
        if (Build.VERSION.SDK_INT > 27) {
            add("锁屏");
            add("截图");
        }
    }}.toArray(new String[0]);

    private final static ArrayList<Integer> values = new ArrayList<Integer>() {{
                add(GLOBAL_ACTION_NONE);
                add(GLOBAL_ACTION_BACK);
                add(GLOBAL_ACTION_HOME);
                add(GLOBAL_ACTION_RECENTS);
                add(GLOBAL_ACTION_NOTIFICATIONS);
                add(GLOBAL_ACTION_QUICK_SETTINGS);
                add(GLOBAL_ACTION_POWER_DIALOG);
                add(VITUAL_ACTION_PREV_APP);
                add(VITUAL_ACTION_NEXT_APP);

                if (isXiaomi) {
                    add(VITUAL_ACTION_XIAO_AI);
                }

                if (Build.VERSION.SDK_INT > 23) {
                    add(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                    add(VITUAL_ACTION_SWITCH_APP);
                }
                if (Build.VERSION.SDK_INT > 27) {
                    add(GLOBAL_ACTION_LOCK_SCREEN);
                    add(GLOBAL_ACTION_TAKE_SCREENSHOT);
                }
            }};

    static void executeVirtualAction(final AccessibilityServiceKeyEvent accessibilityService, final int action) {
        switch (action) {
            case GLOBAL_ACTION_NONE: {
                break;
            }
            case VITUAL_ACTION_SWITCH_APP: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                        try {
                            Thread.sleep(350);
                        } catch (Exception ex) {
                        }
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    }
                }).start();
                break;
            }
            case VITUAL_ACTION_NEXT_APP:
            case VITUAL_ACTION_PREV_APP:
            case GLOBAL_ACTION_HOME: {
                int animation = accessibilityService
                                    .getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE)
                                    .getInt(SpfConfig.HOME_ANIMATION, SpfConfig.HOME_ANIMATION_DEFAULT);
                if (action == GLOBAL_ACTION_HOME && animation == SpfConfig.HOME_ANIMATION_DEFAULT) {
                    accessibilityService.performGlobalAction(action);
                } else {
                    try {
                        Intent intent = new Intent(accessibilityService, AppSwitchActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("animation", animation);
                        switch (action) {
                            case GLOBAL_ACTION_HOME: {
                                intent.putExtra("home", true);
                                break;
                            }
                            case VITUAL_ACTION_PREV_APP:
                            case VITUAL_ACTION_NEXT_APP: {
                                if (config == null) {
                                    config = accessibilityService.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
                                }
                                if (config.getBoolean(SpfConfig.ROOT_GET_RECENTS, SpfConfig.ROOT_GET_RECENTS_DEFAULT)) {
                                    // 单个app：dumpsys activity com.android.browser | grep ACTIVITY | cut -F3 | cut -f1 -d '/'
                                    // recent： dumpsys activity r | grep Recent | grep A= | cut -F7 | cut -f2 -d '='
                                    // top Activity（慢）： dumpsys activity top | grep ACTIVITY | cut -F3 | cut -f1 -d '/'
                                    ArrayList<String> recents = new ArrayList<>();
                                    Collections.addAll(recents, KeepShellPublic.doCmdSync("dumpsys activity r | grep realActivity | cut -f2 -d '=' | cut -f1 -d '/'").split("\n"));
                                    accessibilityService.recents.setRecents(recents, accessibilityService);
                                }
                                if (action == VITUAL_ACTION_PREV_APP) {
                                    String targetApp = accessibilityService.recents.movePrevious();
                                    if (targetApp != null) {
                                        intent.putExtra("prev", targetApp);
                                    } else {
                                        Toast.makeText(accessibilityService, "<<", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    String targetApp = accessibilityService.recents.moveNext();
                                    if (targetApp != null) {
                                        intent.putExtra("next", targetApp);
                                    } else {
                                        Toast.makeText(accessibilityService, ">>", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                break;
                            }
                        }
                        accessibilityService.startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(accessibilityService, "AppSwitch Exception >> " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case VITUAL_ACTION_XIAO_AI: {
                openXiaoAi();
                break;
            }
            case GLOBAL_ACTION_TAKE_SCREENSHOT: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (Exception ex) {
                        }
                        accessibilityService.performGlobalAction(action);
                    }
                }).start();
                break;
            }
            default: {
                accessibilityService.performGlobalAction(action);
                break;
            }
        }
    }

    static String getOption(int value) {
        String[] options = getOptions();
        ArrayList<Integer> values = getValues();
        return options[values.indexOf(value)];
    }

    static String[] getOptions() {
        return options;
    }

    static ArrayList<Integer> getValues() {
        return values;
    }

    private static Process rootProcess = null;
    private static OutputStream rootOutputStream = null;
    static void openXiaoAi() {
            /*
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName xiaoAi = new ComponentName("com.miui.voiceassist", "com.xiaomi.voiceassistant.AiSettings.AiShortcutActivit");
                intent.setComponent(xiaoAi);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                accessibilityService.startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(accessibilityService, "" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            */
            if (rootProcess == null) {
                try {
                    rootProcess = Runtime.getRuntime().exec("su");
                    rootOutputStream = rootProcess.getOutputStream();
                } catch (Exception ex) {
                }
            }
            if (rootProcess != null && rootOutputStream != null) {
                try {
                    rootOutputStream.write("am start -n com.miui.voiceassist/com.xiaomi.voiceassistant.AiSettings.AiShortcutActivity\n".getBytes());
                    rootOutputStream.flush();
                } catch (Exception ex) {
                    rootProcess = null;
                    rootOutputStream = null;
                }
            }
    }
}
