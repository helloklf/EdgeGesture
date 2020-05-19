package com.omarea.gesture.util;

import android.graphics.Bitmap;

import com.omarea.gesture.ActionModel;
import com.omarea.gesture.ui.VisualFeedbackView;

public class GlobalState {
    // 使用连续动作
    public static boolean consecutive = false;
    // 连续动作
    public static ActionModel consecutiveAction = null;
    // 连续动作重复间隔
    public static long consecutiveActionPeriod = 200;
    // 最后一次触发连续动作的时间
    public static long consecutiveActionLastTime = 0;

    public static long lastBackHomeTime = 0;

    public static boolean isLandscapf = false;
    public static boolean testMode = false;
    public static int iosBarColor = Integer.MIN_VALUE;
    public static Runnable updateBar;
    public static int displayHeight = 2340;
    public static int displayWidth = 1080;
    // 增强模式（需要Root或者ADB）
    public static boolean enhancedMode = false;

    public static VisualFeedbackView visualFeedbackView;

    public static void startEdgeFeedback(float startRawX, float startRawY, int sideMode) {
        if (visualFeedbackView != null) {
            visualFeedbackView.startEdgeFeedback(startRawX, startRawY, sideMode);
        }
    }

    public static void updateEdgeFeedback(float currentRawX, float currentRawY) {
        if (visualFeedbackView != null) {
            visualFeedbackView.updateEdgeFeedback(currentRawX, currentRawY);
        }
    }

    public static void updateEdgeFeedbackIcon(Bitmap bitmap, boolean oversize) {
        if (visualFeedbackView != null) {
            visualFeedbackView.updateEdgeFeedbackIcon(bitmap, oversize);
        }
    }

    public static void clearEdgeFeedback() {
        if (visualFeedbackView != null) {
            visualFeedbackView.clearEdgeFeedback();
        }
    }
}
