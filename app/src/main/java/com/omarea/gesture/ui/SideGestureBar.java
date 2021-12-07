package com.omarea.gesture.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.omarea.gesture.AccessibilityServiceGesture;
import com.omarea.gesture.model.ActionModel;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.Memory;

public class SideGestureBar {
    private static WindowManager mWindowManager = null;
    private boolean islandscape;
    private View visualFeedbackView = null;
    private View iosBarView = null;
    private View bottomView = null;
    private View leftView = null;
    private View rightView = null;
    private SharedPreferences config;

    public SideGestureBar(AccessibilityServiceGesture context) {
        this.islandscape = GlobalState.isLandscape;

        config = context.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        mWindowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        try {
            boolean anySide = false;
            if (islandscape) {
                if (config.getBoolean(SpfConfig.THREE_SECTION_LANDSCAPE, SpfConfig.THREE_SECTION_LANDSCAPE_DEFAULT)) {
                    anySide = true;
                    this.bottomView = setThreeSectionView(context);
                } else if (config.getBoolean(SpfConfig.CONFIG_BOTTOM_ALLOW_LANDSCAPE, SpfConfig.CONFIG_BOTTOM_ALLOW_LANDSCAPE_DEFAULT)) {
                    anySide = true;
                    this.bottomView = setBottomView(context);
                }
                if (config.getBoolean(SpfConfig.CONFIG_LEFT_ALLOW_LANDSCAPE, SpfConfig.CONFIG_LEFT_ALLOW_LANDSCAPE_DEFAULT)) {
                    anySide = true;
                    this.leftView = setLeftView(context);
                }
                if (config.getBoolean(SpfConfig.CONFIG_RIGHT_ALLOW_LANDSCAPE, SpfConfig.CONFIG_RIGHT_ALLOW_LANDSCAPE_DEFAULT)) {
                    anySide = true;
                    this.rightView = setRightView(context);
                }
                if (config.getBoolean(SpfConfig.LANDSCAPE_IOS_BAR, SpfConfig.LANDSCAPE_IOS_BAR_DEFAULT)) {
                    this.iosBarView = new iOSWhiteBar(context, islandscape).getView();
                }
            } else {
                if (config.getBoolean(SpfConfig.THREE_SECTION_PORTRAIT, SpfConfig.THREE_SECTION_PORTRAIT_DEFAULT)) {
                    anySide = true;
                    this.bottomView = setThreeSectionView(context);
                } else if (config.getBoolean(SpfConfig.CONFIG_BOTTOM_ALLOW_PORTRAIT, SpfConfig.CONFIG_BOTTOM_ALLOW_PORTRAIT_DEFAULT)) {
                    anySide = true;
                    this.bottomView = setBottomView(context);
                }
                if (config.getBoolean(SpfConfig.CONFIG_LEFT_ALLOW_PORTRAIT, SpfConfig.CONFIG_LEFT_ALLOW_PORTRAIT_DEFAULT)) {
                    anySide = true;
                    this.leftView = setLeftView(context);
                }
                if (config.getBoolean(SpfConfig.CONFIG_RIGHT_ALLOW_PORTRAIT, SpfConfig.CONFIG_RIGHT_ALLOW_PORTRAIT_DEFAULT)) {
                    anySide = true;
                    this.rightView = setRightView(context);
                }
                if (config.getBoolean(SpfConfig.PORTRAIT_IOS_BAR, SpfConfig.PORTRAIT_IOS_BAR_DEFAULT)) {
                    this.iosBarView = new iOSWhiteBar(context, islandscape).getView();
                }
            }
            if (anySide && !config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) {
                this.visualFeedbackView = setVisualFeedbackView(context);
            }
        } catch (Exception ex) {
            Log.e(">>>>", "启动虚拟导航手势失败 " + ex.getMessage());
            Gesture.toast("启动虚拟导航手势失败！\n" + ex.getMessage(), Toast.LENGTH_LONG);
            // throw  ex;
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
    public void removeGestureView() {
        if (this.iosBarView != null) {
            mWindowManager.removeView(this.iosBarView);
            this.iosBarView = null;
            GlobalState.iosBarColor = Integer.MIN_VALUE;
            GlobalState.updateBar = null;
        }
        if (this.bottomView != null) {
            mWindowManager.removeView(this.bottomView);
            this.bottomView = null;
        }
        if (this.leftView != null) {
            mWindowManager.removeView(this.leftView);
            this.leftView = null;
        }
        if (this.rightView != null) {
            mWindowManager.removeView(this.rightView);
            this.rightView = null;
        }
        if (this.visualFeedbackView != null) {
            mWindowManager.removeView(this.visualFeedbackView);
            this.visualFeedbackView = null;
        }
        // KeepShellPublic.doCmdSync("wm overscan reset");
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int value = (int) (dpValue * scale + 0.5f);
        if (value < 1) {
            return 1;
        }
        return value;
    }

    private View setVisualFeedbackView(final AccessibilityServiceGesture context) {
        final View view = new VisualFeedbackView(context);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;

        params.gravity = Gravity.TOP | Gravity.START;
        // 4G以上(基本上就是6G及更高了)内存开启硬件加速
        if ((!config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) && new Memory().getMemorySizeMB(context) > 4096) {
            params.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_HARDWARE_ACCELERATED; // 开启硬件加速也许能提高性能，但是内存占有将会大幅提高
        } else {
            params.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS; // | LayoutParams.FLAG_HARDWARE_ACCELERATED; // 开启硬件加速也许能提高性能，但是内存占有将会大幅提高
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        // 设为GONE 避免出现 “由于某个应用遮挡了权限请求界面，因此设置应用无法验证您的回应” 提示
        // 在需要显示动画时在切换为可见
        view.setVisibility(View.GONE);

        mWindowManager.addView(view, params);

        return view;
    }

    private View setBottomView(final AccessibilityServiceGesture context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.gesture_fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);
        if (GlobalState.testMode) {
            bar.setBackground(context.getDrawable(R.drawable.bar_background));
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
        }

        bar.setEventHandler(
                ActionModel.getConfig(config, SpfConfig.CONFIG_BOTTOM_EVENT, SpfConfig.CONFIG_BOTTOM_EVENT_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.CONFIG_BOTTOM_EVENT_HOVER, SpfConfig.CONFIG_BOTTOM_EVENT_HOVER_DEFAULT),
                context);

        double widthRatio = config.getInt(SpfConfig.CONFIG_BOTTOM_WIDTH, SpfConfig.CONFIG_BOTTOM_WIDTH_DEFAULT) / 100.0;

        // 横屏缩小宽度，避免游戏误触
        if (islandscape && widthRatio > 0.4) {
            widthRatio = 0.4;
        }

        int barWidth = (int) (getScreenWidth(context) * widthRatio);
        int barHeight;
        if (islandscape) {
            barHeight = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_SIDE_WIDTH, SpfConfig.CONFIG_HOT_SIDE_WIDTH_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        } else {
            barHeight = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT, SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        }

        bar.setBarPosition(TouchBarView.BOTTOM, islandscape, config.getBoolean(SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT), barWidth, barHeight);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mWindowManager.addView(view, params);

        return view;
    }

    private View setThreeSectionView(final AccessibilityServiceGesture context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.gesture_fw_three_section, null);

        ThreeSectionView bar = view.findViewById(R.id.core_area);
        if (GlobalState.testMode) {
            bar.setTestMode(true);
        }

        bar.setEventHandler(
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_LEFT_SLIDE, SpfConfig.THREE_SECTION_LEFT_SLIDE_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_LEFT_HOVER, SpfConfig.THREE_SECTION_LEFT_HOVER_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_CENTER_SLIDE, SpfConfig.THREE_SECTION_CENTER_SLIDE_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_CENTER_HOVER, SpfConfig.THREE_SECTION_CENTER_HOVER_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_RIGHT_SLIDE, SpfConfig.THREE_SECTION_RIGHT_SLIDE_DEFAULT),
                ActionModel.getConfig(config, SpfConfig.THREE_SECTION_RIGHT_HOVER, SpfConfig.THREE_SECTION_RIGHT_HOVER_DEFAULT),
                context);

        double widthRatio = config.getInt(SpfConfig.THREE_SECTION_WIDTH, SpfConfig.THREE_SECTION_WIDTH_DEFAULT) / 100.0;

        // 横屏缩小宽度，避免游戏误触
        if (islandscape && widthRatio > 0.5) {
            widthRatio = 0.5;
        }

        int barWidth = (int) (getScreenWidth(context) * widthRatio);
        int barHeight = dp2px(context, config.getInt(SpfConfig.THREE_SECTION_HEIGHT, SpfConfig.THREE_SECTION_HEIGHT_DEFAULT)); // LayoutParams.WRAP_CONTENT;

        bar.setBarPosition(islandscape, config.getBoolean(SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT), barWidth, barHeight);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mWindowManager.addView(view, params);

        return view;
    }

