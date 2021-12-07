package com.omarea.gesture.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.util.GestureActions;

public class TouchIconCache {
    private static Bitmap touch_arrow_left, touch_tasks, touch_home, touch_lock, touch_notice, touch_power, touch_settings, touch_split, touch_info, touch_screenshot, touch_switch, touch_jump_previous, touch_jump_next, touch_window, touch_app, touch_grid, touch_shell, touch_app_window; // 图标资源

    static Bitmap getIcon(int action) {
        if (!Gesture.config.getBoolean(SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT)) {
            switch (action) {
                case GestureActions.GLOBAL_ACTION_BACK: {
                    if (touch_arrow_left == null && Gesture.context != null) {
                        touch_arrow_left = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_arrow_left);
                    }
                    return touch_arrow_left;
                }
                case GestureActions.GLOBAL_ACTION_HOME: {
                    if (touch_home == null && Gesture.context != null) {
                        touch_home = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_home);
                    }
                    return touch_home;
                }
                case GestureActions.GLOBAL_ACTION_RECENTS: {
                    if (touch_tasks == null && Gesture.context != null) {
                        touch_tasks = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_tasks);
                    }
                    return touch_tasks;
                }
                case GestureActions.GLOBAL_ACTION_LOCK_SCREEN: {
                    if (touch_lock == null && Gesture.context != null) {
                        touch_lock = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_lock);
                    }
                    return touch_lock;
                }
                case GestureActions.GLOBAL_ACTION_NOTIFICATIONS: {
                    if (touch_notice == null && Gesture.context != null) {
                        touch_notice = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_notice);
                    }
                    return touch_notice;
                }
                case GestureActions.GLOBAL_ACTION_POWER_DIALOG: {
                    if (touch_power == null && Gesture.context != null) {
                        touch_power = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_power);
                    }
                    return touch_power;
                }
                case GestureActions.GLOBAL_ACTION_QUICK_SETTINGS: {
                    if (touch_settings == null && Gesture.context != null) {
                        touch_settings = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_settings);
                    }
                    return touch_settings;
                }
                case GestureActions.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN: {
                    if (touch_split == null && Gesture.context != null) {
                        touch_split = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_split);
                    }
                    return touch_split;
                }
                case GestureActions.GLOBAL_ACTION_TAKE_SCREENSHOT: {
                    if (touch_screenshot == null && Gesture.context != null) {
                        touch_screenshot = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_screenshot);
                    }
                    return touch_screenshot;
                }
                case GestureActions.VITUAL_ACTION_PREV_APP: {
                    if (touch_jump_previous == null && Gesture.context != null) {
                        touch_jump_previous = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_jump_previous);
                    }
                    return touch_jump_previous;
                }
                case GestureActions.VITUAL_ACTION_NEXT_APP: {
                    if (touch_jump_next == null && Gesture.context != null) {
                        touch_jump_next = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_jump_next);
                    }
                    return touch_jump_next;
                }
                case GestureActions.VITUAL_ACTION_FORM: {
                    if (touch_window == null && Gesture.context != null) {
                        touch_window = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_window);
                    }
                    return touch_window;
                }
                case GestureActions.CUSTOM_ACTION_APP: {
                    if (touch_app == null && Gesture.context != null) {
                        touch_app = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_app);
                    }
                    return touch_app;
                }
                case GestureActions.CUSTOM_ACTION_APP_WINDOW: {
                    if (touch_app_window == null && Gesture.context != null) {
                        touch_app_window = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_app_window);
                    }
                    return touch_app_window;
                }
                case GestureActions.CUSTOM_ACTION_SHELL: {
                    if (touch_shell == null && Gesture.context != null) {
                        touch_shell = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_shell);
                    }
                    return touch_shell;
                }
                case GestureActions.CUSTOM_ACTION_QUICK: {
                    if (touch_grid == null && Gesture.context != null) {
                        touch_grid = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_grid);
                    }
                    return touch_grid;
                }
                case GestureActions.VITUAL_ACTION_SWITCH_APP: {
                    if (touch_switch == null && Gesture.context != null) {
                        touch_switch = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_switch);
                    }
                    return touch_switch;
                }
            }
        }

        if (touch_info == null && Gesture.context != null) {
            touch_info = BitmapFactory.decodeResource(Gesture.context.getResources(), R.drawable.touch_info);
        }
        return touch_info;
    }
}
