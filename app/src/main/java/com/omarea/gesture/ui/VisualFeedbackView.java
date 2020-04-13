package com.omarea.gesture.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.UITools;

public class VisualFeedbackView extends View {
    public VisualFeedbackView(Context context) {
        super(context);
        init();
    }

    public VisualFeedbackView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public VisualFeedbackView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    private void init() {
        GlobalState.visualFeedbackView = this;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (GlobalState.visualFeedbackView == this) {
            GlobalState.visualFeedbackView = null;
        }
        super.onDetachedFromWindow();
    }

    // 常量，表示无效坐标
    private final float INVALID_VALUE = -1;

    // 手势开始位置
    private float startRawX = INVALID_VALUE;
    private float startRawY = INVALID_VALUE;
    // 最后的手指位置
    private float targetRawX = INVALID_VALUE;
    private float targetRawY = INVALID_VALUE;
    // 是否让视觉反馈图形变得更大（以区别短滑和悬停）
    private boolean oversize = false;
    // 手势处于哪个边缘（左、右、下）
    private int sideMode = -1;

    // 当前视觉反馈效果图形对应的手指停留坐标
    private float currentRawX;
    private float currentRawY;

    // 触摸灵敏度（滑动多长距离认为是手势）
    private int FLIP_DISTANCE = UITools.dp2px(getContext(), 25f);

    // 手势动作提示图标
    private Bitmap actionIcon;

    // 设置手势开始位置，以便确定视觉反馈效果显示起点
    public void startEdgeFeedback(float startRawX, float startRawY, int sideMode) {
        stopAnimation();

        this.startRawX = startRawX;
        this.startRawY = startRawY;
        this.sideMode = sideMode;
        this.updateEdgeFeedbackIcon(null, false);

        invalidate();

        // updateFeedback();
    }

    // 根据手指停留坐标更新视觉反馈效果
    public void updateEdgeFeedback(float targetRawX, float targetRawY) {
        this.targetRawX = targetRawX;
        this.targetRawY = targetRawY;

        updateFeedback();
    }

    // 设置视觉反馈提示图标
    public void updateEdgeFeedbackIcon(Bitmap bitmap, boolean oversize) {
        this.actionIcon = bitmap;
        this.oversize = oversize;
    }

    // 手势视觉反馈淡出动画
    private ValueAnimator feedbackAnimation;

    // 停止动画
    private void stopAnimation() {
        if (feedbackAnimation != null) {
            feedbackAnimation.cancel();
            feedbackAnimation = null;
        }
    }