    private int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        if (islandscape) {
            return h > w ? w : h;
        } else {
            return h > w ? h : w;
        }
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        if (islandscape) {
            return h < w ? w : h;
        } else {
            return h < w ? h : w;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setLeftView(final AccessibilityServiceGesture context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.gesture_fw_vitual_touch_bar, null);
        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);
        if (GlobalState.testMode) {
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
            bar.setBackground(context.getDrawable(R.drawable.bar_background));
        }

        bar.setEventHandler(ActionModel.getConfig(config, SpfConfig.CONFIG_LEFT_EVENT, SpfConfig.CONFIG_LEFT_EVENT_DEFAULT), ActionModel.getConfig(config, SpfConfig.CONFIG_LEFT_EVENT_HOVER, SpfConfig.CONFIG_LEFT_EVENT_HOVER_DEFAULT), context);

        double heightRatio = config.getInt(SpfConfig.CONFIG_LEFT_HEIGHT, SpfConfig.CONFIG_LEFT_HEIGHT_DEFAULT) / 100.0;
        int barHeight = (int) (getScreenHeight(context) * heightRatio); //
        int barWidth = -1;
        if (islandscape) {
            barWidth = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT, SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        } else {
            barWidth = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_SIDE_WIDTH, SpfConfig.CONFIG_HOT_SIDE_WIDTH_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        }

