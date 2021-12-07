package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.omarea.gesture.model.ActionModel;
import com.omarea.gesture.daemon.RemoteAPI;
import com.omarea.gesture.shell.KeepShellPublic;
import com.omarea.gesture.ui.QuickPanel;
import com.omarea.gesture.util.AppLauncher;
import com.omarea.gesture.util.AppWindowed;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.SystemProperty;

import java.util.ArrayList;
import java.util.Collections;

public class GestureActions {
    private Context context;
    public GestureActions(final Context context) {
        this.context = context;
        options = options = new ArrayList<ActionModel>() {{
            add(new ActionModel(GLOBAL_ACTION_NONE, context.getString(R.string.action_none)));
            add(new ActionModel(GLOBAL_ACTION_BACK, context.getString(R.string.action_back)));
            add(new ActionModel(GLOBAL_ACTION_HOME, context.getString(R.string.action_home)));
            add(new ActionModel(GLOBAL_ACTION_RECENTS, context.getString(R.string.action_recent)));
            add(new ActionModel(GLOBAL_ACTION_NOTIFICATIONS, context.getString(R.string.action_notification)));
            add(new ActionModel(GLOBAL_ACTION_QUICK_SETTINGS, context.getString(R.string.action_quick_settings)));
            add(new ActionModel(GLOBAL_ACTION_POWER_DIALOG, context.getString(R.string.action_power_menu)));
            add(new ActionModel(VITUAL_ACTION_PREV_APP, context.getString(R.string.action_prev_app)));
            add(new ActionModel(VITUAL_ACTION_NEXT_APP, context.getString(R.string.action_next_app)));

            if (Build.VERSION.SDK_INT > 23) {
                add(new ActionModel(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN, context.getString(R.string.action_split_screen)));
                add(new ActionModel(VITUAL_ACTION_SWITCH_APP, context.getString(R.string.action_prev_simulation)));
                add(new ActionModel(VITUAL_ACTION_FORM, context.getString(R.string.action_windowed)));
            }

            if (Build.VERSION.SDK_INT > 27) {
                add(new ActionModel(GLOBAL_ACTION_LOCK_SCREEN, context.getString(R.string.action_lock_screen)));
                add(new ActionModel(GLOBAL_ACTION_TAKE_SCREENSHOT, context.getString(R.string.action_screenshot)));
            }

            add(new ActionModel(CUSTOM_ACTION_APP, context.getString(R.string.action_launch_app)));
            if (Build.VERSION.SDK_INT > 23) {
                add(new ActionModel(CUSTOM_ACTION_APP_WINDOW, context.getString(R.string.action_launch_app_windowed)));
            }

            add(new ActionModel(CUSTOM_ACTION_SHELL, context.getString(R.string.action_exec_shell)));
            add(new ActionModel(CUSTOM_ACTION_QUICK, context.getString(R.string.action_quick_apps)));
            add(new ActionModel(OMAREA_FILTER_SCREENSHOT, context.getString(R.string.action_filter_screenshot)));
        }};
    }

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
    final public static int VITUAL_ACTION_MI_HANDY_MODE_1 = 900100;
    final public static int VITUAL_ACTION_MI_HANDY_MODE_2 = 900101;

    final public static int CUSTOM_ACTION_APP = 1000001;
    final public static int CUSTOM_ACTION_APP_WINDOW = 1000002;
    final public static int CUSTOM_ACTION_SHELL = 1000006;
    final public static int CUSTOM_ACTION_QUICK = 1000009;
    final public static int OMAREA_FILTER_SCREENSHOT = 1100000;

    private final boolean isXiaomi = Build.MANUFACTURER.toLowerCase().equals("xiaomi") && (Build.BRAND.toLowerCase().equals("xiaomi") || Build.BRAND.toLowerCase().equals("redmi"));

    private ArrayList<ActionModel> options;
    private SharedPreferences configEx;
    private final boolean isMiui12 = new SystemProperty().isMIUI12();

    // 获取动画模式
    private int getAnimationRes(final ActionModel action) {
        if (GlobalState.consecutiveAction != null) {
            return SpfConfig.ANIMATION_FAST;
        } else if (Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) {
            return SpfConfig.ANIMATION_DEFAULT;
        } else if (action.actionCode == GLOBAL_ACTION_HOME) {
            return Gesture.config.getInt(SpfConfig.BACK_HOME_ANIMATION, SpfConfig.ANIMATION_DEFAULT);
        } else {
            return Gesture.config.getInt(SpfConfig.APP_SWITCH_ANIMATION, SpfConfig.ANIMATION_DEFAULT);
        }
    }

