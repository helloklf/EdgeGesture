package com.omarea.gesture;

import com.omarea.gesture.daemon.RemoteAPI;
import com.omarea.gesture.util.GlobalState;

public class WhiteBarColor {
    private static final Object threadRun = "";
    private static ScreenCapThread thread;
    private static int nextTimes = 0;
    private static boolean updating = false; // 是否正在检测颜色

    public static void updateBarColorSingle() {
        updateBarColor();
    }

    static void updateBarColorMultiple() {
        nextTimes = 2;
        updateBarColor();
    }

    private static void updateBarColor() {
        if (thread != null && thread.isAlive() && !thread.isInterrupted()) {
            synchronized (threadRun) {
                if (!updating && (thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TIMED_WAITING)) {
                    threadRun.notify();
                    updating = true;
                }
            }
        } else {
            thread = new ScreenCapThread();
            thread.start();
        }
    }

    static class ScreenCapThread extends Thread {
        @Override
        public void run() {
            do {
                int color = RemoteAPI.getBarAutoColor(nextTimes > 0);
                if (color != Integer.MIN_VALUE) {
                    GlobalState.iosBarColor = color;

                    if (GlobalState.updateBar != null) {
                        GlobalState.updateBar.run();
                    }
                }
                updating = false;
                try {
                    synchronized (threadRun) {
                        if (nextTimes > 0) {
                            threadRun.wait(600);
                            nextTimes -= 1;
                        } else {
                            threadRun.wait();
                        }
                    }
                } catch (Exception ex) {
                    break;
                }
            }
            while (true);
        }
    }
}