        bar.setBarPosition(TouchBarView.LEFT, islandscape, config.getBoolean(SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT), barWidth, barHeight);

        final LayoutParams params = new LayoutParams();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mWindowManager.addView(view, params);
        // view.setOnTouchListener(getTouchPierceListener(params, view));

        // 无效手势监听
        bar.setAntiTouchModeToggle(new Runnable() {
            @Override
            public void run() {
                final int flags = params.flags;

                params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                mWindowManager.updateViewLayout(view, params);

                Toast.makeText(context, "边缘手势将停用5秒~", Toast.LENGTH_SHORT).show();

                view.setOnTouchListener(new View.OnTouchListener() {
                    private final long startTime = System.currentTimeMillis();
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event != null && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            if (System.currentTimeMillis() - startTime > 5000L) {
                                params.flags = flags; // &= ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                                mWindowManager.updateViewLayout(view, params);
                                view.setOnTouchListener(null);
                                return true;
                            }
                        }
                        return false;
                    }
                });

            }
        });

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setRightView(final AccessibilityServiceGesture context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.gesture_fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);
        if (GlobalState.testMode) {
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
            bar.setBackground(context.getDrawable(R.drawable.bar_background));
        }

        bar.setEventHandler(ActionModel.getConfig(config, SpfConfig.CONFIG_RIGHT_EVENT, SpfConfig.CONFIG_RIGHT_EVENT_DEFAULT), ActionModel.getConfig(config, SpfConfig.CONFIG_RIGHT_EVENT_HOVER, SpfConfig.CONFIG_RIGHT_EVENT_HOVER_DEFAULT), context);

        double heightRatio = config.getInt(SpfConfig.CONFIG_RIGHT_HEIGHT, SpfConfig.CONFIG_RIGHT_HEIGHT_DEFAULT) / 100.0;
        int barHeight = (int) (getScreenHeight(context) * heightRatio); //
        int barWidth = -1;
        if (islandscape) {
            barWidth = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT, SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        } else {
            barWidth = dp2px(context, config.getInt(SpfConfig.CONFIG_HOT_SIDE_WIDTH, SpfConfig.CONFIG_HOT_SIDE_WIDTH_DEFAULT)); // LayoutParams.WRAP_CONTENT;
        }
        bar.setBarPosition(TouchBarView.RIGHT, islandscape, config.getBoolean(SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT), barWidth, barHeight);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mWindowManager.addView(view, params);
        // view.setOnTouchListener(getTouchPierceListener(params, view));

        // 无效手势监听
        bar.setAntiTouchModeToggle(new Runnable() {
            @Override
            public void run() {
                final int flags = params.flags;

                params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                mWindowManager.updateViewLayout(view, params);

                Toast.makeText(context, "边缘手势将停用5秒~", Toast.LENGTH_SHORT).show();

                view.setOnTouchListener(new View.OnTouchListener() {
                    private final long startTime = System.currentTimeMillis();
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event != null && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            if (System.currentTimeMillis() - startTime > 5000L) {
                                params.flags = flags; // &= ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                                mWindowManager.updateViewLayout(view, params);
                                view.setOnTouchListener(null);
                                return true;
                            }
                        }
                        return false;
                    }
                });

            }
        });

        return view;
    }
}