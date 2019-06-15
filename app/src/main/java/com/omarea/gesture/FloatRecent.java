package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TabHost;
import android.widget.Toast;

class FloatRecent {

    private static WindowManager mWindowManager = null;
    private View bottomView = null;

    public FloatRecent(AccessibilityService context) {
        mWindowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        try {
            this.bottomView = setBottomView(context);
        } catch (Exception ex) {
            Log.d("异常", ex.getLocalizedMessage());
            Toast.makeText(context, "启动虚拟导航手势失败！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 隐藏弹出框
     */
    void hidePopupWindow() {
        if (this.bottomView != null) {
            mWindowManager.removeView(this.bottomView);
        }
    }

    private void startApp(AccessibilityService context, String appPackageName) {
        try {
            // PackageManager pm = context.getPackageManager();
            // Intent appIntent = pm.getLaunchIntentForPackage(appPackageName);
            Intent appIntent = AppHistory.getAppIntent(appPackageName);
            if (appIntent != null) {
                // Intent.FLAG_ACTIVITY_NO_ANIMATION
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(appIntent);
            }
        } catch (Exception ex) {
            Toast.makeText(context, "无法切换到所选应用", Toast.LENGTH_SHORT).show();
        }
    }

    private View setBottomView(final AccessibilityService context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_rencent_list, null);
        final AbsListView hideList = view.findViewById(R.id.recent_hide_list);
        final AbsListView listView = view.findViewById(R.id.recent_list);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
                    hidePopupWindow();
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.panel_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });

        view.findViewById(R.id.panel_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppHistory.clearHistory();
                listView.setAdapter(new RecentListAdapter());
            }
        });

        view.findViewById(R.id.panel_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                Handlers.executeVitualAction(context, Handlers.GLOBAL_ACTION_BACK);
            }
        });

        view.findViewById(R.id.panel_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                Handlers.executeVitualAction(context, Handlers.GLOBAL_ACTION_HOME);
            }
        });

        view.findViewById(R.id.panel_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                Handlers.executeVitualAction(context, Handlers.GLOBAL_ACTION_RECENTS);
            }
        });

        TabHost tabHost = view.findViewById(R.id.recent_tabs);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("recent").setContent(R.id.recent_list).setIndicator("最近使用"));
        tabHost.addTab(tabHost.newTabSpec("hide").setContent(R.id.recent_hide_list).setIndicator("隐藏的"));
        tabHost.setCurrentTab(0);

        listView.setAdapter(new RecentListAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            hidePopupWindow();
                        } catch (Exception ex) { }
                    }
                }, 200);
                startApp(context, parent.getItemAtPosition(position).toString());
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AppHistory.putBlackListHistory(parent.getItemAtPosition(position).toString());
                listView.setAdapter(new RecentListAdapter());
                return true;
            }
        });

        hideList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AppHistory.removeBlackListHistory(parent.getItemAtPosition(position).toString());
                hideList.setAdapter(new RecentListAdapter(AppHistory.getBlackListConfig()));
                return true;
            }
        });
        hideList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            hidePopupWindow();
                        } catch (Exception ex) { }
                    }
                }, 200);
                startApp(context, parent.getItemAtPosition(position).toString());
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("hide") && hideList.getAdapter() == null) {
                    hideList.setAdapter(new RecentListAdapter(AppHistory.getBlackListConfig()));
                } else if (tabId.equals("recent")) {
                    listView.setAdapter(new RecentListAdapter(AppHistory.getHistory()));
                }
            }
        });

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT; // minSize; //
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.windowAnimations = R.style.windowAnim;

        mWindowManager.addView(view, params);

        return view;
    }
}