package com.omarea.gesture.util;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.omarea.gesture.R;
import com.omarea.gesture.daemon.RemoteAPI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AppWindowed {
    public void switchToFreeForm(Context context, String packageName) {
        // ActivityOptions activityOptions = ActivityOptions.makeTaskLaunchBehind();
        // 设置不合理的动画，可能导致切换窗口模式时奔溃，因此去掉动画
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context.getApplicationContext(), 0, 0);

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Method method = activityOptions.getClass().getMethod("setLaunchWindowingMode", int.class);
            method.invoke(activityOptions, 5);
        } catch (Exception e) {
            if (RemoteAPI.isOnline()) {
                RemoteAPI.windowingMode(packageName + "/" + intent.getComponent().getClassName());
                return;
            } else {
                Toast.makeText(context, R.string.windowed_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            // e.printStackTrace();
        }

        // int left = 50;
        // int top = 100;
        // int right = 50 + GlobalState.displayWidth / 2;
        // int bottom = 100 + (GlobalState.displayWidth / 2 * 16 / 9);
        // activityOptions.setLaunchBounds(new Rect(left, top, right, bottom));

        Bundle bundle = activityOptions.toBundle();
        context.startActivity(intent, bundle);
    }
}
