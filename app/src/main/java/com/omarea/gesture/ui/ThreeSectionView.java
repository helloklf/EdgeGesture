package com.omarea.gesture.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.omarea.gesture.AccessibilityServiceGesture;
import com.omarea.gesture.ActionModel;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.Handlers;

public class ThreeSectionView extends View {
    private SharedPreferences config;
    private int bakWidth = 0;

    // 处于操作提醒状态
    private boolean remindState = false;

    private float touchStartX = 0F; // 触摸开始位置
    private float touchStartRawY = 0F; // 触摸开始位置
    private long gestureStartTime = 0L; // 手势开始时间（是指滑动到一定距离，认定触摸手势生效的时间）
    private boolean isLongTimeGesture = false;
    private Context context = getContext();
    private int FLIP_DISTANCE = dp2px(context, 50f); // 触摸灵敏度（滑动多长距离认为是手势）
    private boolean isTouchDown = false;
    private boolean isGestureCompleted = false;
    private boolean vibratorRun = false;
    private float flingValue = dp2px(context, 3f); // 小于此值认为是点击而非滑动

    private ActionModel eventLeftSlide;
    private ActionModel eventLeftHover;
    private ActionModel eventCenterSlide;
    private ActionModel eventCenterHover;
    private ActionModel eventRightSlide;
    private ActionModel eventRightHover;
    private AccessibilityServiceGesture accessibilityService;
    private boolean isLandscapf = false;
    private boolean gameOptimization = false;

    private Paint p = new Paint();
    private long lastEventTime = 0L;
    private int lastEvent = -1;

    private float touchRawX;
    private float touchRawY;

    public ThreeSectionView(Context context) {
        super(context);
        init();
    }

    public ThreeSectionView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public ThreeSectionView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    private void init() {
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setStrokeCap(Paint.Cap.ROUND);
        // p.setShadowLayer(dp2px(context, 1), 0, 0, 0x99000000);
        p.setStrokeWidth(dp2px(context, 3));

        config = context.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
    }

