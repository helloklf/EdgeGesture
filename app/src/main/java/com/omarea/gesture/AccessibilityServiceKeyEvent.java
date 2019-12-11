package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.omarea.gesture.util.ForceHideNavBarThread;
import com.omarea.gesture.util.Overscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityServiceKeyEvent extends AccessibilityService {
    public Recents recents = new Recents();
    boolean isLandscapf = false;
    private FloatVirtualTouchBar floatVitualTouchBar = null;
    private BroadcastReceiver configChanged = null;
    private BroadcastReceiver serviceDisable = null;
    private BroadcastReceiver screenStateReceiver;
    private SharedPreferences config;
    private ContentResolver cr = null;
    private int screenWidth;
    private int screenHeight;

    private void hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
            floatVitualTouchBar = null;
        }
    }

    private void forceHideNavBar() {
        if (config == null) {
            config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        }

        if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (cr == null) {
                cr = getContentResolver();
            }
            if (config.getBoolean(SpfConfig.SAMSUNG_OPTIMIZE, SpfConfig.SAMSUNG_OPTIMIZE_DEFAULT)) {
                new ForceHideNavBarThread(cr).run();
            }
        } else {
            if (config.getBoolean(SpfConfig.OVERSCAN_SWITCH, SpfConfig.OVERSCAN_SWITCH_DEFAULT)) {
                new Overscan().setOverscan(this);
            }
        }
    }

    private void resumeNavBar() {
        if (config == null) {
            config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        }

        if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (cr == null) {
                    cr = getContentResolver();
                }
                // oneui 策略取消强制禁用手势（因为锁屏唤醒后底部会触摸失灵，需要重新开关）
                Settings.Global.putInt(cr, "navigation_bar_gesture_disabled_by_policy", 0);
            } catch (Exception ex) {
            }
            cr = null;
        } else {
            if (config.getBoolean(SpfConfig.OVERSCAN_SWITCH, SpfConfig.OVERSCAN_SWITCH_DEFAULT)) {
                new Overscan().setOverscan(this);
            }
        }
    }

    // 检测应用是否是可以打开的
    private boolean canOpen(String packageName) {
        if (recents.blackList.indexOf(packageName) > -1 || recents.ignoreApps.indexOf(packageName) > -1) {
            return false;
        } else if (recents.whiteList.indexOf(packageName) > -1) {
            return true;
        } else {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                recents.whiteList.add(packageName);
                return true;
            } else {
                recents.blackList.add(packageName);
                return false;
            }
        }
    }

    /*
    通过
    dumpsys activity top | grep ACTIVITY
    可以获取当前打开的应用，但是，作为普通应用并且有这个权限
    */

    /*
    List<AccessibilityWindowInfo> windowInfos = accessibilityService.getWindows();
    Log.d("AccessibilityWindowInfo", "windowInfos " + windowInfos.size());
    for (AccessibilityWindowInfo windowInfo : windowInfos) {
        try {
            Log.d("AccessibilityWindowInfo", "" + windowInfo.getRoot().getPackageName());
        } catch (Exception ex) {
            Log.e("AccessibilityWindowInfo", "" + ex.getMessage());
        }
    }
    */

    // 启动器应用（桌面）
    private ArrayList<String> getLauncherApps() {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
        ArrayList<String> launcherApps = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveinfoList) {
            launcherApps.add(resolveInfo.activityInfo.packageName);
        }
        return launcherApps;
    }

    // 输入法应用
    private ArrayList<String> getInputMethods() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ArrayList<String> inputMethods = new ArrayList<>();
        for (InputMethodInfo inputMethodInfo : imm.getInputMethodList()) {
            recents.ignoreApps.add(inputMethodInfo.getPackageName());
        }
        return inputMethods;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.d("onAccessibilityEvent", event.getPackageName().toString());
        if (event != null) {
            CharSequence packageName = event.getPackageName();
            if (packageName != null && !packageName.equals(getPackageName())) {
                String packageNameStr = packageName.toString();
                if (recents.ignoreApps == null) {
                    recents.ignoreApps = getLauncherApps();
                    recents.ignoreApps.addAll(getInputMethods());
                }
                if (canOpen(packageNameStr)) {
                    recents.addRecent(packageNameStr);
                }
            }
        }
    }

    private boolean isScreenLocked() {
        try {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            if (display.getState() != Display.STATE_ON) {
                return true;
            }

            KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return mKeyguardManager.inKeyguardRestrictedInputMode() || mKeyguardManager.isDeviceLocked() || mKeyguardManager.isKeyguardLocked();
            } else {
                return mKeyguardManager.inKeyguardRestrictedInputMode() || mKeyguardManager.isKeyguardLocked();
            }
        } catch (Exception ex) {
            return true;
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        screenWidth = point.x;
        screenHeight = point.y;

        TouchIconCache.setContext(this.getBaseContext());

        if (configChanged == null) {
            configChanged = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    createPopupView();
                }
            };

            registerReceiver(configChanged, new IntentFilter(getString(R.string.action_config_changed)));
        }
        if (serviceDisable == null) {
            serviceDisable = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        disableSelf();
                    }
                    stopSelf();
                }
            };
            registerReceiver(serviceDisable, new IntentFilter(getString(R.string.action_service_disable)));
        }
        createPopupView();

        screenStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null &&
                            ((action.equals(Intent.ACTION_USER_PRESENT) || action.equals(Intent.ACTION_USER_UNLOCKED))
                            // || action.equals(Intent.ACTION_SCREEN_ON)
                    )) {
                        forceHideNavBar();
                    }
                }
            }
        };
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        }
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
        forceHideNavBar();

        Collections.addAll(recents.blackList, getResources().getStringArray(R.array.app_switch_black_list));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        hidePopupWindow();
        resumeNavBar();
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {

    }

    // 监测屏幕旋转
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (floatVitualTouchBar != null && newConfig != null) {
            isLandscapf = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

            // 如果分辨率变了，那就重新创建手势区域
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Point point = new Point();
            wm.getDefaultDisplay().getRealSize(point);
            if (point.x != screenWidth || point.y != screenHeight) {
                screenWidth = point.x;
                screenHeight = point.y;
                createPopupView();
                forceHideNavBar();
            }
        }
    }

    private void createPopupView() {
        hidePopupWindow();
        floatVitualTouchBar = new FloatVirtualTouchBar(this, isLandscapf);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
        }

        if (configChanged != null) {
            unregisterReceiver(configChanged);
            configChanged = null;
        }

        if (screenStateReceiver != null) {
            unregisterReceiver(screenStateReceiver);
            screenStateReceiver = null;
        }
    }
}