    // 淡出视觉反馈图形
    public void clearEdgeFeedback() {
        stopAnimation();

        if (sideMode == TouchBarView.BOTTOM) {
            feedbackAnimation = ValueAnimator.ofFloat(this.currentRawY, this.getHeight());
        } else if (sideMode == TouchBarView.LEFT) {
            feedbackAnimation = ValueAnimator.ofFloat(this.currentRawX, 0);
        } else if (sideMode == TouchBarView.RIGHT) {
            feedbackAnimation = ValueAnimator.ofFloat(this.currentRawX, this.getWidth());
        } else {
            targetRawX = INVALID_VALUE;
            targetRawY = INVALID_VALUE;
            invalidate();
            return;
        }

        feedbackAnimation.setDuration(260);
        feedbackAnimation.setStartDelay(40);
        feedbackAnimation.setInterpolator(new DecelerateInterpolator());
        feedbackAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                if (sideMode == TouchBarView.BOTTOM) {
                    currentRawY = value;
                    invalidate();
                } else if (sideMode == TouchBarView.LEFT) {
                    currentRawX = value;
                    invalidate();
                } else if (sideMode == TouchBarView.RIGHT) {
                    currentRawX = value;
                    invalidate();
                }
            }
        });

        feedbackAnimation.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                targetRawX = INVALID_VALUE;
                targetRawY = INVALID_VALUE;
                invalidate();
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                targetRawX = INVALID_VALUE;
                targetRawY = INVALID_VALUE;
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        feedbackAnimation.start();
    }

    // 更新视觉显示
    private void updateFeedback() {
        this.currentRawX = this.targetRawX;
        this.currentRawY = this.targetRawY;

        invalidate();
    }

    // 是否是无效的手势坐标（坐标无效时不显示视觉反馈）
    private boolean isInvalid() {
        return startRawX == INVALID_VALUE || startRawY == INVALID_VALUE || targetRawX == INVALID_VALUE || targetRawY == INVALID_VALUE;
    }

    // 这记录的一条弧线的关键点坐标，数据格式为 x,y,x,y...
    float[] shape = new float[]{
            0.115f, 0.167f,
            0.620f, 0.350f,
            1.000f, 0.500f,
            0.620f, 0.650f,
            0.115f, 0.833f,
            0.000f, 1.000f
    };

    // 视觉反馈图形显示尺寸
    float GRAPH_MAX_SIZE = UITools.dp2px(getContext(), 220);
    float GRAPH_MAX_WEIGH = UITools.dp2px(getContext(), 38);

    // 根据滑动距离计算视觉反馈显示大小
    private float graphSize(float startRaw, float currentRaw) {
        if (Math.abs(currentRaw - startRaw) > FLIP_DISTANCE) {
            if (oversize) {
                return GRAPH_MAX_WEIGH * 1.2f;
            } else {
                return GRAPH_MAX_WEIGH;
            }
        } else {
            return (Math.abs(currentRaw - startRaw) / FLIP_DISTANCE) * GRAPH_MAX_WEIGH;
        }
    }

    // 手势提示图标半径
    private float iconRadius = UITools.dp2px(getContext(), 8f);

    // 绘制手势提示图标
    private void drawIcon(Canvas canvas, float startX, float startY) {
        if (actionIcon != null) {
            canvas.drawBitmap(actionIcon,
                    null,
                    new RectF(startX, startY, startX + iconRadius * 2, startY + iconRadius * 2),
                    null
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float graphW = GRAPH_MAX_SIZE;
        float graphH;

        Paint paint = new Paint();
        paint.setStrokeWidth(6);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        if (!isInvalid()) {
            // 视觉效果绘制中点
            float drawStartX;
            float drawStartY;

            // 左侧边缘手势视觉反馈
            if (sideMode == TouchBarView.LEFT) {
                drawStartY = startRawY - (graphW * 0.5f);
                drawStartX = 0;
                Path path = new Path();
                path.moveTo(0, drawStartY);

                graphH = graphSize(drawStartX, currentRawX);

                for (int i = 0; i < shape.length; i += 4) {
                    float offsetX = shape[i];
                    float offsetY = shape[i + 1];
                    float offsetX2 = shape[i + 2];
                    float offsetY2 = shape[i + 3];
                    path.quadTo(
                            drawStartX + (graphH * offsetX), drawStartY + (graphW * offsetY),
                            drawStartX + (graphH * offsetX2), drawStartY + (graphW * offsetY2)
                    );
                }

                canvas.drawPath(path, paint);

                if (graphH >= GRAPH_MAX_WEIGH && actionIcon != null) {
                    drawIcon(canvas, drawStartX + iconRadius, drawStartY + (graphW / 2) - iconRadius);
                }
            }
            // 底部边缘手势视觉反馈
            else if (sideMode == TouchBarView.BOTTOM) {
                // Bottom
                Path path = new Path();
                drawStartY = this.getHeight();
                drawStartX = this.startRawX - (graphW * 0.5f);
                path.moveTo(drawStartX, drawStartY);

                graphH = graphSize(drawStartY, currentRawY);

                for (int i = 0; i < shape.length; i += 4) {
                    float offsetX = shape[i];
                    float offsetY = shape[i + 1];
                    float offsetX2 = shape[i + 2];
                    float offsetY2 = shape[i + 3];
                    path.quadTo(
                            drawStartX + (graphW * offsetY), drawStartY - (graphH * offsetX),
                            drawStartX + (graphW * offsetY2), drawStartY - (graphH * offsetX2)
                    );
                }

                canvas.drawPath(path, paint);

                if (graphH >= GRAPH_MAX_WEIGH && actionIcon != null) {
                    drawIcon(canvas, drawStartX + (graphW / 2) - iconRadius, drawStartY - iconRadius * 3);
                }
            }
            // 右侧边缘手势视觉反馈
            else if (sideMode == TouchBarView.RIGHT) {
                drawStartY = startRawY - (graphW * 0.5f);
                drawStartX = getWidth();
                Path path = new Path();
                path.moveTo(drawStartX, drawStartY);

                graphH = graphSize(drawStartX, currentRawX);

                for (int i = 0; i < shape.length; i += 4) {
                    float offsetX = shape[i];
                    float offsetY = shape[i + 1];
                    float offsetX2 = shape[i + 2];
                    float offsetY2 = shape[i + 3];
                    path.quadTo(
                            drawStartX - (graphH * offsetX), drawStartY + (graphW * offsetY),
                            drawStartX - (graphH * offsetX2), drawStartY + (graphW * offsetY2)
                    );
                }

                canvas.drawPath(path, paint);

                if (graphH >= iconRadius * 3 && actionIcon != null) {
                    drawIcon(canvas, drawStartX - iconRadius * 3, drawStartY + (graphW / 2) - iconRadius);
                }
            }
            // Unknown
            else {
                return;
            }
            Log.d("GestureFeedback", "" + graphH);
        } else {
            /*
            float drawStartX = 0f;
            float drawStartY = 0f;
            Path path = new Path();
            path.moveTo(drawStartX, drawStartY);
            for (int i = 0; i < shape.length; i += 4) {
                float offsetX = shape[i];
                float offsetY = shape[i + 1];
                float offsetX2 = shape[i + 2];
                float offsetY2 = shape[i + 3];
                path.quadTo(
                        drawStartX + (graphH * offsetX), drawStartY + (graphW * offsetY),
                        drawStartX + (graphH * offsetX2), drawStartY + (graphW * offsetY2)
                );
            }
            Log.d("GestureFeedback", "" + startRawX + "," + startRawY + " > " + targetRawX + "," + targetRawY);

            canvas.drawPath(path, paint);
            */
        }
    }
}
