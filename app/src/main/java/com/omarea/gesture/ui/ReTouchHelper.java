package com.omarea.gesture.ui;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class ReTouchHelper {
    private AccessibilityService mAccessibilityService;
    private WindowManager mWindowManager;
    private View mView;
    public ReTouchHelper(AccessibilityService accessibilityService, WindowManager windowManager, View view) {
        mAccessibilityService = accessibilityService;
        mWindowManager = windowManager;
        mView = view;
    }

    public void dispatchGesture(Path touchPath) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(touchPath, 10L, 20L));
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) mView.getLayoutParams();
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mWindowManager.updateViewLayout(mView, params);
            mAccessibilityService.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    params.flags&= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    mWindowManager.updateViewLayout(mView, params);
                    Log.d("@Gesture", "dispatchGesture √");
                    super.onCompleted(gestureDescription);
                }

                public void onCancelled(GestureDescription gestureDescription) {
                    params.flags&= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    mWindowManager.updateViewLayout(mView, params);
                    Log.e("@Gesture", "dispatchGesture ×");
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }
    }
}
