package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.ui.FloatVirtualTouchBar;
import com.omarea.gesture.ui.QuickPanel;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.Handlers;
import com.omarea.gesture.util.Recents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AccessibilityServiceGesture extends AccessibilityService {
    public Recents recents = new Recents();
    private FloatVirtualTouchBar floatVitualTouchBar = null;
    private BroadcastReceiver configChanged = null;
    private BroadcastReceiver serviceDisable = null;
    private BroadcastReceiver screenStateReceiver;
    private SharedPreferences appSwitchBlackList;
    private BatteryReceiver batteryReceiver;

    private boolean hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar.hidePopupWindow();
            floatVitualTouchBar = null;
            return true;
        }
        return false;
    }

    private boolean ignored(String packageName) {
        return recents.inputMethods.indexOf(packageName) > -1;
    }

    // 检测应用是否是可以打开的
    private boolean canOpen(String packageName) {
        if (recents.blackList.indexOf(packageName) > -1) {
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

    // 启动器应用（桌面）
    private ArrayList<String> getLauncherApps() {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
        ArrayList<String> launcherApps = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveinfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!("com.android.settings".equals(packageName))) { // MIUI的设置有算个桌面，什么鬼
                launcherApps.add(packageName);
            }
        }
        return launcherApps;
    }

    // 输入法应用
    private ArrayList<String> getInputMethods() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ArrayList<String> inputMethods = new ArrayList<>();
        for (InputMethodInfo inputMethodInfo : imm.getInputMethodList()) {
            inputMethods.add(inputMethodInfo.getPackageName());
        }
        return inputMethods;
    }

    private List<String> colorPolingApps = null; // 允许轮询颜色的APP

    // TODO:判断是否进入全屏状态，以便在游戏和视频过程中降低功耗
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (recents.inputMethods == null) {
            recents.inputMethods = getInputMethods();
            recents.launcherApps = getLauncherApps();
        }
        if (event == null) {
            return;
        }
        int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (colorPolingApps != null && GlobalState.updateBar != null && !GlobalState.useBatteryCapacity) {
                CharSequence packageName = event.getPackageName();
                if (packageName != null) {
                    if (colorPolingApps.indexOf(packageName.toString()) > -1) { // 抖音APP
                        startColorPolling();
                    }
                }
            }
        }
        else if (eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            List<AccessibilityWindowInfo> windowInfos = getWindows();
            AccessibilityWindowInfo lastWindow = null;

            // TODO:
            //      此前在MIUI系统上测试，只判定全屏显示（即窗口大小和屏幕分辨率完全一致）的应用，逻辑非常准确
            //      但在类原生系统上表现并不好，例如：有缺口的屏幕或有导航键的系统，报告的窗口大小则可能不包括缺口高度区域和导航键区域高度
            //      因此，现在将逻辑调整为：从所有应用窗口中选出最接近全屏的一个，判定为前台应用
            //      当然，这并不意味着完美，只是暂时没有更好的解决方案……

            int lastWindowSize = 0;
            for (AccessibilityWindowInfo windowInfo : windowInfos) {
                if ((!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && windowInfo.isInPictureInPictureMode())) && (windowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION)) {
                    Rect outBounds = new Rect();
                    windowInfo.getBoundsInScreen(outBounds);
                    int size = (outBounds.right - outBounds.left) * (outBounds.bottom - outBounds.top);

                    if (size >= lastWindowSize) {
                        lastWindow = windowInfo;
                        lastWindowSize = size;
                    }
                }
            }

            if (lastWindow != null) {
                lastParsingThread = System.currentTimeMillis();
                /*
                if (event.getPackageName() == null) {
                    Log.e(">>>>G", " " +event);
                }
                */
                Thread thread = new WindowParsingThread(lastWindow, lastParsingThread, event.getWindowId(), event.getPackageName());
                thread.start();
            }
        }
    }

    private long lastParsingThread = 0;

    private class WindowParsingThread extends Thread {
        private AccessibilityWindowInfo windowInfo;
        private long tid;
        private int eventWindowId;
        private CharSequence eventPackageName;
        private WindowParsingThread(AccessibilityWindowInfo windowInfo, long tid, int eventWindowId, CharSequence eventPackageName) {
            this.windowInfo = windowInfo;
            this.tid = tid;
            this.eventWindowId = eventWindowId;
            this.eventPackageName = eventPackageName;
        }

        @Override
        public void run() {
            if (windowInfo != null) {
                CharSequence packageName;
                if (eventWindowId == windowInfo.getId() && eventPackageName != null) {
                    packageName = eventPackageName;
                } else {
                    // 如果当前window锁属的APP处于未响应状态，此过程可能会等待5秒后超时返回null，因此需要在线程中异步进行此操作
                    AccessibilityNodeInfo root;

                    try {
                        root = windowInfo.getRoot();
                    } catch (Exception ex) {
                        root = null;
                    }

                    if (root == null) {
                        return;
                    }

                    packageName = root.getPackageName();
                }
                if (packageName == null) {
                    return;
                }

                String packageNameStr = packageName.toString();
                // Log.d(">>>>", "To " + packageNameStr);
                if (lastParsingThread == tid) {
                    if (!packageNameStr.equals(getPackageName())) {
                        if (recents.launcherApps.contains(packageNameStr)) {
                            recents.addRecent(Intent.CATEGORY_HOME);
                            GlobalState.lastBackHomeTime = System.currentTimeMillis();
                        } else if (!ignored(packageNameStr) && canOpen(packageNameStr) && !appSwitchBlackList.contains(packageNameStr)) {
                            recents.addRecent(packageNameStr);
                            GlobalState.lastBackHomeTime = 0;
                        }
                        stopColorPolling();
                    }

                    // TODO:思考逻辑合理性
                    if (!(GlobalState.updateBar == null || GlobalState.useBatteryCapacity || packageNameStr.equals("com.android.systemui"))) {
                        if (!(packageNameStr.equals("android") || packageNameStr.equals("com.omarea.filter"))) {
                            WhiteBarColor.updateBarColorMultiple();
                        }
                    }
                }
            }
        }
    }

    private Timer pollingTimer = null;   // 轮询定时器
    private long lastEventTime = 0;      // 最后一次触发事件的时间
    private final long pollingTimeout = 10000; // 轮询超时时间
    private final long pollingInterval = 1000; // 轮询间隔
    private void startColorPolling() {
        lastEventTime = System.currentTimeMillis();
        if (pollingTimer == null) {
            pollingTimer = new Timer();
            pollingTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - lastEventTime < pollingTimeout) {
                        WhiteBarColor.updateBarColorMultiple();
                    } else {
                        stopColorPolling();
                    }
                }
            }, 0, pollingInterval);
        }
    }

    private void stopColorPolling() {
        if (pollingTimer != null) {
            pollingTimer.cancel();
            // pollingTimer.purge();
            pollingTimer = null;
        }
    }

    private void setBatteryReceiver() {
        if (batteryReceiver == null) {
            batteryReceiver = new BatteryReceiver(this);
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        setServiceInfo();

        if (appSwitchBlackList == null) {
            appSwitchBlackList = getSharedPreferences(SpfConfig.AppSwitchBlackList, Context.MODE_PRIVATE);
        }

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        GlobalState.displayWidth = point.x;
        GlobalState.displayHeight = point.y;
        GlobalState.consecutive = Gesture.config.getBoolean(SpfConfig.IOS_BAR_CONSECUTIVE, SpfConfig.IOS_BAR_CONSECUTIVE_DEFAULT);

        GlobalState.useBatteryCapacity = Gesture.config.getBoolean(SpfConfig.IOS_BAR_POP_BATTERY, SpfConfig.IOS_BAR_POP_BATTERY_DEFAULT);
        if (GlobalState.useBatteryCapacity) {
            setBatteryReceiver();
        }

        if (configChanged == null) {
            configChanged = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    GlobalState.consecutive = Gesture.config.getBoolean(SpfConfig.IOS_BAR_CONSECUTIVE, SpfConfig.IOS_BAR_CONSECUTIVE_DEFAULT);
                    GlobalState.useBatteryCapacity = Gesture.config.getBoolean(SpfConfig.IOS_BAR_POP_BATTERY, SpfConfig.IOS_BAR_POP_BATTERY_DEFAULT);
                    if (GlobalState.useBatteryCapacity) {
                        setBatteryReceiver();
                    } else if (batteryReceiver != null) {
                        unregisterReceiver(batteryReceiver);
                        batteryReceiver = null;
                    }

                    String action = intent != null ? intent.getAction() : null;
                    if (action != null && action.equals(getString(R.string.app_switch_changed))) {
                        if (recents != null) {
                            recents.clear();
                            Gesture.toast("OK！", Toast.LENGTH_SHORT);
                        }
                    } else {
                        new AdbProcessExtractor().updateAdbProcessState(context, false);
                        if (action != null && action.equals(getString(R.string.action_adb_process))) {
                            if (GlobalState.enhancedMode) {
                                setResultCode(0);
                                setResultData("Nice, The enhancement mode has been activated ^_^");
                            } else {
                                setResultCode(5);
                                setResultData("Unable to start enhanced mode >_<");
                            }
                        }
                        createPopupView(false);
                    }
                }
            };

            registerReceiver(configChanged, new IntentFilter(getString(R.string.action_config_changed)));
            registerReceiver(configChanged, new IntentFilter(getString(R.string.app_switch_changed)));
            registerReceiver(configChanged, new IntentFilter(getString(R.string.action_adb_process)));
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
        createPopupView(false);

        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        }
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));

        Collections.addAll(recents.blackList, getResources().getStringArray(R.array.app_switch_black_list));

        new AdbProcessExtractor().updateAdbProcessState(this, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String results = RemoteAPI.getColorPollingApps();
                if (results != null) {
                    colorPolingApps = Arrays.asList(results.split("\n"));
                    Gesture.config.edit().putString("color_polling_apps", results).apply();
                    setServiceInfo();
                } else {
                    colorPolingApps = Arrays.asList(Gesture.config.getString("color_polling_apps", "").split("\n"));
                }
            }
        }).start();
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
            // 关闭常用应用面板
            QuickPanel.close();

            GlobalState.isLandscapf = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

            // 如果分辨率变了，那就重新创建手势区域
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Point point = new Point();
            wm.getDefaultDisplay().getRealSize(point);
            if (point.x != GlobalState.displayWidth || point.y != GlobalState.displayHeight) {
                GlobalState.displayWidth = point.x;
                GlobalState.displayHeight = point.y;
                createPopupView(true);
            }
        }
    }

    private void createPopupView(boolean delayed) {
        final AccessibilityServiceGesture context = this;

        new android.os.Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                hidePopupWindow();
                setServiceInfo();
                floatVitualTouchBar = new FloatVirtualTouchBar(context);
            }
        }, (delayed ? 500 : 0));
    }

    private void setServiceInfo() {
        AccessibilityServiceInfo accessibilityServiceInfo = getServiceInfo();
        // accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        // accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        if ((!Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) && colorPolingApps != null && colorPolingApps.size() > 0) {
            // accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
            accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        } else {
            // accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED;
            accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        }
        setServiceInfo(accessibilityServiceInfo);
    }

    @Override
    public void onDestroy() {
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

        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        // stopForeground(true);
        super.onDestroy();
    }
}
