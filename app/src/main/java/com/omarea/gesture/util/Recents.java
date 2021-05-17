package com.omarea.gesture.util;

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

            // Intent.CATEGORY_HOME 代步桌面应用，回到桌面时，桌面应该永远应用的后面放
            // 因此，在桌面上向后退永远是打开桌面前的上一个应用，而不是上上个应用
            if (searchResult > -1 && !Intent.CATEGORY_HOME.equals(packageName)) {
                recents.add(index, packageName);
            } else {
                int indexCurrent = recents.indexOf(currentTop);
                if (indexCurrent > -1 && indexCurrent + 1 < recents.size()) {
                    recents.add(indexCurrent + 1, packageName);
                } else {
                    recents.add(packageName);
                }
            }

            index = recents.indexOf(packageName);
            currentTop = packageName;

            StringBuilder packages = new StringBuilder();
            for (String item : recents) {
                packages.append(item);
                packages.append(", ");
            }
        }
    }

    void setRecents(ArrayList<String> items) {
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

    public boolean notEmpty() {
        return this.recents.size() > 1;
    }

    String getCurrent() {
        return currentTop;
    }

    String moveNext() {
        String packageName;
        synchronized (recents) {
            if (index < recents.size() - 1) {
                index += 1;
                packageName = recents.get(index);
            } else if (recents.size() > 0) {
                index = 0;
                packageName = recents.get(0);
            } else {
                packageName = null;
            }
        }
        if (Intent.CATEGORY_HOME.equals(packageName) && recents.size() > 1) {
            return moveNext();
        }
        return packageName;
    }

    String movePrevious() {
        String packageName;
        synchronized (recents) {
            if (index > 0) {
                index -= 1;
                packageName = recents.get(index);
            } else if (recents.size() > 0) {
                int size = recents.size();
                index = size - 1;
                packageName = recents.get(index);
            } else {
                packageName = null;
            }
        }
        if (Intent.CATEGORY_HOME.equals(packageName) && recents.size() > 1) {
            return movePrevious();
        }

        return packageName;
    }
}
