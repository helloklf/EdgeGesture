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
    private AccessibilityService accessibilityService;
    private SharedPreferences config;
    private Boolean isLandscapf;
    private Vibrator vibrator;

    public iOSWhiteBar(AccessibilityService accessibilityService, Boolean isLandscapf) {
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
        if (GlobalState.testMode) {
            bar.setBackground(accessibilityService.getDrawable(R.drawable.bar_background));
            // bar.setBackgroundColor(Color.argb(128, 0, 0, 0));
        }

        int barWidth = (int) (getScreenWidth(accessibilityService) * 0.3);
        int barHeight = dp2px(accessibilityService, 20);

        bar.setBarPosition(barWidth, barHeight);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // params.y = -dp2px(context, 20); // 20;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;

        mWindowManager.addView(view, params);

        bar.setOnTouchListener(new View.OnTouchListener() {
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
            private ValueAnimator va = null; // 动画程序（x轴定位）
            private ValueAnimator va2 = null; // 动画程序（y轴定位）
            private ValueAnimator va3 = null; // 动画程序（淡出）

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
                // animationTo(limitX, limitY, 10, new LinearInterpolator());
                params.x = limitX;
                params.y = limitY;
                mWindowManager.updateViewLayout(view, params);
            }
            private void fadeOut() {
                if (va3 != null && va3.isRunning()) {
                    va3.cancel();
                }
                va3 = ValueAnimator.ofFloat(1f, 0.3f);
                va3.setDuration(1000);
                va3.setInterpolator(new LinearInterpolator());
                va3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        try {
                            bar.setAlpha((float)animation.getAnimatedValue());
                        } catch (Exception ignored) {}
                    }
                });
                va3.setStartDelay(3000);
                va3.start();
            }

            private void animationTo(int x, int y, int duration, Interpolator interpolator) {
                if (va != null && va.isRunning()) {
                    va.cancel();
                }
                va = ValueAnimator.ofInt(params.x, x);

                va.setDuration(duration);
                va.setInterpolator(interpolator);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        params.x = (int) animation.getAnimatedValue();
                        try {
                            mWindowManager.updateViewLayout(view, params);
                        } catch (Exception ignored) {}
                    }
                });
                va.start();


                if (va2 != null && va2.isRunning()) {
                    va2.cancel();
                }
                va2 = ValueAnimator.ofInt(params.y, y);

                va2.setDuration(duration);
                va2.setInterpolator(interpolator);
                va2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        params.y = (int) animation.getAnimatedValue();
                        try {
                            mWindowManager.updateViewLayout(view, params);
                        } catch (Exception ignored) {}
                    }
                });
                va2.start();
            }

            private boolean onTouchDown(MotionEvent event) {
                isTouchDown = true;
                isGestureCompleted = false;
                touchStartX = event.getX();
                touchStartY = event.getY();
                gestureStartTime = 0;
                isLongTimeGesture = false;
                vibratorRun = true;
                if (va3 != null && va3.isRunning()) {
                    va3.cancel();
                }
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
                                if (isTouchDown && !isGestureCompleted && currentTime == gestureStartTime) {
                                    isLongTimeGesture = true;
                                    if (vibratorRun) {
                                        touchVibrator();
                                        vibratorRun = false;
                                    }
                                    performGlobalAction(Handlers.GLOBAL_ACTION_RECENTS);
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

                setPosition((touchCurrentX - touchStartX) / animationScaling, (touchStartY - touchCurrentY) / animationScaling);
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
                        if (isLongTimeGesture)
                            performGlobalAction(Handlers.GLOBAL_ACTION_RECENTS);
                        else
                            performGlobalAction(Handlers.GLOBAL_ACTION_HOME);
                    } else if (moveX < -FLIP_DISTANCE) {
                        // 返回
                        performGlobalAction(Handlers.GLOBAL_ACTION_BACK);
                    } else if (moveX > FLIP_DISTANCE) {
                        // 任务
                        performGlobalAction(Handlers.GLOBAL_ACTION_RECENTS);
                    }
                }

                clearEffect();

                return true;
            }

            private void clearEffect() {
                animationTo(0, 0, 300, new OvershootInterpolator());
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
        });
        bar.setAlpha(0.3f);

        return view;
    }

    void touchVibrator() {
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
            Handlers.executeVitualAction(accessibilityService, event);
        }
    }
}
