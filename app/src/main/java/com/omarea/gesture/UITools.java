package com.omarea.gesture;

import android.content.Context;

public class UITools {
    /**
     * dp转换成px
     */
    static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int value = (int) (dpValue * scale + 0.5f);
        if (value < 1) {
            return 1;
        }
        return value;
    }
}
