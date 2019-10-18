package com.omarea.gesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class TouchIconCache {
    private static Context mContext;
    private static Bitmap touch_arrow_left, touch_arrow_right, touch_tasks, touch_home, touch_lock, touch_notice, touch_power, touch_settings, touch_split, touch_info, touch_screenshot, touch_switch; // 图标资源

    static void setContext(Context context) {
        mContext = context;
    }

    static Bitmap getIcon(int action) {
        switch (action) {
            case Handlers.GLOBAL_ACTION_BACK: {
                if (touch_arrow_left == null && mContext != null) {
                    touch_arrow_left = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_arrow_left);
                }
                return touch_arrow_left;
            }
            case Handlers.GLOBAL_ACTION_HOME: {
                if (touch_home == null && mContext != null) {
                    touch_home = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_home);
                }
                return touch_home;
            }
            case Handlers.GLOBAL_ACTION_RECENTS: {
                if (touch_tasks == null && mContext != null) {
                    touch_tasks = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_tasks);
                }
                return touch_tasks;
            }
            case Handlers.GLOBAL_ACTION_LOCK_SCREEN: {
                if (touch_lock == null && mContext != null) {
                    touch_lock = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_lock);
                }
                return touch_lock;
            }
            case Handlers.GLOBAL_ACTION_NOTIFICATIONS: {
                if (touch_notice == null && mContext != null) {
                    touch_notice = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_notice);
                }
                return touch_notice;
            }
            case Handlers.GLOBAL_ACTION_POWER_DIALOG: {
                if (touch_power == null && mContext != null) {
                    touch_power = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_power);
                }
                return touch_power;
            }
            case Handlers.GLOBAL_ACTION_QUICK_SETTINGS: {
                if (touch_settings == null && mContext != null) {
                    touch_settings = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_settings);
                }
                return touch_settings;
            }
            case Handlers.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN: {
                if (touch_split == null && mContext != null) {
                    touch_split = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_split);
                }
                return touch_split;
            }
            case Handlers.GLOBAL_ACTION_TAKE_SCREENSHOT: {
                if (touch_screenshot == null && mContext != null) {
                    touch_screenshot = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_screenshot);
                }
                return touch_screenshot;
            }
            case Handlers.VITUAL_ACTION_LAST_APP: {
                if (touch_switch == null && mContext != null) {
                    touch_switch = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_switch);
                }
                return touch_switch;
            }
        }
        if (touch_info == null && mContext != null) {
            touch_info = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.touch_info);
        }
        return touch_info;
    }
}