    private void performGlobalAction(ActionModel event) {
        if (accessibilityService != null) {
            if (gameOptimization && isLandscapf && ((System.currentTimeMillis() - lastEventTime) > 3000 || lastEvent != event.actionCode)) {
                lastEvent = event.actionCode;
                lastEventTime = System.currentTimeMillis();
                Toast.makeText(context, this.getContext().getString(R.string.please_repeat), Toast.LENGTH_SHORT).show();
                remindState = true;
                invalidate();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (remindState && (System.currentTimeMillis() - lastEventTime) >= 3000) {
                            remindState = false;
                            invalidate();
                        }
                    }
                }, 3000);
            } else {
                if (remindState) {
                    remindState = false;
                    invalidate();
                }
                Handlers.executeVirtualAction(accessibilityService, event, touchRawX, touchRawY);
            }
        }
    }

    private void onShortTouch() {
        if (accessibilityService != null) {
            Log.d(">>>>", "" +  touchStartX +"," + getWidth());
            float p = touchStartX / getWidth();
            if (p > 0.6f) {
                performGlobalAction(eventRightSlide);
            } else if (p > 0.4f) {
                performGlobalAction(eventCenterSlide);
            } else {
                performGlobalAction(eventLeftSlide);
            }
        }
    }

    private void onTouchHover() {
        if (accessibilityService != null) {
            float p = touchStartX / getWidth();
            if (p > 0.6f) {
                GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventRightHover.actionCode), false);
                performGlobalAction(eventRightHover);
            } else if (p > 0.4f) {
                GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventCenterHover.actionCode), false);
                performGlobalAction(eventCenterHover);
            } else {
                GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventLeftHover.actionCode), false);
                performGlobalAction(eventLeftHover);
            }
        }
    }

    void setBarPosition(boolean isLandscapf, boolean gameOptimization, int width, int height) {
        this.isLandscapf = isLandscapf;
        this.gameOptimization = gameOptimization;
        p.setColor(config.getInt(SpfConfig.THREE_SECTION_COLOR, SpfConfig.THREE_SECTION_COLOR_DEFAULT));

        setSize(width, height);
    }

    private void setSize(int width, int height) {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        int h = height;
        int w = width;
        if (h < 1) {
            h = 1;
        }
        if (w < 1) {
            w = 1;
        }
        lp.width = w;
        lp.height = h;
        this.bakWidth = width;
        this.setLayoutParams(lp);
    }

    void setEventHandler(
            ActionModel leftSlide,
            ActionModel leftHover,
            ActionModel centerSlide,
            ActionModel centerHover,
            ActionModel rightSlide,
            ActionModel rightHover,
            final AccessibilityServiceGesture context) {
        this.eventLeftSlide = leftSlide;
        this.eventLeftHover = leftHover;
        this.eventCenterSlide = centerSlide;
        this.eventCenterHover = centerHover;
        this.eventRightSlide = rightSlide;
        this.eventRightHover = rightHover;

        this.accessibilityService = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                }
            }
        } else {
            clearEffect();
        }
        return true;
    }

    private boolean onTouchDown(MotionEvent event) {
        isTouchDown = true;
        isGestureCompleted = false;
        touchStartX = event.getX();
        touchStartRawY = event.getRawY();
        gestureStartTime = 0;
        isLongTimeGesture = false;
        vibratorRun = true;

        GlobalState.startThreeSectionFeedback(event.getRawX(), event.getRawY());

        return true;
    }

    private boolean onTouchMove(MotionEvent event) {
        if (isGestureCompleted || !isTouchDown) {
            return true;
        }

        touchRawX = event.getRawX();
        touchRawY = event.getRawY();

        if (touchStartRawY - event.getRawY() > FLIP_DISTANCE) {
            if (gestureStartTime < 1) {
                float p = touchStartX / getWidth();
                if (p > 0.6f) {
                    GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventRightSlide.actionCode), false);
                } else if (p > 0.4f) {
                    GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventCenterSlide.actionCode), false);
                } else {
                    GlobalState.updateThreeSectionFeedbackIcon(TouchIconCache.getIcon(eventLeftSlide.actionCode), false);
                }

                final long currentTime = System.currentTimeMillis();
                gestureStartTime = currentTime;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isTouchDown && !isGestureCompleted && currentTime == gestureStartTime) {
                            isLongTimeGesture = true;

                            if (vibratorRun) {
                                Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE_HOVER, getRootView());
                                vibratorRun = false;
                            }
                            onTouchHover();
                            isGestureCompleted = true;
                            clearEffect();
                        }
                    }
                }, config.getInt(SpfConfig.CONFIG_HOVER_TIME, SpfConfig.CONFIG_HOVER_TIME_DEFAULT));
            }
        } else {
            GlobalState.updateThreeSectionFeedbackIcon(null, false);
            vibratorRun = true;
            gestureStartTime = 0;
        }
        GlobalState.updateThreeSectionFeedback(touchRawX, touchRawY);

        return true;
    }

    private boolean onTouchUp(MotionEvent event) {
        if (!isTouchDown || isGestureCompleted) {
            return true;
        }

        isTouchDown = false;
        isGestureCompleted = true;

        float moveY = touchStartRawY - event.getRawY();

        if (Math.abs(moveY) > flingValue) {
            if (moveY > FLIP_DISTANCE) { // 纵向滑动
                if (isLongTimeGesture) {
                    Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE_HOVER, getRootView());
                    onTouchHover();
                } else {
                    Gesture.vibrate(Gesture.VibrateMode.VIBRATE_SLIDE, getRootView());
                    onShortTouch();
                }
            }
        }
        clearEffect();
        return true;
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 清除手势效果
     */
    private void clearEffect() {
        gestureStartTime = 0;
        isTouchDown = false;

        // TODO:
        GlobalState.clearThreeSectionFeedback();
    }


    private boolean testMode = false;
    public void setTestMode(boolean b) {
        this.testMode = b;
    }

    @Override
    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (testMode || remindState) {
            p.setColor(Color.argb(128,225,203,255));
            canvas.drawRoundRect(bakWidth * 0.66f, 0, bakWidth * 0.95f, getHeight(), 10f, 10f, p);
            canvas.drawRoundRect(bakWidth * 0.36f, 0, bakWidth * 0.64f, getHeight(), 10f, 10f, p);
            canvas.drawRoundRect(bakWidth * 0.06f, 0, bakWidth * 0.34f, getHeight(), 10f, 10f, p);
        }
    }
}
