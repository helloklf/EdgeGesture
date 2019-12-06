package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

public class iOSWhiteBar {
    private AccessibilityServiceKeyEvent accessibilityService;
    private SharedPreferences config;
    private Boolean isLandscapf;
    private Vibrator vibrator;

    public iOSWhiteBar(AccessibilityServiceKeyEvent accessibilityService, Boolean isLandscapf) {
        this.accessibilityService = accessibilityService;
        this.isLandscapf = isLandscapf;
        config = accessibilityService.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
    }

    /**
     * 获取当前屏幕方向下的屏幕宽度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        if (isLandscapf) {
            return h < w ? w : h;
        } else {
            return h < w ? h : w;
        }
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

    @SuppressLint("ClickableViewAccessibility")
    public View getView() {
        final WindowManager mWindowManager = (WindowManager) (accessibilityService.getSystemService(Context.WINDOW_SERVICE));

        final View view = LayoutInflater.from(accessibilityService).inflate(R.layout.fw_ios_touch_bar, null);

        final iOSTouchBarView bar = view.findViewById(R.id.bottom_touch_bar);
        /*
        if (GlobalState.testMode) {
            bar.setBackground(accessibilityService.getDrawable(R.drawable.bar_background));
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
        }
        */

        float widthRatio = 0.3f;
        if (isLandscapf) {
            widthRatio = config.getInt(SpfConfig.IOS_BAR_WIDTH_LANDSCAPE, SpfConfig.IOS_BAR_WIDTH_DEFAULT_LANDSCAPE) / 100f;
        } else {
            widthRatio = config.getInt(SpfConfig.IOS_BAR_WIDTH_PORTRAIT, SpfConfig.IOS_BAR_WIDTH_DEFAULT_PORTRAIT) / 100f;
        }

        final boolean gameOptimization = config.getBoolean(SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT);
        final float fateOutAlpha = config.getInt(SpfConfig.IOS_BAR_ALPHA_FADEOUT, SpfConfig.IOS_BAR_ALPHA_FADEOUT_DEFAULT) / 100f; // 0.2f;
        final int barColor = isLandscapf ? (config.getInt(SpfConfig.IOS_BAR_COLOR_LANDSCAPE, SpfConfig.IOS_BAR_COLOR_LANDSCAPE_DEFAULT)) : (config.getInt(SpfConfig.IOS_BAR_COLOR_PORTRAIT, SpfConfig.IOS_BAR_COLOR_PORTRAIT_DEFAULT));
        final int shadowColor = config.getInt(SpfConfig.IOS_BAR_COLOR_SHADOW, SpfConfig.IOS_BAR_COLOR_SHADOW_DEFAULT);
        final int shadowSize = config.getInt(SpfConfig.IOS_BAR_SHADOW_SIZE, SpfConfig.IOS_BAR_SHADOW_SIZE_DEFAULT);
        final int lineWeight = config.getInt(SpfConfig.IOS_BAR_HEIGHT, SpfConfig.IOS_BAR_HEIGHT_DEFAULT);
        final int marginBottom =  config.getInt(SpfConfig.IOS_BAR_MARGIN_BOTTOM, SpfConfig.IOS_BAR_MARGIN_BOTTOM_DEFAULT);
        final int totalHeight = marginBottom + lineWeight + (shadowSize * 2);

        bar.setStyle(
                ((int) (getScreenWidth(accessibilityService) * widthRatio)),
                dp2px(accessibilityService, totalHeight),
                barColor,
                shadowColor,
                shadowSize,
                lineWeight);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        final int originY =  - dp2px(accessibilityService, ((isLandscapf && gameOptimization) ? marginBottom : 0));
        final int originX = 0;

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = originY;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;

        mWindowManager.addView(view, params);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private float touchStartX = 0F; // 触摸开始位置
            private float touchStartY = 0F; // 触摸开始位置
            private boolean isTouchDown = false;
            private boolean isGestureCompleted = false;
            private long gestureStartTime = 0L; // 手势开始时间（是指滑动到一定距离，认定触摸手势生效的时间）
            private boolean isLongTimeGesture = false;
            private float touchCurrentX = 0F; // 当前触摸位置
            private float touchCurrentY = 0F; // 当前触摸位置
            private int FLIP_DISTANCE = dp2px(accessibilityService, 50f); // 触摸灵敏度（滑动多长距离认为是手势）
            private float flingValue = dp2px(accessibilityService, 3f); // 小于此值认为是点击而非滑动
            private int offsetLimitX = dp2px(accessibilityService, 50);
            private int offsetLimitY = dp2px(accessibilityService, 20);
            private int animationScaling = dp2px(accessibilityService, 2); // 手指移动多少像素时动画才移动1像素
            private boolean vibratorRun = false;
            private ValueAnimator moveXAnimation = null; // 动画程序（x轴定位）
            private ValueAnimator moveYAnimation = null; // 动画程序（y轴定位）
            private ValueAnimator fareOutAnimation = null; // 动画程序（淡出）

            private void setPosition(float x, float y) {
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
                // animationTo(limitX, limitY, 5, new LinearInterpolator());
                params.x = limitX;
                params.y = limitY;
                mWindowManager.updateViewLayout(view, params);
            }

