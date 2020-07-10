package com.omarea.gesture.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

class SystemProperty {
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

    boolean isMiui12() {
        try {
            // 反射调用私有接口，被Google封杀了
            // Object result = Class.forName("android.os.Systemproperties").getMethod("get").invoke(null, "ro.miui.ui.version.name", "");
            // return "V12".equals(result.toString());
            return Objects.equals(get("ro.miui.ui.version.name"), "V12");
        } catch (Exception ex) {
            return false;
        }
    }
}
