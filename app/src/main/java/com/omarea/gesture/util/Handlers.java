package com.omarea.gesture.util;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.omarea.gesture.AccessibilityServiceGesture;
import com.omarea.gesture.ActionModel;
import com.omarea.gesture.AppSwitchActivity;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.SpfConfigEx;
import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.shell.KeepShellPublic;
import com.omarea.gesture.ui.QuickPanel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

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
    final public static int VITUAL_ACTION_FORM = 900009;
    final public static int VITUAL_ACTION_SWITCH_APP = 900005;

    final public static int CUSTOM_ACTION_APP = 1000001;
    final public static int CUSTOM_ACTION_APP_WINDOW = 1000002;
    final public static int CUSTOM_ACTION_SHELL = 1000006;
    final public static int CUSTOM_ACTION_QUICK = 1000009;
    final public static int OMAREA_FILTER_SCREENSHOT = 1100000;
    private final static ActionModel[] options = new ArrayList<ActionModel>() {{
        add(new ActionModel(GLOBAL_ACTION_NONE, "无"));
        add(new ActionModel(GLOBAL_ACTION_BACK, "返回键"));
        add(new ActionModel(GLOBAL_ACTION_HOME, "Home键"));
        add(new ActionModel(GLOBAL_ACTION_RECENTS, "任务键"));
        add(new ActionModel(GLOBAL_ACTION_NOTIFICATIONS, "下拉通知"));
        add(new ActionModel(GLOBAL_ACTION_QUICK_SETTINGS, "快捷面板"));
        add(new ActionModel(GLOBAL_ACTION_POWER_DIALOG, "电源菜单"));
        add(new ActionModel(VITUAL_ACTION_PREV_APP, "上个应用"));
        add(new ActionModel(VITUAL_ACTION_NEXT_APP, "下个应用"));

        if (Build.VERSION.SDK_INT > 23) {
            add(new ActionModel(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN, "分屏"));
            add(new ActionModel(VITUAL_ACTION_SWITCH_APP, "上个应用[模拟任务键双击]"));
            add(new ActionModel(VITUAL_ACTION_FORM, "窗口化当前应用[试验]"));
        }

        if (Build.VERSION.SDK_INT > 27) {
            add(new ActionModel(GLOBAL_ACTION_LOCK_SCREEN, "锁屏"));
            add(new ActionModel(GLOBAL_ACTION_TAKE_SCREENSHOT, "屏幕截图"));
        }

        add(new ActionModel(CUSTOM_ACTION_APP, "打开应用 > "));
        if (Build.VERSION.SDK_INT > 23) {
            add(new ActionModel(CUSTOM_ACTION_APP_WINDOW, "以小窗口打开应用[试验]  > "));
        }
        add(new ActionModel(CUSTOM_ACTION_SHELL, "运行脚本 > "));
        add(new ActionModel(CUSTOM_ACTION_QUICK, "常用应用 > "));
        add(new ActionModel(OMAREA_FILTER_SCREENSHOT, "屏幕滤镜-正常截图"));
    }}.toArray(new ActionModel[0]);
    // private static boolean isXiaomi = Build.MANUFACTURER.equals("Xiaomi") && Build.BRAND.equals("Xiaomi");
    private static SharedPreferences configEx;
    private static boolean isMiui12 = getIsMiui12();

    // 获取动画模式
    private static int getAnimationRes() {
        if (GlobalState.consecutiveAction != null) {
            return SpfConfig.HOME_ANIMATION_FAST;
        } else if (Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) {
            return SpfConfig.HOME_ANIMATION_DEFAULT;
        } else {
            return Gesture.config.getInt(SpfConfig.HOME_ANIMATION, SpfConfig.HOME_ANIMATION_DEFAULT);
        }
    }

    // FIXME:
    // <uses-permission android:name="android.permission.STOP_APP_SWITCHES" />
    // 由于Google限制，再按下Home键以后，后台应用如果想要打开Activity则需要等待5秒，参考 stopAppSwitches 相关逻辑
    // 这导致应用切换手势和打开应用的操作变得体验很差
    // 目前还没找到解决办法
    public static void executeVirtualAction(
            final AccessibilityServiceGesture accessibilityService,
            final ActionModel action, float touchStartRawX, float touchStartRawY) {

        switch (action.actionCode) {
            case GLOBAL_ACTION_NONE: {
                break;
            }
            case VITUAL_ACTION_NEXT_APP:
            case VITUAL_ACTION_PREV_APP:
            case VITUAL_ACTION_FORM:
            case GLOBAL_ACTION_HOME: {
                if (Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT) || (action.actionCode == GLOBAL_ACTION_HOME  && (isMiui12 && GlobalState.isLandscapf))) {
                    accessibilityService.performGlobalAction(action.actionCode);
                } else {
                    int animation = getAnimationRes();

                    if (action.actionCode == GLOBAL_ACTION_HOME && animation == SpfConfig.HOME_ANIMATION_DEFAULT) {
                        accessibilityService.performGlobalAction(action.actionCode);
                    } else {
                        appSwitch(accessibilityService, action.actionCode, animation);
                    }
                }
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
                        accessibilityService.performGlobalAction(action.actionCode);
                    }
                }).start();
                break;
            }
            case CUSTOM_ACTION_SHELL: {
                executeShell(accessibilityService, action);
                break;
            }
            case CUSTOM_ACTION_APP:
            case CUSTOM_ACTION_APP_WINDOW: {
                openApp(accessibilityService, action);
                break;
            }
            case CUSTOM_ACTION_QUICK: {
                openQuickPanel(accessibilityService, touchStartRawX, touchStartRawY);
                break;
            }
            case OMAREA_FILTER_SCREENSHOT: {
                omareaFilterScreenShot(accessibilityService);
                break;
            }
            case VITUAL_ACTION_SWITCH_APP: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                        try {
                            Thread.sleep(400);
                        } catch (Exception ignored) {
                        }
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    }
                }).start();
                break;
            }
            default: {
                accessibilityService.performGlobalAction(action.actionCode);
                break;
            }
        }
    }

    private static void omareaFilterScreenShot(final AccessibilityServiceGesture accessibilityServiceGesture) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName("com.omarea.filter", "com.omarea.filter.ScreenCapActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            accessibilityServiceGesture.startActivity(intent);
        } catch (Exception ex) {
            Gesture.toast("[正常截图]是我另一款软件(屏幕滤镜)的功能，你似乎并没有在使用那款软件！", Toast.LENGTH_LONG);
        }
    }

    private static void openQuickPanel(final AccessibilityServiceGesture accessibilityService, float touchRawX, float touchRawY) {
        new QuickPanel(accessibilityService).open(touchRawX, touchRawY);
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (Exception ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
        }
        return line;
    }

    private static boolean getIsMiui12() {
        try {
            // 反射调用私有接口，被Google封杀了
            // Object result = Class.forName("android.os.Systemproperties").getMethod("get").invoke(null, "ro.miui.ui.version.name", "");
            // return "V12".equals(result.toString());
            return Objects.equals(getSystemProperty("ro.miui.ui.version.name"), "V12");
        } catch (Exception ex) {
            return false;
        }
    }

    private static void appSwitch(final AccessibilityServiceGesture accessibilityService, final int action, final int animation) {
        try {
            Intent intent = AppSwitchActivity.getOpenAppIntent(accessibilityService);
            intent.putExtra("animation", animation);

            switch (action) {
                case GLOBAL_ACTION_HOME: {
                    intent.putExtra("home", "");
                    // AppSwitchActivity.backHome(accessibilityService);
                    break;
                }
                case VITUAL_ACTION_FORM: {
                    intent.putExtra("form", accessibilityService.recents.getCurrent());
                    break;
                }
                case VITUAL_ACTION_PREV_APP:
                case VITUAL_ACTION_NEXT_APP: {
                    if (GlobalState.enhancedMode) {
                        ArrayList<String> recents = new ArrayList<>();
                        // ADB
                        Collections.addAll(recents, RemoteAPI.getRecents());
                        accessibilityService.recents.setRecents(recents, accessibilityService);
                    }
                    if (action == VITUAL_ACTION_PREV_APP) {
                        String targetApp = accessibilityService.recents.movePrevious();
                        if (targetApp != null) {
                            if (!targetApp.equals(Intent.CATEGORY_HOME)) {
                                intent.putExtra("prev", targetApp);
                            } else {
                                intent.putExtra("home", "prev");
                            }
                        } else {
                            Gesture.toast("<<", Toast.LENGTH_SHORT);
                            return;
                        }
                    } else {
                        String targetApp = accessibilityService.recents.moveNext();
                        if (targetApp != null) {
                            if (!targetApp.equals(Intent.CATEGORY_HOME)) {
                                intent.putExtra("next", targetApp);
                            } else {
                                intent.putExtra("home", "next");
                            }
                        } else {
                            Gesture.toast(">>", Toast.LENGTH_SHORT);
                            return;
                        }
                    }
                    break;
                }
            }
            if (GlobalState.enhancedMode && System.currentTimeMillis() - GlobalState.lastBackHomeTime < 4800) {
                RemoteAPI.fixDelay();
            }
            // ro.miui.ui.version.name=V12
            if (action == GLOBAL_ACTION_HOME && isMiui12 && GlobalState.enhancedMode) {
                Gesture.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME);
                    }
                }, 400);
            }
            accessibilityService.startActivity(intent);
        } catch (Exception ex) {
            Gesture.toast("AppSwitch Exception >> " + ex.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    private static void openApp(AccessibilityServiceGesture accessibilityService, ActionModel action) {
        if (configEx == null) {
            configEx = accessibilityService.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        }

        boolean windowMode = action.actionCode == Handlers.CUSTOM_ACTION_APP_WINDOW;

        String app = configEx.getString((windowMode ? SpfConfigEx.prefix_app_window : SpfConfigEx.prefix_app) + action.exKey, "");
        if (!app.isEmpty()) {
            try {
                Intent intent = AppSwitchActivity.getOpenAppIntent(accessibilityService);
                if (windowMode) {
                    intent.putExtra("app-window", app);
                } else {
                    intent.putExtra("app", app);
                }
                if (GlobalState.enhancedMode && System.currentTimeMillis() - GlobalState.lastBackHomeTime < 4800) {
                    RemoteAPI.fixDelay();
                }
                accessibilityService.startActivity(intent);
                // PendingIntent pendingIntent = PendingIntent.getActivity(accessibilityService.getApplicationContext(), 0, intent, 0);
                // pendingIntent.send();
            } catch (Exception ex) {
                Gesture.toast("AppSwitch Exception >> " + ex.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }

    private static void executeShell(AccessibilityServiceGesture accessibilityService, ActionModel action) {
        if (configEx == null) {
            configEx = accessibilityService.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        }

        String shell = configEx.getString(SpfConfigEx.prefix_shell + action.exKey, "");
        if (!shell.isEmpty()) {
            KeepShellPublic.doCmdSync(shell);
        }
    }

    public static String getOption(int value) {
        for (ActionModel actionModel : options) {
            if (actionModel.actionCode == value) {
                return actionModel.title;
            }
        }
        return "";
    }

    public static ActionModel[] getOptions() {
        return options;
    }
}