            private void fadeOut() {
                if (fareOutAnimation != null) {
                    fareOutAnimation.cancel();
                }
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

            private void animationTo(int x, int y, int duration, Interpolator interpolator) {
                if (moveXAnimation != null && moveXAnimation.isRunning()) {
                    moveXAnimation.cancel();
                }
                moveXAnimation = ValueAnimator.ofInt(params.x, x);

                moveXAnimation.setDuration(duration);
                moveXAnimation.setInterpolator(interpolator);
                moveXAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        params.x = (int) animation.getAnimatedValue();
                        try {
                            mWindowManager.updateViewLayout(view, params);
                        } catch (Exception ignored) {
                        }
                    }
                });
                moveXAnimation.start();

                if (moveYAnimation != null && moveYAnimation.isRunning()) {
                    moveYAnimation.cancel();
                }
                moveYAnimation = ValueAnimator.ofInt(params.y, y);

                moveYAnimation.setDuration(duration);
                moveYAnimation.setInterpolator(interpolator);
                moveYAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        params.y = (int) animation.getAnimatedValue();
                        try {
                            mWindowManager.updateViewLayout(view, params);
                        } catch (Exception ignored) {
                        }
                    }
                });
                moveYAnimation.start();
            }

            private boolean onTouchDown(MotionEvent event) {
                isTouchDown = true;
                isGestureCompleted = false;
                touchStartX = event.getX();
                touchStartY = event.getY();
                gestureStartTime = 0;
                isLongTimeGesture = false;
                vibratorRun = true;
                if (fareOutAnimation != null) {
                    fareOutAnimation.cancel();
                }
                bar.setAlpha(1f);
                return true;
            }

            private boolean onTouchMove(MotionEvent event) {
                if (isGestureCompleted || !isTouchDown) {
                    return true;
                }

                touchCurrentX = event.getX();
                touchCurrentY = event.getY();

                if (touchStartY - touchCurrentY > FLIP_DISTANCE) {
                    if (gestureStartTime < 1) {
                        final long currentTime = System.currentTimeMillis();
                        gestureStartTime = currentTime;
                        bar.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 上滑悬停
                                if (isTouchDown && !isGestureCompleted && currentTime == gestureStartTime) {
                                    isLongTimeGesture = true;
                                    if (vibratorRun) {
                                        touchVibrator();
                                        vibratorRun = false;
                                    }
                                    performGlobalAction(config.getInt(SpfConfig.IOS_BAR_SLIDE_UP_HOVER, SpfConfig.IOS_BAR_SLIDE_UP_HOVER_DEFAULT));
                                    isGestureCompleted = true;
                                    clearEffect();
                                }
                            }
                        }, config.getInt(SpfConfig.CONFIG_HOVER_TIME, SpfConfig.CONFIG_HOVER_TIME_DEFAULT));
                    }
                } else {
                    vibratorRun = true;
                    gestureStartTime = 0;
                }

                setPosition(originX + ((touchCurrentX - touchStartX) / animationScaling), originY + ((touchStartY - touchCurrentY) / animationScaling));
                return false;
            }

            private boolean onTouchUp(MotionEvent event) {
                if (!isTouchDown || isGestureCompleted) {
                    return true;
                }

                isTouchDown = false;
                isGestureCompleted = true;

                float moveX = event.getX() - touchStartX;
                float moveY = touchStartY - event.getY();

                if (Math.abs(moveX) > flingValue || Math.abs(moveY) > flingValue) {
                    if (moveY > FLIP_DISTANCE) {
                        if (isLongTimeGesture) // 上滑悬停
                            performGlobalAction(config.getInt(SpfConfig.IOS_BAR_SLIDE_UP_HOVER, SpfConfig.IOS_BAR_SLIDE_UP_HOVER_DEFAULT));
                        else // 上滑
                            performGlobalAction(config.getInt(SpfConfig.IOS_BAR_SLIDE_UP, SpfConfig.IOS_BAR_SLIDE_UP_DEFAULT));
                    } else if (moveX < -FLIP_DISTANCE) { // 向左滑动
                        performGlobalAction(config.getInt(SpfConfig.IOS_BAR_SLIDE_LEFT, SpfConfig.IOS_BAR_SLIDE_LEFT_DEFAULT));
                    } else if (moveX > FLIP_DISTANCE) { // 向右滑动
                        performGlobalAction(config.getInt(SpfConfig.IOS_BAR_SLIDE_RIGHT, SpfConfig.IOS_BAR_SLIDE_RIGHT_DEFAULT));
                    }
                }

                clearEffect();

                return true;
            }

            void clearEffect() {
                animationTo(originX, originY, 300, new OvershootInterpolator());
                // if (isLandscapf) {
                fadeOut();
                // }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            return onTouchDown(event);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            return onTouchMove(event);
                        }
                        case MotionEvent.ACTION_UP: {
                            return onTouchUp(event);
                        }
                        case MotionEvent.ACTION_CANCEL:
                            clearEffect();
                            return true;
                        case MotionEvent.ACTION_OUTSIDE: {
                            clearEffect();
                            return false;
                        }
                        default: {
                            Log.d("MotionEvent", "com.omarea.gesture OTHER" + event.getAction());
                        }
                    }
                } else {
                    clearEffect();
                }
                return true;
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

    private void touchVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) (accessibilityService.getSystemService(Context.VIBRATOR_SERVICE));
        }
        if (vibrator.hasVibrator()) {
            vibrator.cancel();
            int time = config.getInt(SpfConfig.VIBRATOR_TIME, SpfConfig.VIBRATOR_TIME_DEFAULT);
            int amplitude = config.getInt(SpfConfig.VIBRATOR_AMPLITUDE, SpfConfig.VIBRATOR_AMPLITUDE_DEFAULT);
            if (time > 0 && amplitude > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(time, amplitude));
                } else {
                    vibrator.vibrate(new long[]{0, time, amplitude}, -1);
                }
            }
        }
    }

    private void performGlobalAction(int event) {
        if (accessibilityService != null) {
            Handlers.executeVirtualAction(accessibilityService, event);
        }
    }
}
