package com.omarea.gesture;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;

public class AppHistory {
    private static int LIMIT_COUNT = 50;
    private static SharedPreferences config;
    private static final ArrayList<String> history = new ArrayList<>();
    private static Context mContext;
    private static HashMap<String, Intent> intentHashMap = new HashMap<>();
    private static final ArrayList<String> blackList = new ArrayList<String>() {{
        add("android");
        add("com.android.systemui");
        add("com.omarea.gesture");
        add("com.miui.aod");
    }};

    public static void initConfig(Context context) {
        mContext = context;
        config = context.getSharedPreferences("blacklist", Context.MODE_PRIVATE);
    }

    public static Intent getAppIntent(String packageName) {
        if (intentHashMap.containsKey(packageName)) {
            return intentHashMap.get(packageName);
        }

        if (mContext != null) {
            try {
                PackageManager pm = mContext.getPackageManager();
                Intent appIntent = pm.getLaunchIntentForPackage(packageName);
                if (appIntent == null) {
                    putBlackList(packageName);
                    return null;
                } else {
                    intentHashMap.put(packageName, appIntent);
                    return appIntent;
                }
            } catch (Exception ex) {
                putBlackList(packageName);
                return null;
            }
        }
        return null;
    }

    public static void putHistory(String packageName) {
        synchronized (history) {
            for (int i = 0; i < blackList.size(); i++) {
                if (blackList.get(i).equals(packageName)) {
                    return;
                }
            }

            if (config != null && config.contains(packageName)) {
                return;
            }
            if (getAppIntent(packageName) != null) {
                for (int i = 0; i < history.size(); i++) {
                    if (history.get(i).equals(packageName)) {
                        history.remove(i);
                        break;
                    }
                }

                history.add(packageName);

                if (history.size() > LIMIT_COUNT) {
                    history.remove(0);
                }
            }
        }
    }

    public static void clearHistory() {
        synchronized (history) {
            history.clear();
        }
    }

    public static ArrayList<String> getHistory() {
        return history;
    }

    public static ArrayList<String> getBlackList() {
        return blackList;
    }

    public static String[] getBlackListConfig() {
        String[] empty = new String[]{};
        return (config != null) ? (config.getAll().keySet().toArray(empty)) : empty;
    }

    public static void putBlackList(String packageName) {
        synchronized (history) {
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).equals(packageName)) {
                    history.remove(i);
                    break;
                }
            }
        }
        for (int i = 0; i < blackList.size(); i++) {
            if (blackList.get(i).equals(packageName)) {
                return;
            }
        }
        blackList.add(packageName);
    }

    public static void putBlackListHistory(String packageName) {
        synchronized (history) {
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).equals(packageName)) {
                    history.remove(i);
                    break;
                }
            }
        }
        if (!config.contains(packageName)) {
            config.edit().putString(packageName, "").apply();
        }
    }

    public static void removeBlackListHistory(String packageName) {
        if (config.contains(packageName)) {
            config.edit().remove(packageName).apply();
        }
    }
}
