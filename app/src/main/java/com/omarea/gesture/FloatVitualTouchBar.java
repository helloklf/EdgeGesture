package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * 弹窗辅助类
 *
 * @ClassName WindowUtils
 */
class FloatVitualTouchBar {
    private static WindowManager mWindowManager = null;
    private boolean isLandscapf = false;
    // private boolean vibratorOn = false;
    private View bottomView = null;
    private View leftView = null;
    private View rightView = null;
    // private AccessibilityService context;
    private long lastEventTime = 0L;
    private int lastEvent = -1;
    // private Vibrator vibrator;

    public FloatVitualTouchBar(AccessibilityService context, boolean isLandscapf, boolean vibratorOn) {
        // this.context = context;
        this.isLandscapf = isLandscapf;
        // this.vibratorOn = vibratorOn;
        // this.vibrator = (Vibrator) (context.getSystemService(VIBRATOR_SERVICE));

        // 获取WindowManager
        mWindowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));

        try {
            this.bottomView = setBottomView(context);
            this.leftView = setLeftView(context);
            this.rightView = setRightView(context);
        } catch (Exception ex) {
            Log.d("异常", ex.getLocalizedMessage());
            Toast.makeText(context, "启动虚拟导航手势失败！", Toast.LENGTH_LONG).show();
        }
    }

    int getNavBarHeight(Context context) {
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * 隐藏弹出框
     */
    void hidePopupWindow() {
        if (this.bottomView != null) {
            mWindowManager.removeView(this.bottomView);
        }
        if (this.leftView != null) {
            mWindowManager.removeView(this.leftView);
        }
        if (this.rightView != null) {
            mWindowManager.removeView(this.rightView);
        }
        // KeepShellPublic.doCmdSync("wm overscan reset");
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void performGlobalAction(AccessibilityService context, int event) {
        if (isLandscapf && (lastEventTime + 1500 < System.currentTimeMillis() || lastEvent != event)) {
            lastEvent = event;
            lastEventTime = System.currentTimeMillis();
            Toast.makeText(context, "请重复手势~", Toast.LENGTH_SHORT).show();
        } else {
            /*
            if (vibratorOn) {
                vibrator.cancel()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(20, 10), DEFAULT_AMPLITUDE))
                    vibrator.vibrate(VibrationEffect.createOneShot(1, 1))
                } else {
                    vibrator.vibrate(longArrayOf(20, 10), -1)
                }
            }
            */
            context.performGlobalAction(event);
        }
    }

    private View setBottomView(final AccessibilityService context) {
        View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setSize(LayoutParams.MATCH_PARENT, dp2px(context, 8f), TouchBarView.BOTTOM);
        bar.setEventHandler(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_HOME);
            }
        }, new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_RECENTS);
            }
        });

        LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        /*
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int minSize = width;
        int maxSize = height;
        if (height < width) {
            minSize = height;
            maxSize = width;
        }
        */

        params.format = PixelFormat.TRANSLUCENT;
        params.width =  LayoutParams.MATCH_PARENT; // minSize; //
        params.height = LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowManager.addView(view, params);

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setLeftView(final AccessibilityService context) {
        View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);
        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setEventHandler(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }, new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_RECENTS);
            }
        });

        int height = context.getResources().getDisplayMetrics().heightPixels;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int minSize = width;
        int maxSize = height;
        if (height < width) {
            minSize = height;
            maxSize = width;
        }

        if (isLandscapf) {
            bar.setSize(dp2px(context, 12f), (int) (minSize * 0.6), TouchBarView.LEFT);
        } else {
            bar.setSize(dp2px(context, 12f), (int) (minSize * 1.4), TouchBarView.LEFT);
        }

        LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.START | Gravity.BOTTOM;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowManager.addView(view, params);

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setRightView(final AccessibilityService context) {
        View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setEventHandler(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }, new Runnable() {
            @Override
            public void run() {
                performGlobalAction(context, AccessibilityService.GLOBAL_ACTION_RECENTS);
            }
        });

        int height = context.getResources().getDisplayMetrics().heightPixels;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int minSize = width;
        int maxSize = height;
        if (height < width) {
            minSize = height;
            maxSize = width;
        }

        if (isLandscapf) {
            bar.setSize(dp2px(context, 12f), (int) (minSize * 0.6), TouchBarView.RIGHT);
        } else {
            bar.setSize(dp2px(context, 12f), (int) (minSize * 1.4), TouchBarView.RIGHT);
        }

        LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        mWindowManager.addView(view, params);

        return view;
    }
}