    // FIXME:
    // <uses-permission android:name="android.permission.STOP_APP_SWITCHES" />
    // 由于Google限制，再按下Home键以后，后台应用如果想要打开Activity则需要等待5秒，参考 stopAppSwitches 相关逻辑
    // 这导致应用切换手势和打开应用的操作变得体验很差
    // 目前还没找到解决办法
    public void executeVirtualAction(
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
                if ((action.actionCode == GLOBAL_ACTION_HOME  && (isMiui12 && GlobalState.isLandscape))) {
                    accessibilityService.performGlobalAction(action.actionCode);
                } else if (Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) {
                    lowPowerModeAppSwitch(accessibilityService, action.actionCode);
                } else {
                    int animation = getAnimationRes(action);

                    if (action.actionCode == GLOBAL_ACTION_HOME && animation == SpfConfig.ANIMATION_DEFAULT) {
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
                filterScreenShot(accessibilityService);
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
            case VITUAL_ACTION_MI_HANDY_MODE_1: {
                RemoteAPI.xiaomiHandymode(1);
                break;
            }
            case VITUAL_ACTION_MI_HANDY_MODE_2: {
                RemoteAPI.xiaomiHandymode(2);
                break;
            }
            default: {
                accessibilityService.performGlobalAction(action.actionCode);
                break;
            }
        }
    }

    private void filterScreenShot(final AccessibilityServiceGesture accessibilityServiceGesture) {
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

    private void openQuickPanel(final AccessibilityServiceGesture accessibilityService, float touchRawX, float touchRawY) {
        new QuickPanel(accessibilityService).open(touchRawX, touchRawY);
    }

    private Intent getAppSwitchIntent(String appPackageName) {
        Intent i = Gesture.context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        i.setFlags((i.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        i.setPackage(null);
        return i;
    }

    private void lowPowerModeAppSwitch(final AccessibilityServiceGesture service, final int action) {
        switch (action) {
            case GLOBAL_ACTION_HOME: {
                service.performGlobalAction(GLOBAL_ACTION_HOME);
                break;
            }
            case VITUAL_ACTION_NEXT_APP: {
                updateRecent(service);
                String targetApp = service.recents.moveNext();
                if (targetApp != null) {
                    new AppLauncher().startActivity(service, targetApp);
                } else {
                    if (Gesture.config.getBoolean(SpfConfig.WINDOW_WATCH, SpfConfig.WINDOW_WATCH_DEFAULT)) {
                        Gesture.toast(">>", Toast.LENGTH_SHORT);
                    } else {
                        Gesture.toast(service.getString(R.string.window_watch_disabled), Toast.LENGTH_LONG);
                    }
                }
                break;
            }
            case VITUAL_ACTION_PREV_APP: {
                String targetApp = service.recents.movePrevious();
                if (targetApp != null) {
                    new AppLauncher().startActivity(service, targetApp);
                } else {
                    if (Gesture.config.getBoolean(SpfConfig.WINDOW_WATCH, SpfConfig.WINDOW_WATCH_DEFAULT)) {
                        Gesture.toast("<<", Toast.LENGTH_SHORT);
                    } else {
                        Gesture.toast(service.getString(R.string.window_watch_disabled), Toast.LENGTH_LONG);
                    }
                }
                break;
            }
            case VITUAL_ACTION_FORM: {
                new AppWindowed().switchToFreeForm(service, service.recents.getCurrent());
                break;
            }
        }
    }

    private void updateRecent(final AccessibilityServiceGesture service) {
        if (GlobalState.enhancedMode) {
            ArrayList<String> recent = new ArrayList<>();
            // ADB
            Collections.addAll(recent, RemoteAPI.getRecents());
            service.recents.setRecents(recent);
        }
    }

    private void appSwitch(final AccessibilityServiceGesture service, final int action, final int animation) {
        try {
            Intent intent = AppSwitchActivity.getOpenAppIntent(service);
            intent.putExtra("animation", animation);

            switch (action) {
                case GLOBAL_ACTION_HOME: {
                    intent.putExtra("home", "");
                    // AppSwitchActivity.backHome(service);
                    break;
                }
                case VITUAL_ACTION_FORM: {
                    String current = service.recents.getCurrent();
                    if (current.isEmpty()) {
                        if (Gesture.config.getBoolean(SpfConfig.WINDOW_WATCH, SpfConfig.WINDOW_WATCH_DEFAULT)) {
                            Gesture.toast("□", Toast.LENGTH_SHORT);
                        } else {
                            Gesture.toast(service.getString(R.string.window_watch_disabled), Toast.LENGTH_LONG);
                        }
                        return;
                    } else {
                        intent.putExtra("form", current);
                    }
                    break;
                }
                case VITUAL_ACTION_PREV_APP:
                case VITUAL_ACTION_NEXT_APP: {
                    updateRecent(service);
                    if (action == VITUAL_ACTION_PREV_APP) {
                        String targetApp = service.recents.movePrevious();
                        if (targetApp != null) {
                            // Log.d(">>>>", targetApp);
                            intent.putExtra("prev", targetApp);
                        } else {
                            if (Gesture.config.getBoolean(SpfConfig.WINDOW_WATCH, SpfConfig.WINDOW_WATCH_DEFAULT)) {
                                Gesture.toast("<<", Toast.LENGTH_SHORT);
                            } else {
                                Gesture.toast(service.getString(R.string.window_watch_disabled), Toast.LENGTH_LONG);
                            }
                            return;
                        }
                    } else {
                        String targetApp = service.recents.moveNext();
                        if (targetApp != null) {
                            // Log.d(">>>>", targetApp);
                            intent.putExtra("next", targetApp);
                        } else {
                            if (Gesture.config.getBoolean(SpfConfig.WINDOW_WATCH, SpfConfig.WINDOW_WATCH_DEFAULT)) {
                                Gesture.toast(">>", Toast.LENGTH_SHORT);
                            } else {
                                Gesture.toast(service.getString(R.string.window_watch_disabled), Toast.LENGTH_LONG);
                            }
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
                    service.performGlobalAction(GLOBAL_ACTION_HOME);
                    }
                }, 400);
            }
            service.startActivity(intent);
        } catch (Exception ex) {
            Gesture.toast("AppSwitch Exception >> " + ex.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    private void openApp(AccessibilityServiceGesture service, ActionModel action) {
        if (configEx == null) {
            configEx = service.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        }

        boolean windowMode = action.actionCode == GestureActions.CUSTOM_ACTION_APP_WINDOW;

        String app = configEx.getString((windowMode ? SpfConfigEx.prefix_app_window : SpfConfigEx.prefix_app) + action.exKey, "");
        if (!app.isEmpty()) {
            try {
                Intent intent = AppSwitchActivity.getOpenAppIntent(service);
                if (windowMode) {
                    intent.putExtra("app-window", app);
                } else {
                    intent.putExtra("app", app);
                }
                if (GlobalState.enhancedMode && System.currentTimeMillis() - GlobalState.lastBackHomeTime < 4800) {
                    RemoteAPI.fixDelay();
                }
                service.startActivity(intent);
                // PendingIntent pendingIntent = PendingIntent.getActivity(service.getApplicationContext(), 0, intent, 0);
                // pendingIntent.send();
            } catch (Exception ex) {
                Gesture.toast("AppSwitch Exception >> " + ex.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }

    private void executeShell(AccessibilityServiceGesture service, ActionModel action) {
        if (configEx == null) {
            configEx = service.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        }

        String shell = configEx.getString(SpfConfigEx.prefix_shell + action.exKey, "");
        if (!shell.isEmpty()) {
            KeepShellPublic.doCmdSync(shell);
        }
    }

    public String getOption(int value) {
        for (ActionModel actionModel : getOptions()) {
            if (actionModel.actionCode == value) {
                return actionModel.title;
            }
        }
        return "";
    }

    public ActionModel[] getOptions() {
        ArrayList<ActionModel> items = new ArrayList<ActionModel>(){{
            addAll(options);
            if (isXiaomi && GlobalState.enhancedMode) {
                add(new ActionModel(VITUAL_ACTION_MI_HANDY_MODE_1, context.getString(R.string.single_hand_model_left)));
                add(new ActionModel(VITUAL_ACTION_MI_HANDY_MODE_2, context.getString(R.string.single_hand_model_right)));
            }
        }};
        return items.toArray(new ActionModel[0]);
    }
}
