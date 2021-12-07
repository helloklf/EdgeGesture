package com.omarea.gesture.util;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.omarea.gesture.Gesture;

public class AppLauncher {
    Intent getAppSwitchIntent(String appPackageName) {
        Intent i = Gesture.context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        i.setFlags((i.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        i.setPackage(null);
        return i;
    }

    public void startActivity(Context context, String targetApp) {
        try {
            context.startActivity(getAppSwitchIntent(targetApp));
        } catch (Exception ex) {
            Gesture.toast("" + ex.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    public void startActivity(Context context, String targetApp, ActivityOptions activityOptions) {
        context.startActivity(getAppSwitchIntent(targetApp), activityOptions.toBundle());
    }
}
