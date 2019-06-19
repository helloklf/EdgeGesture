package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class AccessibilityServiceKeyEvent extends AccessibilityService {
    boolean isLandscapf = false;
    private FloatVitualTouchBar floatVitualTouchBar = null;
    private BroadcastReceiver configChanged = null;

    private void hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
            floatVitualTouchBar = null;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.d("onAccessibilityEvent", event.getPackageName().toString());
        CharSequence packageName = event.getPackageName();
        if (packageName != null) {
            AppHistory.putHistory(packageName.toString());
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        TouchIconCache.setContext(this.getBaseContext());
        AppHistory.initConfig(this.getApplicationContext());

        if (configChanged == null) {
            configChanged = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    createPopupView();
                }
            };

            registerReceiver(configChanged, new IntentFilter(getString(R.string.action_config_changed)));
        }
        createPopupView();
        InputMethodManager inputMethodManager = (InputMethodManager)(getSystemService(Context.INPUT_METHOD_SERVICE));
        for (InputMethodInfo inputMethod : inputMethodManager.getInputMethodList()) {
            AppHistory.putBlackList(inputMethod.getPackageName());
        }
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

        SharedPreferences config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        boolean panelEnabled = config.getAll().containsValue(Handlers.VITUAL_ACTION_RECENT_LIST);
        AccessibilityServiceInfo accessibilityServiceInfo = getServiceInfo();
        if (panelEnabled) {
            if (accessibilityServiceInfo.packageNames != null) {
                accessibilityServiceInfo.packageNames = null;
                setServiceInfo(accessibilityServiceInfo);
            }
        } else {
            if (accessibilityServiceInfo.packageNames == null) {
                accessibilityServiceInfo.packageNames = new String[]{};
                setServiceInfo(accessibilityServiceInfo);
            }
        }
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
    }
}
