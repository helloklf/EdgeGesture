package com.omarea.gesture.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.omarea.gesture.AccessibilityServiceKeyEvent;
import com.omarea.gesture.ActionModel;
import com.omarea.gesture.AppSwitchActivity;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.shell.KeepShellPublic;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class Handlers {
    final public static int GLOBAL_ACTION_NONE = 0;
    final public static int GLOBAL_ACTION_BACK = AccessibilityService.GLOBAL_ACTION_BACK;
    final public static int GLOBAL_ACTION_HOME = AccessibilityService.GLOBAL_ACTION_HOME;
    final public static int GLOBAL_ACTION_RECENTS = AccessibilityService.GLOBAL_ACTION_RECENTS;
    final public static int GLOBAL_ACTION_NOTIFICATIONS = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
    final public static int GLOBAL_ACTION_QUICK_SETTINGS = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS;
    final public static int GLOBAL_ACTION_POWER_DIALOG = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG;
    final public static int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
    final public static int GLOBAL_ACTION_LOCK_SCREEN = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN;
    final public static int GLOBAL_ACTION_TAKE_SCREENSHOT = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;
    final public static int VITUAL_ACTION_NEXT_APP = 900000;
    final public static int VITUAL_ACTION_PREV_APP = 900001;
    final public static int VITUAL_ACTION_XIAO_AI = 900002;
    final public static int VITUAL_ACTION_SWITCH_APP = 900005;
    final public static int VITUAL_ACTION_FORM = 900009;
    private static SharedPreferences config;
    private static boolean isXiaomi = Build.MANUFACTURER.equals("Xiaomi") && Build.BRAND.equals("Xiaomi");

    private final static ActionModel[] options = new ArrayList<ActionModel>() {{
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_NONE;
            title = "无";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_BACK;
            title = "返回键";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_HOME;
            title = "主页键";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_RECENTS;
            title = "任务键";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_NOTIFICATIONS;
            title = "下拉通知";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_QUICK_SETTINGS;
            title = "快捷面板";
        }});
        add(new ActionModel() {{
            actionCode = GLOBAL_ACTION_POWER_DIALOG;
            title = "电源菜单";
        }});
        add(new ActionModel() {{
            actionCode = VITUAL_ACTION_PREV_APP;
            title = "上个应用";
        }});
        add(new ActionModel() {{
            actionCode = VITUAL_ACTION_NEXT_APP;
            title = "下个应用";
        }});

        if (isXiaomi) {
            add(new ActionModel() {{
                actionCode = VITUAL_ACTION_XIAO_AI;
                title = "小爱[ROOT]";
            }});
        }

        if (Build.VERSION.SDK_INT > 23) {
            add(new ActionModel() {{
                actionCode = GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
                title = "分屏";
            }});
            add(new ActionModel() {{
                actionCode = VITUAL_ACTION_SWITCH_APP;
                title = "上个应用[原生]";
            }});
            add(new ActionModel() {{
                actionCode = VITUAL_ACTION_FORM;
                title = "窗口化[试验]";
            }});
        }

        if (Build.VERSION.SDK_INT > 27) {
            add(new ActionModel() {{
                actionCode = GLOBAL_ACTION_LOCK_SCREEN;
                title = "锁屏";
            }});
            add(new ActionModel() {{
                actionCode = GLOBAL_ACTION_TAKE_SCREENSHOT;
                title = "屏幕截图";
            }});
        }
    }}.toArray(new ActionModel[0]);

    private static Process rootProcess = null;
    private static OutputStream rootOutputStream = null;

    public static void executeVirtualAction(final AccessibilityServiceKeyEvent accessibilityService, final int action) {
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
                        } catch (Exception ignored) {
                        }
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    }
                }).start();
                break;
            }
            case VITUAL_ACTION_NEXT_APP:
            case VITUAL_ACTION_PREV_APP:
            case VITUAL_ACTION_FORM:
            case GLOBAL_ACTION_HOME: {
                int animation = accessibilityService
                        .getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE)
                        .getInt(SpfConfig.HOME_ANIMATION, SpfConfig.HOME_ANIMATION_DEFAULT);
                if (action == GLOBAL_ACTION_HOME && animation == SpfConfig.HOME_ANIMATION_DEFAULT) {
                    accessibilityService.performGlobalAction(action);
                } else {
                    appSwitch(accessibilityService, action, animation);
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
                        } catch (Exception ignored) {
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

    private static void appSwitch(final AccessibilityServiceKeyEvent accessibilityService, final int action, final int animation) {
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
                case VITUAL_ACTION_FORM: {
                    intent.putExtra("form", accessibilityService.recents.getCurrent());
                    break;
                }
                case VITUAL_ACTION_PREV_APP:
                case VITUAL_ACTION_NEXT_APP: {
                    if (config == null) {
                        config = accessibilityService.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
                    }
                    if (config.getBoolean(SpfConfig.ROOT_GET_RECENTS, SpfConfig.ROOT_GET_RECENTS_DEFAULT)) {
                        // 单个app：dumpsys activity com.android.browser | grep ACTIVITY | cut -F3 | cut -f1 -d '/'
                        // recent： dumpsys activity r | grep TaskRecord | grep A= | cut -F7 | cut -f2 -d '='
                        // top Activity（慢）： dumpsys activity top | grep ACTIVITY | cut -F3 | cut -f1 -d '/'
                        ArrayList<String> recents = new ArrayList<>();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            Collections.addAll(recents, KeepShellPublic.doCmdSync("dumpsys activity r | grep realActivity | cut -f2 -d '=' | cut -f1 -d '/'").split("\n"));
                        } else {
                            Collections.addAll(recents, KeepShellPublic.doCmdSync("dumpsys activity r | grep mActivityComponent | cut -f2 -d '=' | cut -f1 -d '/'").split("\n"));
                        }
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

    public static String getOption(int value) {
        for (ActionModel actionModel:options) {
            if (actionModel.actionCode == value) {
                return actionModel.title;
            }
        }
        return "";
    }

    public static ActionModel[] getOptions() {
        return options;
    }

    private static void openXiaoAi() {
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
