package com.omarea.gesture.remote;

import android.os.Build;
import android.util.Log;

import com.omarea.gesture.util.GlobalState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class RemoteAPI {
    private static String host = "http://localhost:8906/";

    public static boolean isOnline() {
        String result = loadContent("version");
        return result != null && !result.isEmpty();
    }

    public static String[] getRecents() {
        return loadContent((Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ? "recent-9" : "recent-10").split("\n");
    }

    public static int getBarAutoColor() {
        // TODO:改为可配置而非自动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String isLightColor = loadContent("nav-light-color");
            if (isLightColor != null && isLightColor.equals("true")) {
                Log.d("getBarAutoColor", "FastWhite");
                return 0xFF000000;
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


    private static String loadContent(String api) {
        try {
            URL url = new URL(host + api);
            return readResponse(url.openConnection());
        } catch (Exception ex) {
            return "";
        }
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
}
