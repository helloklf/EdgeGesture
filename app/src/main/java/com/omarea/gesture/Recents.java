package com.omarea.gesture;

import java.util.ArrayList;

public class Recents {
    private static final ArrayList<String> recents = new ArrayList<>();
    private static int index = -1;
    private static final int sizeLimit = 10;

    public static void addRecent(String packageName) {
        synchronized (recents) {
            int searchResult = recents.indexOf(packageName);
            // if (searchResult > -1 && Math.abs(searchResult - index) == 1) {
            if (searchResult > -1) {
                index = searchResult;
            } else {
                if (recents.size() >= sizeLimit) {
                    recents.remove(0);
                }

                recents.add(packageName);
                index = recents.size() - 1;
            }
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

    public static String movePrevious() {
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

    public static String moveNext() {
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
