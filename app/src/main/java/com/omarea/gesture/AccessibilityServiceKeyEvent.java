package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityServiceKeyEvent extends AccessibilityService {
    boolean isLandscapf = false;
    private FloatVitualTouchBar floatVitualTouchBar = null;
    private BroadcastReceiver configChanged = null;
    private BroadcastReceiver serviceDisable = null;
    private Handler handler = new Handler();
    private BroadcastReceiver screenStateReceiver;

    private void hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
            floatVitualTouchBar = null;
        }
    }

    private void forceHideNavBar() {
        try {
            if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ContentResolver cr = getContentResolver();
                // Samsung
                Settings.Global.putInt(cr, "navigation_bar_gesture_while_hidden", 1); // oneui 开启手势模式
                Settings.Global.putInt(cr, "navigation_bar_gesture_hint", 0); // oneui 隐藏手势提示
                Settings.Global.putInt(cr, "navigation_bar_gesture_disabled_by_policy", 0); // oneui 策略取消强制禁用手势（因为锁屏唤醒后底部会触摸失灵，需要重新开关）
                Thread.sleep(200);
                Settings.Global.putInt(cr, "navigation_bar_gesture_disabled_by_policy", 1); // oneui 策略强制禁用手势
            }
        } catch (java.lang.Exception ex) {
            Log.e("forceHideNavBar", "" + ex.getMessage());
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
                    if (action == Intent.ACTION_USER_PRESENT ||
                        action == Intent.ACTION_USER_UNLOCKED ||
                        action == Intent.ACTION_SCREEN_ON) {
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
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                isLandscapf = false;
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                isLandscapf = true;
            }
            createPopupView();
        }
    }

    private void createPopupView() {
        hidePopupWindow();
        floatVitualTouchBar = new FloatVitualTouchBar(this, isLandscapf);
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
