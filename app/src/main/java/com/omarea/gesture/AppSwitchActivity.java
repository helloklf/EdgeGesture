package com.omarea.gesture;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.omarea.gesture.util.AppLauncher;
import com.omarea.gesture.util.AppWindowed;

import java.lang.reflect.Method;

public class AppSwitchActivity extends Activity {
    public static Intent getOpenAppIntent(final AccessibilityServiceGesture accessibilityService) {
        Intent intent = new Intent(accessibilityService, AppSwitchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    switchApp(appPackageName, R.anim.gesture_next_enter_2, R.anim.gesture_next_exit_2);
                } else if (animation == SpfConfig.HOME_ANIMATION_BASIC) {
                    switchApp(appPackageName, R.anim.gesture_next_enter, R.anim.gesture_next_exit);
                } else if (animation == SpfConfig.HOME_ANIMATION_FAST) {
                    switchApp(appPackageName, R.anim.gesture_next_enter_fast, R.anim.gesture_next_exit_fast);
                } else {
                    switchApp(appPackageName, R.anim.gesture_next_enter_basic, R.anim.gesture_next_exit_basic);
                }
            } else if (currentIntent.hasExtra("prev")) {
                String appPackageName = currentIntent.getStringExtra("prev");
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                    switchApp(appPackageName, R.anim.gesture_prev_enter_2, R.anim.gesture_prev_exit_2);
                } else if (animation == SpfConfig.HOME_ANIMATION_BASIC) {
                    switchApp(appPackageName, R.anim.gesture_prev_enter, R.anim.gesture_prev_exit);
                } else if (animation == SpfConfig.HOME_ANIMATION_FAST) {
                    switchApp(appPackageName, R.anim.gesture_prev_enter_fast, R.anim.gesture_prev_exit_fast);
                } else {
                    switchApp(appPackageName, R.anim.gesture_prev_enter_basic, R.anim.gesture_prev_exit_basic);
                }
            } else if (currentIntent.hasExtra("form")) {
                String appPackageName = currentIntent.getStringExtra("form");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    new AppWindowed().switchToFreeForm(this, appPackageName);
                }
            } else if (currentIntent.hasExtra("app-window")) {
                String appPackageName = currentIntent.getStringExtra("app-window");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    new AppWindowed().switchToFreeForm(this, appPackageName);
                }
            } else if (currentIntent.hasExtra("app")) {
                String appPackageName = currentIntent.getStringExtra("app");
                startActivity(appPackageName);
            } else if (currentIntent.hasExtra("home")) {
                String value = currentIntent.getStringExtra("home");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                if (animation == SpfConfig.HOME_ANIMATION_CUSTOM || animation == SpfConfig.HOME_ANIMATION_BASIC) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    boolean anim2 = animation == SpfConfig.HOME_ANIMATION_CUSTOM;
                    int homeAnim;
                    int appAnim;
                    if (value.equals("prev")) {
                        if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                            homeAnim = R.anim.gesture_prev_enter_2;
                            appAnim = R.anim.gesture_prev_exit_2;
                        } else if (animation == SpfConfig.HOME_ANIMATION_BASIC) {
                            homeAnim = R.anim.gesture_prev_enter;
                            appAnim = R.anim.gesture_prev_exit;
                        } else {
                            homeAnim = R.anim.gesture_prev_enter_basic;
                            appAnim = R.anim.gesture_prev_exit_basic;
                        }
                    } else if (value.equals("next")) {
                        if (animation == SpfConfig.HOME_ANIMATION_CUSTOM) {
                            homeAnim = R.anim.gesture_next_enter_2;
                            appAnim = R.anim.gesture_next_exit_2;
                        } else if (animation == SpfConfig.HOME_ANIMATION_BASIC) {
                            homeAnim = R.anim.gesture_next_enter;
                            appAnim = R.anim.gesture_next_exit;
                        } else {
                            homeAnim = R.anim.gesture_next_enter_basic;
                            appAnim = R.anim.gesture_next_exit_basic;
                        }
                    } else {
                        homeAnim = anim2 ? R.anim.gesture_back_home_2 : R.anim.gesture_back_home;
                        appAnim = anim2 ? R.anim.gesture_app_exit_2 : R.anim.gesture_app_exit;
                    }
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this.getApplicationContext(), homeAnim, appAnim);
                    // 很奇怪，在三星手机的OneUI（Android P）系统上，必须先overridePendingTransition再启动startActivity方可覆盖动画
                    overridePendingTransition(homeAnim, appAnim);
                    startActivity(intent, activityOptions.toBundle());
                    overridePendingTransition(homeAnim, appAnim);
                } else {
                    startActivity(intent);
                }
            }
        } catch (Exception ex) {
            Gesture.toast("" + ex.getMessage(), Toast.LENGTH_SHORT);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 50);
    }

    private void startActivity(String packageName) {
        switchApp(packageName, R.anim.gesture_app_open_enter, R.anim.gesture_app_open_exit);
    }

    private void switchApp(String appPackageName, int enterAnimation, int exitAnimation) {
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this.getApplicationContext(), enterAnimation, exitAnimation);
        new AppLauncher().startActivity(this, appPackageName, activityOptions);
        overridePendingTransition(enterAnimation, exitAnimation);
    }
}
