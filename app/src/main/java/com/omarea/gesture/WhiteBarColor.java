package com.omarea.gesture;

import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.util.GlobalState;

public class WhiteBarColor {
    private static final Object threadRun = "";
    private static ScreenCapThread thread;
    private static int nextTimes = 0;
    private static boolean notifyed = false; // 是否已经notify过，并且还未进入wait清除notifyed状态，避免多次notify进入队列

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
                if (!notifyed && (thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TIMED_WAITING)) {
                    threadRun.notify();
                    notifyed = true;
                }
            }
        } else {
            thread = new ScreenCapThread();
            thread.start();
        }
    }

    public static void updateDisplaySize() {

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
                notifyed = false;
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
