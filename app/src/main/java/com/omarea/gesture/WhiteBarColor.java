package com.omarea.gesture;

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
                    // Log.d("GestureRemote", "Color is " + color);
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
                            // 延缓截图，以免在动画播放期间过早的截图导致颜色取到的还是黑色背景
                            if (hasNext) {
                                Thread.sleep(300);
                            }
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
