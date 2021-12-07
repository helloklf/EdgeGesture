package com.omarea.gesture.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class SystemProperty {
    String get(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            p.destroy();
        } catch (Exception ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
        }
        return line;
    }

    public boolean isMIUI12() {
        try {
            // 反射调用私有接口，被Google封杀了
            // Object result = Class.forName("android.os.Systemproperties").getMethod("get").invoke(null, "ro.miui.ui.version.name", "");
            // return "V12".equals(result.toString());
            String version = get("ro.miui.ui.version.name");
            return Objects.equals(version, "V12") || Objects.equals(version, "V12.5");
        } catch (Exception ex) {
            return false;
        }
    }
}
