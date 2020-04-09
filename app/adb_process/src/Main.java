import shell.KeepShellPublic;
import shell.RemoteAPI;
import shell.ScreenColor;
import shell.ShellExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static final Object lock = new Object();

    public static void main(String[] args) {
        try {
            ScreenColor.updateBarColor(false);
            System.out.println(KeepShellPublic.doCmdSync("sh"));
            // System.out.println(KeepShellPublic.doCmdSync("screencap > /sdcard/screen"));
        } catch (Exception ex) {
            System.out.println("错误！" + ex.getMessage());
        }
        System.out.println("Gesture ADB Process!");

        RemoteAPI remoteAPI = new RemoteAPI();
        remoteAPI.start();

        try {
            System.out.println("Gesture ADB Process Keep Alive!");
            remoteAPI.wait();
        } catch (Exception ignored) {
        }
    }
}
