package com.omarea.gesture;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.omarea.gesture.util.AppInfo;
import com.omarea.gesture.util.AppListHelper;
import com.omarea.gesture.util.UITools;

import java.util.ArrayList;
import java.util.Arrays;

public class DialogFrequentlyAppEdit {
    private AccessibilityServiceGesture accessibilityService;

    public DialogFrequentlyAppEdit(AccessibilityServiceGesture accessibilityServiceGesture) {
        accessibilityService = accessibilityServiceGesture;
    }

    private WindowManager.LayoutParams getLayoutParams() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = UITools.dp2px(accessibilityService, 240); // WindowManager.LayoutParams.MATCH_PARENT;
        params.height = UITools.dp2px(accessibilityService, 300); // WindowManager.LayoutParams.MATCH_PARENT;

        params.gravity = Gravity.CENTER;

        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        return params;
    }

    private ArrayList<String> configApps;

    public void openEdit(final String[] current) {
        configApps = new ArrayList<>(Arrays.asList(current));

        final WindowManager mWindowManager = (WindowManager) (accessibilityService.getSystemService(Context.WINDOW_SERVICE));

        final ArrayList<AppInfo> appInfos = new ArrayList<>();
        for (AppInfo appInfo : new AppListHelper().loadAppList(accessibilityService)) {
            if (!configApps.contains(appInfo.packageName)) {
                appInfos.add(appInfo);
            }
        }
        final boolean[] status = new boolean[appInfos.size()];

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return appInfos.size();
            }

            @Override
            public Object getItem(int position) {
                return appInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view = layoutInflater.inflate(R.layout.gesture_layout_app_option2, null);
                TextView title = view.findViewById(R.id.item_title);
                TextView desc = view.findViewById(R.id.item_desc);
                CheckBox state = view.findViewById(R.id.item_state);

                AppInfo appInfo = (AppInfo) getItem(position);
                title.setText(appInfo.appName);
                desc.setText(appInfo.packageName);
                state.setChecked(status[position]);

                return view;
            }
        };

        final View view = LayoutInflater.from(accessibilityService).inflate(R.layout.layout_frequently_app_edit, null);
        final ListView listView = view.findViewById(R.id.app_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = view.findViewById(R.id.item_state);
                checkBox.setChecked(!checkBox.isChecked());

                status[position] = checkBox.isChecked();
                if (checkBox.isChecked()) {
                    configApps.add(appInfos.get(position).packageName);
                } else {
                    configApps.remove(appInfos.get(position).packageName);
                }
            }
        });

        view.findViewById(R.id.quick_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveConfig(configApps);
                    Toast.makeText(accessibilityService, accessibilityService.getString(R.string.save_succeed), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(accessibilityService, accessibilityService.getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                }

                mWindowManager.removeView(view);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindowManager.removeView(view);
                }
                return true;
            }
        });

        mWindowManager.addView(view, getLayoutParams());
    }

    private void saveConfig(ArrayList<String> apps) {
        SharedPreferences config = accessibilityService.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        if (apps != null) {
            StringBuilder configApps = new StringBuilder();
            for (String app : apps) {
                configApps.append(app);
                configApps.append(",");
            }
            config.edit().putString(SpfConfigEx.frequently_apps, configApps.toString()).apply();
        }
    }
}
