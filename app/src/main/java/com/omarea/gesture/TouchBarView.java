package com.omarea.gesture;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class TouchBarView extends View {
    static int RIGHT = 2;
    static int BOTTOM = 0;
    static int LEFT = 1;
    ValueAnimator va = null;
    private int orientation = 0;
    private int bakWidth = 0;
    private int bakHeight = 0;
    private float touchStartX = 0F;
    private float touchStartY = 0F;
    private float touchCurrentX = 0F;
    private float touchCurrentY = 0F;
    private long gestureStartTime = 0L; // 手势开始时间（是指滑动到一定距离，认定触摸手势生效的时间）
    private boolean isLongTimeGesture = false;
    private Context context = getContext();
    private int FLIP_DISTANCE = dp2px(context, 50f); // 触摸灵敏度（滑动多长距离认为是手势）
    private float effectSize = (float) (dp2px(context, 15f)); // 特效大小
    private float effectWidth = effectSize * 6; // 特效大小
    private float cushion = effectWidth + effectWidth * 1.4f; // 左右两端的缓冲宽度（数值越大则约缓和）
    private long longTouchTime = 250L;
    private boolean isTouchDown = false;
    private boolean isGestureCompleted = false;
    private float iconRadius = dp2px(context, 8f);
    private float currentGraphSize = 0f;
    private Vibrator vibrator = (Vibrator) (context.getSystemService(Context.VIBRATOR_SERVICE));
    private boolean vibratorRun = false;
    private boolean drawIcon = true;
    private Bitmap touch_arrow_left,
            touch_arrow_right,
            touch_tasks,
            touch_home;
    private float flingValue = dp2px(context, 3f); // 小于此值认为是点击而非滑动
    private Runnable shortTouch, longTouch;

    private Paint p = new Paint();

    private void init() {
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x101010);
        p.setAlpha(240);
        touch_arrow_left = BitmapFactory.decodeResource(context.getResources(), R.drawable.touch_arrow_left);
        touch_arrow_right = BitmapFactory.decodeResource(context.getResources(), R.drawable.touch_arrow_right);
        touch_tasks = BitmapFactory.decodeResource(context.getResources(), R.drawable.touch_tasks);
        touch_home = BitmapFactory.decodeResource(context.getResources(), R.drawable.touch_home);
    }

    public TouchBarView(Context context) {
        super(context);
        init();
    }

    public TouchBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public TouchBarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    void touchVibrator() {
        vibrator.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1, 1));
        } else {
            vibrator.vibrate(new long[]{20, 10}, -1);
        }
    }

    void setSize(int width, int height, int orientation) {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width = width;
        lp.height = height;
        this.bakWidth = width;
        this.bakHeight = height;
        this.setLayoutParams(lp);
        this.orientation = orientation;
    }

    void setEventHandler(Runnable shortTouch, Runnable longTouch) {
        this.shortTouch = shortTouch;
        this.longTouch = longTouch;
    }

    // 动画（触摸效果）显示期间，将悬浮窗显示调大，以便显示完整的动效
    private void setSizeOnTouch() {
        if (bakHeight > 0 || bakWidth > 0) {
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            if (orientation == BOTTOM) {
                lp.height = FLIP_DISTANCE;
            } else if (orientation == LEFT || orientation == RIGHT) {
                lp.width = FLIP_DISTANCE;
                if (touchStartY < effectWidth) {
                    int toHeight = (int) (this.bakHeight + effectWidth);
                    if (toHeight != lp.height) {
                        lp.height = toHeight;
                        touchStartY += effectWidth;
                    }
                }
            }
            this.setLayoutParams(lp);
        }
    }

    // 动画结束后缩小悬浮窗，以免影响正常操作
    private void resumeBackupSize() {
        touchStartX = 0f;
        touchStartY = 0f;

        ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width = bakWidth;
        lp.height = bakHeight;
        this.setLayoutParams(lp);
        invalidate();
    }

    void cgangePer(float per) {
        if (va != null && va.isRunning()) {
            va.cancel();
        }
        va = ValueAnimator.ofFloat(this.currentGraphSize, per);
        va.setDuration(200);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentGraphSize = (float) (animation.getAnimatedValue());
                invalidate();
            }
        });
        va.start();
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
                    // Log.d("MotionEvent", "com.omarea.gesture ACTION_CANCEL");
                    cleartEffect();
                    return true;
                case MotionEvent.ACTION_OUTSIDE: {
                    // Log.d("MotionEvent", "com.omarea.gesture ACTION_OUTSIDE");
                    cleartEffect();
                    return false;
                }
                default: {
                    Log.d("MotionEvent", "com.omarea.gesture OTHER" + event.getAction());
                }
            }
        } else {
            // Log.d("MotionEvent", "com.omarea.gesture Null");
            cleartEffect();
        }
        return true;
    }

    private boolean onTouchDown (MotionEvent event) {
        // Log.d("MotionEvent", "com.omarea.gesture ACTION_DOWN");

        isTouchDown = true;
        isGestureCompleted = false;
        touchStartX = event.getX();
        touchStartY = event.getY();
        gestureStartTime = 0;
        isLongTimeGesture = false;
        currentGraphSize = (float) (dp2px(context, 5f));
        setSizeOnTouch();
        currentGraphSize = 0f;
        // invalidate();
        // cgangePer(effectSize);
        vibratorRun = true;

        return true;
    }

    private boolean onTouchMove(MotionEvent event) {
        // Log.d("MotionEvent", "com.omarea.gesture ACTION_MOVE");

        if (isGestureCompleted || !isTouchDown) {
            return true;
        }

        touchCurrentX = event.getX();
        touchCurrentY = event.getY();
        float a = -1f;
        float b = -1f;
        if (orientation == LEFT) {
            a = touchCurrentX;
            b = touchStartX;
        } else if (orientation == RIGHT) {
            a = touchStartX;
            b = touchCurrentX;
        } else if (orientation == BOTTOM) {
            a = touchStartY;
            b = touchCurrentY;
        }

        if (a - b > FLIP_DISTANCE) {
            currentGraphSize = effectSize;
            if (gestureStartTime < 1) {
                final long currentTime = System.currentTimeMillis();
                gestureStartTime = currentTime;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isTouchDown && !isGestureCompleted && currentTime == gestureStartTime) {
                            isLongTimeGesture = true;
                            if (vibratorRun) {
                                touchVibrator();
                                vibratorRun = false;
                            }
                            if (orientation == BOTTOM) {
                                longTouch.run();
                                isGestureCompleted = true;
                                cleartEffect();
                            } else {
                                invalidate();
                            }
                        }
                    }
                }, longTouchTime);
            }
        } else {
            vibratorRun = true;
            gestureStartTime = 0;
        }
        float size = (a - b) / FLIP_DISTANCE * effectSize;
        if (size > effectSize) {
            size = effectSize;
        }
        currentGraphSize = size;
        invalidate();
        return true;
    }

    private boolean onTouchUp(MotionEvent event) {
        // Log.d("MotionEvent", "com.omarea.gesture ACTION_UP");

        if (!isTouchDown || isGestureCompleted) {
            return true;
        }

        isTouchDown = false;
        isGestureCompleted = true;

        float moveX = event.getX() - touchStartX;
        float moveY = touchStartY - event.getY();

        if (Math.abs(moveX) > flingValue || Math.abs(moveY) > flingValue) {
            if (orientation == LEFT) {
                if (moveX > FLIP_DISTANCE) {
                    // 向屏幕内侧滑动 - 停顿250ms 打开最近任务，不停顿则“返回”
                    if (isLongTimeGesture)
                        longTouch.run();
                    else
                        shortTouch.run();
                }
            } else if (orientation == RIGHT) {
                if (-moveX > FLIP_DISTANCE) {
                    // 向屏幕内侧滑动 - 停顿250ms 打开最近任务，不停顿则“返回”
                    if (isLongTimeGesture)
                        longTouch.run();
                    else
                        shortTouch.run();
                }
            } else if (orientation == BOTTOM) {
                if (moveY > FLIP_DISTANCE) {
                    if (isLongTimeGesture)
                        longTouch.run();
                    else
                        shortTouch.run();
                }
            }
        }
        cleartEffect();

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
    private void cleartEffect() {
        invalidate();
        isTouchDown = false;

        if (va != null && va.isRunning()) {
            va.cancel();
        }
        va = ValueAnimator.ofFloat(this.currentGraphSize, 5f);

        va.setDuration(200);
        va.setInterpolator(new AccelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentGraphSize = (float) (animation.getAnimatedValue());
                if (touchCurrentX > 0 || touchCurrentY > 0) {
                    if (currentGraphSize < iconRadius) {
                        touchCurrentX = 0f;
                        touchCurrentY = 0f;
                        gestureStartTime = 0;
                    }
                }
                if (currentGraphSize <= 8f && !isTouchDown) {
                    resumeBackupSize();
                }
                invalidate();
            }
        });
        va.start();
    }

    /**
     * 计算手势中的图标显示位置
     */
    private RectF getEffectIconRectF(float centerX, float centerY) {
        return new RectF(centerX - iconRadius, centerY - iconRadius, centerX + iconRadius, centerY + iconRadius);
    }

    @Override
    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentGraphSize < 6f) {
            return;
        }

        // 纵向触摸条
        if (orientation == LEFT) {
            if (touchStartY > 0) {
                Path path = new Path();
                float graphHeight = -currentGraphSize;
                float graphWidth = effectWidth;
                float centerX = -graphHeight;
                float centerY = touchStartY; // height / 2

                path.moveTo((centerX + graphHeight), (centerY - cushion));
                path.quadTo((centerX + graphHeight * 2), (centerY - graphWidth), centerX, (centerY - graphWidth / 2)); // 左侧平缓弧线
                path.quadTo((centerX - graphHeight * 2.4f), centerY, centerX, (centerY + graphWidth / 2)); // 顶部圆拱
                path.quadTo((centerX + graphHeight * 2), (centerY + graphWidth), (centerX + graphHeight), (centerY + cushion)); // 右侧平缓弧线

                canvas.drawPath(path, p);

                if (drawIcon && touchCurrentX - touchStartX > FLIP_DISTANCE) {
                    try {
                        canvas.drawBitmap(
                                isLongTimeGesture ? touch_tasks : touch_arrow_right,
                                null,
                                getEffectIconRectF(centerX, centerY),
                                p);
                    } catch (Exception ex) {
                        Log.e("图标渲染错误", ex.getLocalizedMessage());
                        drawIcon = false;
                    }
                }
            }
        } else if (orientation == RIGHT) {
            if (touchStartY > 0) {
                Path path = new Path();
                float graphHeight = currentGraphSize;
                float graphWidth = effectWidth;
                float centerX = this.getWidth() - graphHeight;
                float centerY = touchStartY; // height / 2

                path.moveTo((centerX + graphHeight), (centerY - cushion));
                path.quadTo((centerX + graphHeight * 2), (centerY - graphWidth), centerX, (centerY - graphWidth / 2)); // 左侧平缓弧线
                path.quadTo((centerX - graphHeight * 2.4f), centerY, centerX, (centerY + graphWidth / 2)); // 顶部圆拱
                path.quadTo((centerX + graphHeight * 2), (centerY + graphWidth), (centerX + graphHeight), (centerY + cushion)); // 右侧平缓弧线

                canvas.drawPath(path, p);

                if (drawIcon && touchStartX - touchCurrentX > FLIP_DISTANCE) {
                    try {
                        canvas.drawBitmap(
                                isLongTimeGesture ? touch_tasks : touch_arrow_left,
                                null,
                                getEffectIconRectF(centerX, centerY),
                                p);
                    } catch (Exception ex) {
                        Log.e("图标渲染错误", ex.getLocalizedMessage());
                        drawIcon = false;
                    }
                }
            }
        }
        // 横向触摸条
        else {
            if (touchStartX > 0) {
                Path path = new Path();
                float graphHeight = currentGraphSize; // 35
                float graphWidth = effectWidth;
                float centerX = touchStartX; // width / 2
                float centerY = this.getHeight() - graphHeight;

                path.moveTo((centerX - cushion), (centerY + graphHeight)); //贝赛尔的起始点moveTo(x,y)
                path.quadTo((centerX - graphWidth), (centerY + graphHeight * 2), (centerX - graphWidth / 2), centerY); // 左侧平缓弧线
                path.quadTo(centerX, (centerY - graphHeight * 2.5f), (centerX + graphWidth / 2), centerY); // 顶部圆拱
                path.quadTo((centerX + graphWidth), (centerY + graphHeight * 2), (centerX + cushion), (centerY + graphHeight)); // 右侧平缓弧线

                canvas.drawPath(path, p);

                if (drawIcon && touchStartY - touchCurrentY > FLIP_DISTANCE) {
                    try {
                        canvas.drawBitmap(
                                isLongTimeGesture ? touch_tasks : touch_home,
                                null,
                                getEffectIconRectF(centerX, centerY),
                                p);
                    } catch (Exception ex) {
                        Log.e("图标渲染错误", ex.getLocalizedMessage());
                        drawIcon = false;
                    }
                }
            }
        }
    }
}
