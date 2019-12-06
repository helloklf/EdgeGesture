package com.omarea.gesture;

import java.util.ArrayList;

public class Recents {
    private static final ArrayList<String> recents = new ArrayList<>();
    private static int index = -1;
    private static final int sizeLimit = 60;
    private static String currentTop = "";

    static void addRecent(String packageName) {
        synchronized (recents) {
            int searchResult = recents.indexOf(packageName);
            if (searchResult > -1) {
                recents.remove(searchResult);
            }
            if (recents.size() >= sizeLimit) {
                recents.remove(0);
                if (index >= recents.size()) {
                    index = recents.size() - 1;
                }
            }

            if (index > -1) {
                recents.add(index, packageName);
            } else {
                recents.add(packageName);
            }
            index = recents.indexOf(packageName);
            currentTop = packageName;
        }
    }

    static void setRecents(ArrayList<String> items) {
        synchronized (recents) {
            for (String recent:recents) {
                if (items.indexOf(recent) < 0) {
                    recents.remove(recent);
                }
            }
            index = recents.indexOf(currentTop);
        }
    }

    public static int getIndex(String packageName) {
        return recents.indexOf(packageName);
    }

    public static void setIndex(int to) {
        if (index < recents.size()) {
            index = to;
        }
    }

    static String movePrevious() {
        synchronized (recents) {
            if (index > 0) {
                index -= 1;
                return recents.get(index);
            } else if (recents.size() > 0) {
                int size = recents.size();
                index = size - 1;
                return recents.get(index);
            } else {
                return null;
            }
        }
    }

    static String moveNext() {
        synchronized (recents) {
            if (index < recents.size() - 1) {
                index += 1;
                return recents.get(index);
            } else if (recents.size() > 0) {
                index = 0;
                return recents.get(0);
            } else {
                return null;
            }
        }
    }
}
