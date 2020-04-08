package com.omarea.gesture.util;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class Recents {
    private final ArrayList<String> recents = new ArrayList<>();
    // TODO:关闭辅助服务时清理以下数据
    // 已经确保可以打开的应用
    public ArrayList<String> whiteList = new ArrayList<>();
    // 已经可以肯定不是可以打开的应用
    public ArrayList<String> blackList = new ArrayList<String>() {
    };

    public ArrayList<String> inputMethods = null;
    public ArrayList<String> launcherApps = null;
    private int index = -1;
    private String currentTop = "";

    public void clear() {
        synchronized (recents) {
            recents.clear();
            currentTop = "";
        }
    }

    public void addRecent(String packageName) {
        if (currentTop.equals(packageName)) {
            return;
        }

        synchronized (recents) {
            int searchResult = recents.indexOf(packageName);
            if (searchResult > -1) {
                recents.remove(searchResult);
            }
            // Log.d(">>>>", "add " + index + "  " + packageName);

            if (index > -1) {
                recents.add(index, packageName);
            } else {
                recents.add(packageName);
            }
            index = recents.indexOf(packageName);
            currentTop = packageName;

            /*
            StringBuilder packages = new StringBuilder("  ");
            for (String item:recents) {
                packages.append(item);
                packages.append(", ");
            }
            Log.d(">>>>", packages.toString());
            */
        }
    }

    void setRecents(ArrayList<String> items, Context context) {
        synchronized (recents) {
            /*
            if (recents.size() < 4) {
                recents.clear();
                for (String packageName : items) {
                    if (
                            whiteList.indexOf(packageName) > -1 ||
                            (blackList.indexOf(packageName) < 0 && ignoreApps.indexOf(packageName) < 0)
                    ) {
                        recents.add(packageName);
                    }
                }
                index = recents.indexOf(currentTop);
            } else {
                ArrayList<String> lostedItems = new ArrayList<>();
                for (String recent : recents) {
                    if (items.indexOf(recent) < 0) {
                        lostedItems.add(recent);
                    }
                }
                recents.removeAll(lostedItems);
                index = recents.indexOf(currentTop);
            }
            */

            ArrayList<String> lostedItems = new ArrayList<>();
            for (String recent : recents) {
                if (!recent.equals(Intent.CATEGORY_HOME) && items.indexOf(recent) < 0) {
                    lostedItems.add(recent);
                }
            }
            recents.removeAll(lostedItems);
            index = recents.indexOf(currentTop);
        }
    }

    public int getIndex(String packageName) {
        return recents.indexOf(packageName);
    }

    public void setIndex(int to) {
        if (index < recents.size()) {
            index = to;
        }
    }

    String getCurrent() {
        return currentTop;
    }

    String movePrevious(boolean switchToHome) {
        String previous;
        synchronized (recents) {
            if (index < recents.size() - 1) {
                index += 1;
                previous = recents.get(index);
            } else if (recents.size() > 0) {
                index = 0;
                previous = recents.get(0);
            } else {
                previous = null;
            }
        }
        if (Intent.CATEGORY_HOME.equals(previous) && !switchToHome && recents.size() > 1) {
            return movePrevious(switchToHome);
        }
        return previous;
    }

    String moveNext(boolean switchToHome) {
        String next;
        synchronized (recents) {
            if (index > 0) {
                index -= 1;
                next = recents.get(index);
            } else if (recents.size() > 0) {
                int size = recents.size();
                index = size - 1;
                next = recents.get(index);
            } else {
                next = null;
            }
        }
        if (Intent.CATEGORY_HOME.equals(next) && !switchToHome && recents.size() > 1) {
            return moveNext(switchToHome);
        }

        return next;
    }
}
