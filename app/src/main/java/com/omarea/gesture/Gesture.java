package com.omarea.gesture;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

public class Gesture extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private static Vibrator vibrator;
    private static SharedPreferences config;

    public static enum VibrateMode {
        VIBRATE_CLICK,
        VIBRATE_PRESS,
        VIBRATE_SLIDE_HOVER,
        VIBRATE_SLIDE
    }

    public static void vibrate(VibrateMode mode, View view) {
        if (vibrator == null) {
            config = context.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
            vibrator = (Vibrator) (context.getSystemService(Context.VIBRATOR_SERVICE));
        }

        if (vibrator.hasVibrator()) {
            if (config.getBoolean(SpfConfig.VIBRATOR_USE_SYSTEM, SpfConfig.VIBRATOR_USE_SYSTEM_DEFAULT)) {
                switch (mode) {
                    case VIBRATE_CLICK:{
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        return;
                    }
                    case VIBRATE_PRESS:{
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                    case VIBRATE_SLIDE_HOVER:{
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                    case VIBRATE_SLIDE:{
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                        return;
                    }
                    default:{  }
                }
            } else {
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = this;

        new AdbProcessExtractor().extract(context);
    }
}
