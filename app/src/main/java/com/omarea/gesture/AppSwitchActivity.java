package com.omarea.gesture;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

public class AppSwitchActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(">>>>", "11");
        try {
            overridePendingTransition(0, 0);
            Intent currentIntent = getIntent();
            int animation = SpfConfig.HOME_ANIMATION_DEFAULT;
            if (currentIntent.hasExtra("animation")) {
                animation = currentIntent.getIntExtra("animation", SpfConfig.HOME_ANIMATION_DEFAULT);
            }

            if (currentIntent.hasExtra("next")) {
                String appPackageName = currentIntent.getStringExtra("next");
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                    switchApp(appPackageName, R.anim.activity_open_enter_2, R.anim.activity_open_exit_2);
                } else {
                    switchApp(appPackageName, R.anim.activity_open_enter, R.anim.activity_open_exit);
                }
            } else if (currentIntent.hasExtra("prev")) {
                String appPackageName = currentIntent.getStringExtra("prev");
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                    switchApp(appPackageName, R.anim.activity_close_enter_2, R.anim.activity_close_exit_2);
                } else {
                    switchApp(appPackageName, R.anim.activity_close_enter, R.anim.activity_close_exit);
                }
            } else if (currentIntent.hasExtra("form")) {
                String appPackageName = currentIntent.getStringExtra("form");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    startActivityAsFreeForm(appPackageName);
                }
            } else if (currentIntent.hasExtra("app")) {
                String appPackageName = currentIntent.getStringExtra("app");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    startActivity(appPackageName);
                }
            } else if (currentIntent.hasExtra("home")) {
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                intent.addCategory(Intent.CATEGORY_HOME);
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this.getApplicationContext(), R.anim.activity_close_enter_2, R.anim.activity_close_exit_2);
                    // 很奇怪，在三星手机的OneUI（Android P）系统上，必须先overridePendingTransition再启动startActivity方可覆盖动画
                    overridePendingTransition(R.anim.home_enter, R.anim.app_exit);
                    startActivity(intent, activityOptions.toBundle());
                    overridePendingTransition(R.anim.home_enter, R.anim.app_exit);
                } else {
                    startActivity(intent);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 300);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ActivityOptions getActivityOptions() {
        ActivityOptions options = ActivityOptions.makeTaskLaunchBehind();
        try {
            Method method = ActivityOptions.class.getMethod("setLaunchWindowingMode", int.class);
            method.invoke(options, 5);
        } catch (Exception e) { /* Gracefully fail */ }

        return options;
    }

    private void startActivity(String packageName) {
        switchApp(packageName, R.anim.app_open_enter, R.anim.app_open_exit);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startActivityAsFreeForm(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        ActivityOptions activityOptions = getActivityOptions();
        // int left = 50;
        // int top = 100;
        // int right = 800;
        // int bottom = 1100;
        // activityOptions.setLaunchBounds(new Rect(left, top, right, bottom));
        Bundle bundle = activityOptions.toBundle();
        startActivity(intent, bundle);
    }

    /*
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivityAsFreeForm(intent);
    } else {
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.send();
    }
    */
    /*
    @Override
    public void startActivity(Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Toast.makeText(this.getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    */

    private void switchApp(String appPackageName, int enterAnimation, int exitAnimation) {
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this.getApplicationContext(), enterAnimation, exitAnimation);
        startActivity(getAppSwitchIntent(appPackageName), activityOptions.toBundle());
        overridePendingTransition(enterAnimation, exitAnimation);
    }

    private Intent getAppSwitchIntent(String appPackageName) {
        Intent i = getPackageManager().getLaunchIntentForPackage(appPackageName);
        // i.setFlags((i.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK);
        // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // i.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        // i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        // i.setFlags(0x10200000);
        // Log.d("getAppSwitchIntent", "" + i.getFlags());
        i.setFlags((i.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // @参考 https://blog.csdn.net/weixin_34335458/article/details/88020972
        i.setPackage(null); // 加上这句代

        // i.setFlags((i.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // i.setFlags((i.getFlags() | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        // i.setAction(Intent.ACTION_MAIN);
        // i.addCategory(Intent.CATEGORY_LAUNCHER);
        return i;
    }
}
