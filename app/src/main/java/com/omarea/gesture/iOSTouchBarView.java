package com.omarea.gesture;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

class iOSTouchBarView extends View {
    private Context context = getContext();

    private Paint p = new Paint();

    public iOSTouchBarView(Context context) {
        super(context);
        init();
    }

    public iOSTouchBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public iOSTouchBarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    private void init() {
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xf0f0f0f0);
        p.setStrokeWidth(dp2px(context, 8));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setShadowLayer(dp2px(context, 4), 0, 0, 0x88000000);

        // setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private float shadowSize = 16f;
    private float lineWeight = 16f;
    private float margin = 24f;
    void setStyle(int widthPx, int heightPx, int color, int shadowColor, int shadowSizeDp, int lineWeightDp) {
        this.shadowSize = dp2px(context, shadowSizeDp);
        this.lineWeight = dp2px(context, lineWeightDp);
        this.margin = shadowSize + (lineWeight / 2f);

        p.setColor(color);
        p.setShadowLayer(shadowSize, 0, 0, shadowColor);
        p.setStrokeWidth(lineWeight);

        ViewGroup.LayoutParams lp = this.getLayoutParams();
        int h = heightPx;
        int w = widthPx;
        if (h < 1) {
            h = 1;
        }
        if (w < 1) {
            w = 1;
        }
        lp.width = w;
        lp.height = h;
        this.setLayoutParams(lp);
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(margin, margin, getWidth() - margin, margin, p);
    }
}
