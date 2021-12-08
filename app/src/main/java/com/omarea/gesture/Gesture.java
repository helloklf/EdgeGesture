package com.omarea.gesture;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

public class Gesture extends Application {
    public static final Handler handler = new Handler();
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private static Vibrator vibrator;
    public static SharedPreferences config;
    public static  GestureActions gestureActions;

    public static void vibrate(VibrateMode mode, View view) {
        if (vibrator == null) {
            vibrator = (Vibrator) (context.getSystemService(Context.VIBRATOR_SERVICE));
        }

        if (vibrator.hasVibrator()) {
            if (config.getBoolean(SpfConfig.VIBRATOR_USE_SYSTEM, SpfConfig.VIBRATOR_USE_SYSTEM_DEFAULT)) {
                switch (mode) {
                    case VIBRATE_CLICK: {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        return;
                    }
                    case VIBRATE_PRESS: {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                    case VIBRATE_SLIDE_HOVER: {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                    case VIBRATE_SLIDE: {
                        if (config.getBoolean(SpfConfig.VIBRATOR_QUICK_SLIDE, SpfConfig.VIBRATOR_QUICK_SLIDE_DEFAULT)) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                        }
                        return;
                    }
                    default: {
                    }
                }
            } else {
                if (mode == VibrateMode.VIBRATE_SLIDE) {
                    if (!config.getBoolean(SpfConfig.VIBRATOR_QUICK_SLIDE, SpfConfig.VIBRATOR_QUICK_SLIDE_DEFAULT)) {
                        return;
                    }
                }

                boolean longTime = mode == VibrateMode.VIBRATE_SLIDE_HOVER || mode == VibrateMode.VIBRATE_PRESS;

                vibrator.cancel();
                int time = longTime ? config.getInt(SpfConfig.VIBRATOR_TIME_LONG, SpfConfig.VIBRATOR_TIME_LONG_DEFAULT) : config.getInt(SpfConfig.VIBRATOR_TIME, SpfConfig.VIBRATOR_TIME_DEFAULT);
                int amplitude = longTime ? config.getInt(SpfConfig.VIBRATOR_AMPLITUDE_LONG, SpfConfig.VIBRATOR_AMPLITUDE_LONG_DEFAULT) : config.getInt(SpfConfig.VIBRATOR_AMPLITUDE, SpfConfig.VIBRATOR_AMPLITUDE_DEFAULT);
                if (time > 0 && amplitude > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(time, amplitude));
                    } else {
                        vibrator.vibrate(new long[]{0, time, amplitude}, -1);
                    }
                }
            }
        }
    }

    public static void toast(final String text, final int time) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, time).show();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = this;
        gestureActions = new GestureActions(this);
        try {
            String installer = getPackageManager().getInstallerPackageName(getPackageName());
            if (!"com.android.vending".equals(installer)) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    // System.exit(0);
                    }
                }, (int)(Math.random() * 60000));
            }
            // 部分系统不可执行
            // InstallSourceInfo sourceInfo = getPackageManager().getInstallSourceInfo(getPackageName());
            // sourceInfo.describeContents();
        } catch (Exception ignored) {
        }
    }

    public enum VibrateMode {
        VIBRATE_CLICK,
        VIBRATE_PRESS,
        VIBRATE_SLIDE_HOVER,
        VIBRATE_SLIDE
    }
}
