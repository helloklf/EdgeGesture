package com.omarea.gesture.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.omarea.gesture.AccessibilityServiceGesture;
import com.omarea.gesture.AppSwitchActivity;
import com.omarea.gesture.DialogFrequentlyAppEdit;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfigEx;
import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.util.GlobalState;
import com.omarea.gesture.util.UITools;

import java.util.ArrayList;

public class QuickPanel {
    @SuppressLint("StaticFieldLeak")
    private static View view;
    private static WindowManager mWindowManager;
    private AccessibilityServiceGesture accessibilityService;
    private ArrayList<AppInfo> apps;

    public QuickPanel(AccessibilityServiceGesture context) {
        accessibilityService = context;
    }

    public static void close() {
        if (view != null && mWindowManager != null) {
            mWindowManager.removeView(view);
            view = null;
        }
    }

    private WindowManager.LayoutParams getLayoutParams() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(accessibilityService)) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (GlobalState.isLandscapf) {
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            params.height = UITools.dp2px(accessibilityService, 480); // WindowManager.LayoutParams.MATCH_PARENT;
        }

        /*
        if (x > 0 && y > 0) {
            params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            // params.gravity = Gravity.START | Gravity.TOP;

            // params.x = x - UITools.dp2px(accessibilityService, 115);
            // params.y = y - UITools.dp2px(accessibilityService, 110);
        } else {
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
        */
        // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_BLUR_BEHIND; // 模糊背景 不支持淡入淡出
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.7f;

        params.windowAnimations = android.R.style.Animation_Translucent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        return params;
    }

    private void saveConfig() {
        SharedPreferences config = accessibilityService.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        if (apps != null) {
            StringBuilder configApps = new StringBuilder();
            for (AppInfo appInfo : apps) {
                configApps.append(appInfo.packageName);
                configApps.append(",");
            }
            config.edit().putString(SpfConfigEx.frequently_apps, configApps.toString()).apply();
        }
    }

    private String[] getCurrentConfig() {
        SharedPreferences config = accessibilityService.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);

        return config.getString(SpfConfigEx.frequently_apps,
                "com.android.contacts,com.android.mms,com.android.browser,com.android.camera,com.tencent.mm,com.tencent.mobileqq,com.eg.android.AlipayGphone,com.netease.cloudmusic,com.omarea.vtools").split(",");
    }

    private ArrayList<AppInfo> listFrequentlyApps() {
        final String[] apps = getCurrentConfig();

        ArrayList<AppInfo> appInfos = new ArrayList<>();
        final PackageManager pm = accessibilityService.getPackageManager();
        for (String app : apps) {
            if (app.isEmpty()) {
                continue;
            }
            try {
                AppInfo appInfo = new AppInfo(app);
                ApplicationInfo applicationInfo = pm.getApplicationInfo(app, 0);
                appInfo.appName = (String) applicationInfo.loadLabel(pm);
                appInfo.icon = applicationInfo.loadIcon(pm);
                appInfos.add(appInfo);
            } catch (Exception ignored) {
            }
        }
        return appInfos;
    }

    private void setFrequentlyAppList(final GridView gridView, final boolean editMode) {
        if (apps == null) {
            apps = listFrequentlyApps();
        }

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return editMode ? (apps.size() + 1) : apps.size();
            }

            @Override
            public Object getItem(int position) {
                return apps.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (position >= apps.size()) {
                    return LayoutInflater.from(accessibilityService).inflate(R.layout.layout_quick_panel_add, null);
                } else {
                    View view = LayoutInflater.from(accessibilityService).inflate(R.layout.gesture_layout_quick_panel_item, null);
                    AppInfo appInfo = (AppInfo) getItem(position);
                    ImageView imageView = view.findViewById(R.id.qp_icon);
                    TextView nameView = view.findViewById(R.id.qp_name);
                    if (appInfo.icon != null) {
                        imageView.setImageDrawable(appInfo.icon);
                    }
                    nameView.setText(appInfo.appName);
                    return view;
                }
            }
        });

        if (editMode) {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= apps.size()) {
                        close();
                        new DialogFrequentlyAppEdit(accessibilityService).openEdit(getCurrentConfig());
                        // Toast.makeText(accessibilityService, accessibilityService.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                    } else {
                        apps.remove(position);
                        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    }
                }
            });
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= apps.size()) {
                        close();
                        new DialogFrequentlyAppEdit(accessibilityService).openEdit(getCurrentConfig());
                        // Toast.makeText(accessibilityService, accessibilityService.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                    } else {
                        apps.remove(position);
                        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    }
                    return true;
                }
            });
        } else {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = AppSwitchActivity.getOpenAppIntent(accessibilityService);
                    intent.putExtra("app", apps.get(position).packageName);
                    if (GlobalState.enhancedMode && System.currentTimeMillis() - GlobalState.lastBackHomeTime < 4800) {
                        RemoteAPI.fixDelay();
                    }
                    accessibilityService.startActivity(intent);

                    close();
                }
            });

            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = AppSwitchActivity.getOpenAppIntent(accessibilityService);
                    intent.putExtra("app-window", apps.get(position).packageName);
                    if (GlobalState.enhancedMode && System.currentTimeMillis() - GlobalState.lastBackHomeTime < 4800) {
                        RemoteAPI.fixDelay();
                    }
                    accessibilityService.startActivity(intent);

                    close();
                    return false;
                }
            });
        }
    }

    public void open(float touchRawX, float touchRawY) {
        mWindowManager = (WindowManager) (accessibilityService.getSystemService(Context.WINDOW_SERVICE));
        close();

        if (GlobalState.isLandscapf && !(touchRawY > GlobalState.displayHeight - (GlobalState.displayHeight * 0.15))) {
            view = LayoutInflater.from(accessibilityService).inflate(R.layout.layout_quick_panel_landscape, null);
        } else {
            view = LayoutInflater.from(accessibilityService).inflate(R.layout.layout_quick_panel, null);
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    close();
                }
                return true;
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        final View editBtn = view.findViewById(R.id.quick_edit);
        final View saveBtn = view.findViewById(R.id.quick_save);
        final View questionBtn = view.findViewById(R.id.quick_question);
        saveBtn.setVisibility(View.GONE);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBtn.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                setFrequentlyAppList((GridView) view.findViewById(R.id.quick_apps), true);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBtn.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);
                saveConfig();
                setFrequentlyAppList((GridView) view.findViewById(R.id.quick_apps), false);
            }
        });
        questionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                Toast.makeText(accessibilityService, accessibilityService.getString(R.string.quick_question), Toast.LENGTH_LONG).show();
            }
        });
        GridView appList = (GridView) view.findViewById(R.id.quick_apps);
        setFrequentlyAppList(appList, false);

        WindowManager.LayoutParams windowParams = getLayoutParams();

        // 下
        if (touchRawY > GlobalState.displayHeight - (GlobalState.displayHeight * 0.15)) {
            View wrapView = view.findViewById(R.id.quick_apps_wrap);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) wrapView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wrapView.setBackground(accessibilityService.getDrawable(R.drawable.quick_panel_bg_bottom));
            wrapView.setLayoutParams(layoutParams);
            if (GlobalState.isLandscapf) {
                appList.setNumColumns(10);
            } else {
                appList.setNumColumns(5);
            }
            windowParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            windowParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            windowParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            windowParams.windowAnimations = R.style.BottomQuickPanelAnimation;
        }
        // 右
        else if (touchRawX > 100) {
            View wrapView = view.findViewById(R.id.quick_apps_wrap);
            wrapView.setBackground(accessibilityService.getDrawable(R.drawable.quick_panel_bg_right));
            windowParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            windowParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            windowParams.windowAnimations = R.style.RightQuickPanelAnimation;
        }
        // 左
        else {
            Log.d(">>>>", "Left touchRawX" + touchRawX);
            View wrapView = view.findViewById(R.id.quick_apps_wrap);
            wrapView.setBackground(accessibilityService.getDrawable(R.drawable.quick_panel_bg_left));
            windowParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            windowParams.windowAnimations = R.style.LeftQuickPanelAnimation;
            windowParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            // windowParams.windowAnimations = android.R.style.Animation_Dialog;
        }

        mWindowManager.addView(view, windowParams);
    }

    class AppInfo {
        String appName;
        String packageName;
        Drawable icon;

        AppInfo(String packageName) {
            this.packageName = packageName;
        }
    }
}
