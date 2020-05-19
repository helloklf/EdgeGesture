package com.omarea.gesture.remote;

import android.os.Build;
import android.util.Log;

import com.omarea.gesture.util.GlobalState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class RemoteAPI {
    private final static Object networkWaitLock = new Object();
    private static String host = "http://localhost:8906/";

    public static boolean isOnline() {
        String result = loadContent("version");
        return result != null && !result.isEmpty();
    }

    public static String[] getRecents() {
        return loadContent((Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ? "recent-9" : "recent-10").split("\n");
    }

    public static int getBarAutoColor(boolean delayScreenCap) {
        // TODO:改为可配置而非自动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String isLightColor = loadContent("nav-light-color");
            if (isLightColor != null && isLightColor.equals("true")) {
                return 0xFF000000;
            }
        }
        if (delayScreenCap) {
            // 延缓截图，以免在动画播放期间过早的截图导致颜色取到的还是黑色背景
            try {
                Thread.sleep(300);
            } catch (Exception ignored) {
            }
        }

        String colorStr = loadContent("bar-color?" + GlobalState.displayWidth + "x" + GlobalState.displayHeight);

        if (!(colorStr == null || colorStr.isEmpty())) {
            try {
                return Integer.parseInt(colorStr);
            } catch (Exception ignored) {
            }
        }
        return Integer.MIN_VALUE;
    }

    private static String loadContent(final String api) {
        final String[] result = {""};
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (networkWaitLock) {
                    try {
                        URL url = new URL(host + api);
                        result[0] = readResponse(url.openConnection());
                    } catch (Exception ignored) {
                    } finally {
                        networkWaitLock.notify();
                    }
                }
            }
        }).start();
        try {
            synchronized (networkWaitLock) {
                networkWaitLock.wait(5000);
            }
        } catch (Exception ignored) {
        }
        return result[0];
    }

    private static String readResponse(URLConnection connection) {
        try {
            connection.setConnectTimeout(500);
            connection.setReadTimeout(3000);

            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                } else {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
            }
            return stringBuffer.toString().trim();
        } catch (IOException ignored) {
        }
        return null;
    }

    public static void fixDelay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadContent("fix-delay");
            }
        }).start();
    }

    public static void startActivity(final String params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder paramsBuilder = new StringBuilder("am start -n com.omarea.gesture/.AppSwitchActivity");
                    if (params != null) {
                        paramsBuilder.append(" ");
                        paramsBuilder.append(params);
                    }
                    paramsBuilder.append(" --activity-no-animation --activity-no-history --activity-exclude-from-recents --activity-clear-top --activity-clear-task");
                    loadContent("shell?" + URLEncoder.encode(paramsBuilder.toString(), "UTF-8"));
                } catch (Exception ignored) {
                }
            }
        }).start();
    }
}
