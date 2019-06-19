package com.omarea.gesture;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;

public class RecentListAdapter extends BaseAdapter {
    private static LruCache<String, Drawable> iconCaches = new LruCache<>(30);

    private ArrayList<String> history = new ArrayList<>();
    private PackageManager packageManager = null;

    RecentListAdapter() {
        history.addAll(AppHistory.getHistory());
    }
    RecentListAdapter(ArrayList<String> items) {
        if(items!= null) {
            history.addAll(items);
        }
    }
    RecentListAdapter(String[] items) {
        if(items!= null) {
            history.addAll(Arrays.asList(items));
        }
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Object getItem(int position) {
        return history.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = View.inflate(parent.getContext(), R.layout.layout_rencent_app, null);
        }

        String packageName = getItem(position).toString();
        try {
            if (packageManager == null) {
                packageManager = parent.getContext().getApplicationContext().getPackageManager();
            }
            Drawable iconCache = iconCaches.get(packageName);
            if (iconCache == null) {
                iconCache = packageManager.getApplicationIcon(packageName);
                iconCaches.put(packageName, iconCache);
            }
            ((ImageView)(view.findViewById(R.id.recent_icon))).setImageDrawable(iconCache);

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = packageManager.getApplicationLabel(applicationInfo).toString();
            ((TextView)(view.findViewById(R.id.recent_name))).setText(appName);
        } catch (Exception ex) {
            ((TextView)(view.findViewById(R.id.recent_name))).setText(packageName);
        }

        return view;
    }
}
