package com.omarea.gesture;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;

import com.omarea.gesture.fragments.Fragment3Section;
import com.omarea.gesture.fragments.FragmentBasic;
import com.omarea.gesture.fragments.FragmentOther;
import com.omarea.gesture.fragments.FragmentSimple;
import com.omarea.gesture.fragments.FragmentWhiteBar;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.Memory;


public class SettingsActivity extends Activity {
    private boolean inLightMode = false;

    private void setTheme(boolean restart) {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        boolean isNightMode = uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;

        if (!isNightMode) {
            setTheme(R.style.gestureAppThemeLight);
            // 设置白色状态栏
            View window = this.getWindow().getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        if (inLightMode != isNightMode && restart) {
            recreate();
        }
        inLightMode = isNightMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (new Memory().getMemorySizeMB(this) > 4096) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        setTheme(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_settings);

        try {
            final TabHost tabHost = findViewById(R.id.main_tabhost);
            tabHost.setup();
            tabHost.addTab(tabHost.newTabSpec("Basic")
                    .setContent(R.id.main_tab_0).setIndicator("", getDrawable(R.drawable.gesture_tab_switch)));
            tabHost.addTab(tabHost.newTabSpec("WhiteBar")
                    .setContent(R.id.main_tab_1).setIndicator("", getDrawable(R.drawable.gesture_tab_apple)));
            tabHost.addTab(tabHost.newTabSpec("Edge")
                    .setContent(R.id.main_tab_2).setIndicator("", getDrawable(R.drawable.gesture_tab_lab)));
            tabHost.addTab(tabHost.newTabSpec("ThreeSection")
                    .setContent(R.id.main_tab_3).setIndicator("", getDrawable(R.drawable.gesture_tab_edge)));
            tabHost.addTab(tabHost.newTabSpec("Other")
                    .setContent(R.id.main_tab_4).setIndicator("", getDrawable(R.drawable.gesture_tab_settings)));

            getFragmentManager().beginTransaction()
                    .replace(R.id.main_tab_0, new FragmentBasic()).commit();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_tab_1, new FragmentWhiteBar()).commit();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_tab_2, new FragmentSimple()).commit();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_tab_3, new Fragment3Section()).commit();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_tab_4, new FragmentOther()).commit();
        } catch (Exception ignored) {
        }

    }

    private void updateView() {
        try {
            Intent intent = new Intent(getString(R.string.action_config_changed));
            sendBroadcast(intent);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        GlobalState.testMode = true;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        }, 500);

        setTheme(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalState.testMode = false;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        }, 500);
    }


    private void setExcludeFromRecent(boolean excludeFromRecents) {
        try {
            ActivityManager service = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            int taskId = this.getTaskId();
            for (ActivityManager.AppTask task : service.getAppTasks()) {
                if (task.getTaskInfo().id == taskId) {
                    task.setExcludeFromRecents(excludeFromRecents);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        setExcludeFromRecent(true);
        super.finishAfterTransition();
    }
}
