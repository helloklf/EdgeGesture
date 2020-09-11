package com.omarea.gesture.ui;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.omarea.gesture.AccessibilityServiceGesture;
import com.omarea.gesture.ActionModel;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.WhiteBarColor;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.Handlers;
import com.omarea.gesture.util.ReceiverLock;
import com.omarea.gesture.util.ReceiverLockHandler;
import com.omarea.gesture.util.ScreenState;

import java.util.Timer;
import java.util.TimerTask;

public class iOSWhiteBar {
    private AccessibilityServiceGesture accessibilityService;
    private SharedPreferences config;
    private Boolean isLandscapf;
    private Path touchPath = new Path(); // 记录触摸轨迹

    iOSWhiteBar(AccessibilityServiceGesture accessibilityService, Boolean isLandscapf) {
        this.accessibilityService = accessibilityService;
        this.isLandscapf = isLandscapf;
        config = accessibilityService.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
    }

    /**
     * 获取当前屏幕方向下的屏幕宽度
     */
    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        if (isLandscapf) {
            return Math.max(h, w);
        } else {
            return Math.min(h, w);
        }
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int value = (int) (dpValue * scale + 0.5f);
        return Math.max(value, 1);
    }

    // 判断是否没有定义任何动作（装饰模式）
    private boolean decorativeMode() {
        int actionSlideUp = config.getInt(SpfConfig.IOS_BAR_SLIDE_UP, SpfConfig.IOS_BAR_SLIDE_UP_DEFAULT);
        int actionSlideUpHover = config.getInt(SpfConfig.IOS_BAR_SLIDE_UP_HOVER, SpfConfig.IOS_BAR_SLIDE_UP_HOVER_DEFAULT);
        int actionTouch = config.getInt(SpfConfig.IOS_BAR_TOUCH, SpfConfig.IOS_BAR_TOUCH_DEFAULT);
        int actionTouchHover = config.getInt(SpfConfig.IOS_BAR_PRESS, SpfConfig.IOS_BAR_PRESS_DEFAULT);
        int actionSlideLeft = config.getInt(SpfConfig.IOS_BAR_SLIDE_LEFT, SpfConfig.IOS_BAR_SLIDE_LEFT_DEFAULT);
        int actionSlideRight = config.getInt(SpfConfig.IOS_BAR_SLIDE_RIGHT, SpfConfig.IOS_BAR_SLIDE_RIGHT_DEFAULT);

        return actionSlideUp == Handlers.GLOBAL_ACTION_NONE &&
                actionSlideUpHover == Handlers.GLOBAL_ACTION_NONE &&
                actionTouch == Handlers.GLOBAL_ACTION_NONE &&
                actionTouchHover == Handlers.GLOBAL_ACTION_NONE &&
                actionSlideLeft == Handlers.GLOBAL_ACTION_NONE &&
                actionSlideRight == Handlers.GLOBAL_ACTION_NONE;
    }

    @SuppressLint("ClickableViewAccessibility")
    public View getView() {
        final WindowManager mWindowManager = (WindowManager) (accessibilityService.getSystemService(Context.WINDOW_SERVICE));

        final View view = LayoutInflater.from(accessibilityService).inflate(R.layout.gesture_fw_ios_touch_bar, null);

        final iOSTouchBarView bar = view.findViewById(R.id.bottom_touch_bar);
        /*
        if (GlobalState.testMode) {
            bar.setBackground(accessibilityService.getDrawable(R.drawable.bar_background));
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
        }
        */

        float widthRatio;
        if (isLandscapf) {
            widthRatio = config.getInt(SpfConfig.IOS_BAR_WIDTH_LANDSCAPE, SpfConfig.IOS_BAR_WIDTH_DEFAULT_LANDSCAPE) / 100f;
        } else {
            widthRatio = config.getInt(SpfConfig.IOS_BAR_WIDTH_PORTRAIT, SpfConfig.IOS_BAR_WIDTH_DEFAULT_PORTRAIT) / 100f;
        }

        final float fateOutAlpha = (isLandscapf ?
                config.getInt(SpfConfig.IOS_BAR_ALPHA_FADEOUT_LANDSCAPE, SpfConfig.IOS_BAR_ALPHA_FADEOUT_PORTRAIT_DEFAULT) :
                config.getInt(SpfConfig.IOS_BAR_ALPHA_FADEOUT_PORTRAIT, SpfConfig.IOS_BAR_ALPHA_FADEOUT_PORTRAIT_DEFAULT)) / 100f; // 0.2f;
        final int barColor = (isLandscapf ?
                (config.getInt(SpfConfig.IOS_BAR_COLOR_LANDSCAPE, SpfConfig.IOS_BAR_COLOR_LANDSCAPE_DEFAULT)) :
                (config.getInt(SpfConfig.IOS_BAR_COLOR_PORTRAIT, SpfConfig.IOS_BAR_COLOR_PORTRAIT_DEFAULT)));
        final int shadowColor = config.getInt(SpfConfig.IOS_BAR_COLOR_SHADOW, SpfConfig.IOS_BAR_COLOR_SHADOW_DEFAULT); // 阴影颜色
        final int shadowSize = config.getInt(SpfConfig.IOS_BAR_SHADOW_SIZE, SpfConfig.IOS_BAR_SHADOW_SIZE_DEFAULT); // 阴影大小
        final int lineWeight = config.getInt(SpfConfig.IOS_BAR_HEIGHT, SpfConfig.IOS_BAR_HEIGHT_DEFAULT); // 线宽度（百分比）
        final int strokeWidth = config.getInt(SpfConfig.IOS_BAR_STROKE_SIZE, SpfConfig.IOS_BAR_STROKE_SIZE_DEFAULT); // 描边大小
        final int strokeColor = config.getInt(SpfConfig.IOS_BAR_COLOR_STROKE, SpfConfig.IOS_BAR_COLOR_STROKE_DEFAULT); // 描边颜色
        final int marginBottom = (isLandscapf ? config.getInt(SpfConfig.IOS_BAR_MARGIN_BOTTOM_LANDSCAPE, SpfConfig.IOS_BAR_MARGIN_BOTTOM_LANDSCAPE_DEFAULT) : config.getInt(SpfConfig.IOS_BAR_MARGIN_BOTTOM_PORTRAIT, SpfConfig.IOS_BAR_MARGIN_BOTTOM_PORTRAIT_DEFAULT)); // 底部边距
        final int totalHeight = marginBottom + lineWeight + (shadowSize * 2) + (strokeWidth * 2);
        final boolean inputAvoid = config.getBoolean(SpfConfig.INPUT_METHOD_AVOID, SpfConfig.INPUT_METHOD_AVOID_DEFAULT); // 输入法避让

        bar.setStyle(
                ((int) (getScreenWidth(accessibilityService) * widthRatio)),
                dp2px(accessibilityService, totalHeight),
                barColor,
                shadowColor,
                shadowSize,
                lineWeight,
                strokeWidth,
                strokeColor);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        final int originY = 0;
        final int originX = 0;

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        if (decorativeMode()) { // 装饰模式
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            // | WindowManager.LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;

            // 输入法避让
            if (inputAvoid) {
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            } else {
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mWindowManager.addView(view, params);
        final ReTouchHelper reTouchHelper = new ReTouchHelper(accessibilityService, mWindowManager, view);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private float touchStartX = 0F; // 触摸开始位置
            private float touchStartY = 0F; // 触摸开始位置
            private float touchStartRawX = 0F; // 触摸开始位置
            private float touchStartRawY = 0F; // 触摸开始位置
            private boolean isTouchDown = false;
            private boolean isGestureCompleted = false;
            private long gestureStartTime = 0L; // 手势开始时间（是指滑动到一定距离，认定触摸手势生效的时间）
            private boolean isLongTimeGesture = false;
            private float touchCurrentX = 0F; // 当前触摸位置
            private float touchCurrentY = 0F; // 当前触摸位置
            private int FLIP_DISTANCE = dp2px(accessibilityService, 50f); // 触摸灵敏度（滑动多长距离认为是手势）
            private float flingValue = dp2px(accessibilityService, 3f); // 小于此值认为是点击而非滑动
            private int offsetLimitX = dp2px(accessibilityService, 50);
            private int offsetLimitY = dp2px(accessibilityService, 12);
            private int animationScaling = dp2px(accessibilityService, 2); // 手指移动多少像素时动画才移动1像素
            private boolean vibratorRun = false;
            private ValueAnimator fareOutAnimation = null; // 动画程序（淡出）
            private ObjectAnimator objectAnimator = null; // 位置调整动画
            private int slideThresholdY = dp2px(accessibilityService, 5); // 滑动多少像素才认为算是滑动，而非点击
            private int slideThresholdX = dp2px(accessibilityService, 10); // 滑动多少像素才认为算是滑动，而非点击
            private boolean lowPowerMode = config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT);

            private float touchCurrentRawX;
            private float touchCurrentRawY;
            private long lastTouchDown = 0L;

            private void performGlobalAction(final ActionModel event) {
                if (accessibilityService != null) {
                    Handlers.executeVirtualAction(accessibilityService, event, touchStartRawX, touchStartRawY);
                }
            }

            private void setPosition(float x, float y) {
                if (!lowPowerMode) {
                    int limitX = (int) x;
                    if (limitX < -offsetLimitX) {
                        limitX = -offsetLimitX;
                    } else if (limitX > offsetLimitX) {
                        limitX = offsetLimitX;
                    }
                    int limitY = (int) y;
                    if (limitY < -offsetLimitY) {
                        limitY = -offsetLimitY;
                    } else if (limitY > offsetLimitY) {
                        limitY = offsetLimitY;
                    }
                    params.x = limitX;
                    params.y = limitY;
                    mWindowManager.updateViewLayout(view, params);
                }
            }

            private void fadeOut() {
                if (fareOutAnimation != null) {
                    fareOutAnimation.cancel();
                }
                if (lowPowerMode) {
                    bar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isTouchDown) {
                                bar.setAlpha(fateOutAlpha);
                            }
                        }
                    }, 5000);
                } else {
                    fareOutAnimation = ValueAnimator.ofFloat(1f, fateOutAlpha);
                    fareOutAnimation.setDuration(1000);
                    fareOutAnimation.setInterpolator(new LinearInterpolator());
                    fareOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            try {
                                bar.setAlpha((float) animation.getAnimatedValue());
                            } catch (Exception ignored) {
                            }
                        }
                    });
                    fareOutAnimation.setStartDelay(5000);
                    fareOutAnimation.start();
                }
            }

            private void animationTo(int x, int y, int duration, Interpolator interpolator) {
                Path path = new Path();
                path.moveTo(params.x, params.y);
                path.lineTo(x, y);
                if (objectAnimator != null) {
                    objectAnimator.cancel();
                    objectAnimator = null;
                }

                objectAnimator = ObjectAnimator.ofInt(params, "x", "y", path);
                objectAnimator.setStartDelay(200);
                objectAnimator.setInterpolator(interpolator);
                objectAnimator.setAutoCancel(true);
                objectAnimator.setDuration(duration);
                objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animation.getValues();
                        int x = (int) animation.getAnimatedValue("x");
                        int y = (int) animation.getAnimatedValue("y");
                        if (x != params.x || y != params.y) {
                            params.x = x;
                            params.y = y;
                            try {
                                mWindowManager.updateViewLayout(view, params);
                            } catch (Exception ex) {
                                animation.cancel();
                                objectAnimator = null;
                            }
                        }
                    }
                });
                objectAnimator.start();
            }

            /*
            // 只有被输入的引用才能获取输入法状态
            private boolean softKeyboardIsActive () {
                InputMethodManager imm = (InputMethodManager)accessibilityService.getSystemService(Context.INPUT_METHOD_SERVICE);
                return imm.isActive();
            }
            */

            private boolean onTouchDown(final MotionEvent event) {
                isTouchDown = true;
                isGestureCompleted = false;
                touchStartX = event.getX();
                touchStartY = event.getY();
                touchStartRawX = event.getRawX();
                touchStartRawY = event.getRawY();
                touchCurrentRawX = event.getRawX();
                touchCurrentRawY = event.getRawY();
                touchCurrentX = event.getX();
                touchCurrentY = event.getY();
                gestureStartTime = 0;
                isLongTimeGesture = false;
                vibratorRun = true;
                final long downTime = event.getDownTime();
                lastTouchDown = downTime;
                lowPowerMode = config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT);

                if (fareOutAnimation != null) {
                    fareOutAnimation.cancel();
                    fareOutAnimation = null;
                }

                if (objectAnimator != null) {
                    objectAnimator.cancel();
                    objectAnimator = null;
                }

                if (bar.getAlpha() != 1f) {
                    bar.setAlpha(1f);
                    bar.invalidate();
                }

                bar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isTouchDown && !isGestureCompleted && lastTouchDown == downTime) {
                            if (Math.abs(touchStartRawX - touchCurrentRawX) < slideThresholdX && Math.abs(touchStartRawY - touchCurrentRawY) < slideThresholdY) {
                                int pressureAction = config.getInt(SpfConfig.IOS_BAR_PRESS, SpfConfig.IOS_BAR_PRESS_DEFAULT);
                                if (pressureAction != SpfConfig.IOS_BAR_TOUCH_DEFAULT) {
                                    isLongTimeGesture = true;
                                    if (vibratorRun) {
                                        Gesture.vibrate(Gesture.VibrateMode.VIBRATE_PRESS, view);
                                        vibratorRun = false;
                                    }
                                    performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_PRESS, SpfConfig.IOS_BAR_PRESS_DEFAULT));
                                    isGestureCompleted = true;
                                    clearEffect();
                                }
                            }
                        }
                    }
                }, 280);

                return true;
            }

            private boolean onTouchMove(MotionEvent event) {
                if (isGestureCompleted || !isTouchDown) {
                    return true;
                }

                touchCurrentRawX = event.getRawX();
                touchCurrentRawY = event.getRawY();

                touchCurrentX = event.getX();
                touchCurrentY = event.getY();

                if (!isGestureCompleted && ((touchStartY - touchCurrentY > FLIP_DISTANCE) || (GlobalState.consecutive && Math.abs(touchStartRawX - touchCurrentRawX) > slideThresholdX))) {
                    if (gestureStartTime < 1) {
                        final long currentTime = System.currentTimeMillis();
                        gestureStartTime = currentTime;
                        bar.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isTouchDown && !isGestureCompleted && currentTime == gestureStartTime) {
                                    isLongTimeGesture = true;
                                    // 上滑悬停
                                    if (touchStartY - touchCurrentY > FLIP_DISTANCE) {
                                        if (vibratorRun) {
                                            Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE_HOVER, view);
                                            vibratorRun = false;
                                        }
                                        performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_UP_HOVER, SpfConfig.IOS_BAR_SLIDE_UP_HOVER_DEFAULT));
                                        isGestureCompleted = true;
                                        clearEffect();
                                    }
                                    // 右滑悬停
                                    else if (GlobalState.consecutive && Math.abs(touchStartRawX - touchCurrentRawX) > slideThresholdX) {
                                        consecutiveActionStart();
                                    }
                                }
                            }
                        }, config.getInt(SpfConfig.CONFIG_HOVER_TIME, SpfConfig.CONFIG_HOVER_TIME_DEFAULT));
                        Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                    }
                } else {
                    vibratorRun = true;
                    gestureStartTime = 0;
                }

                setPosition(originX + ((touchCurrentX - touchStartX) / animationScaling), originY + ((touchStartY - touchCurrentY) / animationScaling));

                return false;
            }

            private void onTouchUp(MotionEvent event) {
                if (!isTouchDown || isGestureCompleted) {
                    return;
                }

                isTouchDown = false;
                isGestureCompleted = true;
                lastTouchDown = 0L;

                float moveX = event.getX() - touchStartX;
                float moveY = touchStartY - event.getY();
                if (GlobalState.consecutiveAction == null || GlobalState.consecutiveAction.actionCode == Handlers.GLOBAL_ACTION_NONE) {
                    if (Math.abs(moveX) > flingValue || Math.abs(moveY) > flingValue) {
                        if (moveY > FLIP_DISTANCE) {
                            if (isLongTimeGesture) { // 上滑悬停
                                Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE_HOVER, view);
                                performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_UP_HOVER, SpfConfig.IOS_BAR_SLIDE_UP_HOVER_DEFAULT));
                            } else { // 上滑
                                // Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                                performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_UP, SpfConfig.IOS_BAR_SLIDE_UP_DEFAULT));
                            }
                        } else if (moveX < -FLIP_DISTANCE) { // 向左滑动
                            Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                            // Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                            performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_LEFT, SpfConfig.IOS_BAR_SLIDE_LEFT_DEFAULT));
                        } else if (moveX > FLIP_DISTANCE) { // 向右滑动
                            Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                            // Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                            performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_RIGHT, SpfConfig.IOS_BAR_SLIDE_RIGHT_DEFAULT));
                        }
                    } else {
                        // 轻触
                        int action = config.getInt(SpfConfig.IOS_BAR_TOUCH, SpfConfig.IOS_BAR_TOUCH_DEFAULT);
                        if (action != Handlers.GLOBAL_ACTION_NONE) {
                            Gesture.vibrate(Gesture.VibrateMode.VIBRATE_CLICK, view);
                            performGlobalAction(ActionModel.getConfig(config, SpfConfig.IOS_BAR_TOUCH, SpfConfig.IOS_BAR_TOUCH_DEFAULT));
                        } else {
                            buildGesture();
                        }
                    }
                } else if (!(Math.abs(moveX) > flingValue || Math.abs(moveY) > flingValue)) {
                    buildGesture();
                }

                clearEffect();
            }

            private void buildGesture() {
                reTouchHelper.dispatchGesture(touchPath);
            }

            void clearEffect() {
                if (!lowPowerMode) {
                    animationTo(originX, originY, 800, new OvershootInterpolator());
                }
                // if (isLandscapf) {
                fadeOut();
                // }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            touchPath.reset();
                            touchPath.moveTo(event.getRawX(), event.getRawY());
                            return onTouchDown(event);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            touchPath.rLineTo(event.getRawX(), event.getRawY());
                            return onTouchMove(event);
                        }
                        case MotionEvent.ACTION_UP: {
                            onTouchUp(event);
                            consecutiveActionStop();
                            return true;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            consecutiveActionStop();
                            clearEffect();
                            return true;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            consecutiveActionStop();
                            clearEffect();
                            return false;
                        }
                        default: {
                            consecutiveActionStop();
                        }
                    }
                } else {
                    clearEffect();
                }
                return true;
            }

            private Timer consecutiveActionTimer;

            private void consecutiveActionStart() {
                consecutiveActionStop();
                consecutiveActionTimer = new Timer();
                consecutiveActionTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ActionModel actionModel = null;
                        if (touchStartRawX - touchCurrentRawX > slideThresholdX) {
                            actionModel = ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_LEFT, SpfConfig.IOS_BAR_SLIDE_LEFT_DEFAULT);
                        } else if (touchStartRawX - touchCurrentRawX < -slideThresholdX) {
                            actionModel = ActionModel.getConfig(config, SpfConfig.IOS_BAR_SLIDE_RIGHT, SpfConfig.IOS_BAR_SLIDE_RIGHT_DEFAULT);
                        }
                        if (actionModel != null && (actionModel.actionCode == Handlers.VITUAL_ACTION_NEXT_APP || actionModel.actionCode == Handlers.VITUAL_ACTION_PREV_APP)) {
                            GlobalState.consecutiveAction = actionModel;
                            if (vibratorRun) {
                                Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, view);
                                vibratorRun = false;
                            }
                            if (accessibilityService.recents != null && accessibilityService.recents.notEmpty()) {
                                performGlobalAction(actionModel);
                            }
                        }
                    }
                }, 0, 500);
            }

            private void consecutiveActionStop() {
                if (consecutiveActionTimer != null) {
                    consecutiveActionTimer.cancel();
                    consecutiveActionTimer = null;
                }
                GlobalState.consecutiveAction = null;
            }
        };

        bar.setOnTouchListener(onTouchListener);
        if (!GlobalState.testMode) {
            bar.setAlpha(fateOutAlpha);
        }

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                ReceiverLock.unRegister(accessibilityService);
                if (config.getBoolean(SpfConfig.IOS_BAR_LOCK_HIDE, SpfConfig.IOS_BAR_LOCK_HIDE_DEFAULT)) {
                    ReceiverLock.autoRegister(accessibilityService, new ReceiverLockHandler(bar, accessibilityService));
                }
                if (config.getBoolean(SpfConfig.IOS_BAR_AUTO_COLOR, SpfConfig.IOS_BAR_AUTO_COLOR_DEFAULT) || config.getBoolean(SpfConfig.IOS_BAR_POP_BATTERY, SpfConfig.IOS_BAR_POP_BATTERY_DEFAULT)) {
                    GlobalState.updateBar = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        bar.invalidate();
                                    }
                                });
                            } catch (Exception ignored) {
                            }
                        }
                    };
                    WhiteBarColor.updateBarColorSingle();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
        if (config.getBoolean(SpfConfig.IOS_BAR_LOCK_HIDE, SpfConfig.IOS_BAR_LOCK_HIDE_DEFAULT)) {
            if (new ScreenState(accessibilityService).isScreenLocked()) {
                bar.setVisibility(View.GONE);
            }
        }

        return view;
    }
}
