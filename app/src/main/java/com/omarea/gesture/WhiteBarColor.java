package com.omarea.gesture;

import android.util.Log;

import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.util.GlobalState;

public class WhiteBarColor {
    private static final Object threadRun = "";
    private static ScreenCapThread thread;
    private static boolean hasNext = false;
    private static boolean notifyed = false; // 是否已经notify过，并且还未进入wait清除notifyed状态，避免多次notify进入队列

    public static void updateBarColor(boolean dual) {
        hasNext = dual;

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
                int color = RemoteAPI.getBarAutoColor();
                if (color != Integer.MIN_VALUE) {
                    Log.d("GestureRemote", "Color is " + color);
                    GlobalState.iosBarColor = color;

                    if (GlobalState.updateBar != null) {
                        GlobalState.updateBar.run();
                    }
                }
                notifyed = false;
                try {
                    synchronized (threadRun) {
                        if (hasNext) {
                            threadRun.wait(600);
                            hasNext = false;
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
