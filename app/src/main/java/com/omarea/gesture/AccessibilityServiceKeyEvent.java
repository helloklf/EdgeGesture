package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityServiceKeyEvent extends AccessibilityService {
    boolean isLandscapf = false;
    private FloatVirtualTouchBar floatVitualTouchBar = null;
    private BroadcastReceiver configChanged = null;
    private BroadcastReceiver serviceDisable = null;
    private Handler handler = new Handler();
    private BroadcastReceiver screenStateReceiver;
    private SharedPreferences config;

    private void hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
            floatVitualTouchBar = null;
        }
    }

    private ContentResolver cr = null;

    private void forceHideNavBar() {
        if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (cr == null) {
                cr = getContentResolver();
            }

            if (config == null) {
                config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
            }

            if (config.getBoolean(SpfConfig.SAMSUNG_OPTIMIZE, SpfConfig.SAMSUNG_OPTIMIZE_DEFAULT)) {
                new ForceHideNavBarThread(cr).run();
            }
        }
    }

    private void resumeNavBar() {
        if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (cr == null) {
                    cr = getContentResolver();
                }
                Settings.Global.putInt(cr, "navigation_bar_gesture_disabled_by_policy", 0); // oneui 策略取消强制禁用手势（因为锁屏唤醒后底部会触摸失灵，需要重新开关）
            } catch (Exception ex) {
            }
            cr = null;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*
        Log.d("onAccessibilityEvent", "onAccessibilityEvent");
        // Log.d("onAccessibilityEvent", event.getPackageName().toString());
        CharSequence packageName = event.getPackageName();
        if (packageName != null) {
            AppHistory.putHistory(packageName.toString());
        }
        */
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
                    if (action != null && ((action.equals(Intent.ACTION_USER_PRESENT)
                            || action.equals(Intent.ACTION_USER_UNLOCKED))
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

    private int screenWidth;
    private int screenHeight;

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
