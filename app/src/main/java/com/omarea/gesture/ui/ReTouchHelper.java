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
    private int mFlags;
    private boolean running;
    final WindowManager.LayoutParams mParams;
    public ReTouchHelper(AccessibilityService accessibilityService, WindowManager windowManager, View view) {
        mAccessibilityService = accessibilityService;
        mWindowManager = windowManager;
        mView = view;
        mParams = (WindowManager.LayoutParams) view.getLayoutParams();
        mFlags = mParams.flags;
    }

    public void dispatchGesture(final Path touchPath) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            synchronized (this) {
                if (running) {
                    return;
                }
                Log.e("@Gesture", "dispatchGesture >>");
                pause();
                mView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final GestureDescription.Builder builder = new GestureDescription.Builder();
                            builder.addStroke(new GestureDescription.StrokeDescription(touchPath, 1, 1));
                            mAccessibilityService.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
                                @Override
                                public void onCompleted(GestureDescription gestureDescription) {
                                    resume();
                                    super.onCompleted(gestureDescription);
                                }

                                public void onCancelled(GestureDescription gestureDescription) {
                                    resume();
                                    super.onCancelled(gestureDescription);
                                }
                            }, null);
                        } catch (Exception ignored) {
                            resume();
                        }
                    }
                }, 60);
            }
        }
    }

    private void pause() {
        running = true;
        mParams.flags = mFlags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.updateViewLayout(mView, mParams);
    }

    private void resume() {
        mParams.flags = mFlags ;
        mWindowManager.updateViewLayout(mView, mParams);
        Log.e("@Gesture", "dispatchGesture √");
        mView.postDelayed(new Runnable() {
            @Override
            public void run() {
                running = false;
            }
        }, 60);
    }
}
