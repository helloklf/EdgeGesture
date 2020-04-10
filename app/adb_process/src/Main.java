import shell.KeepShellPublic;
import shell.RemoteAPI;
import shell.ScreenColor;

public class Main {
    static final Object lock = new Object();

    public static void main(String[] args) {
        try {
            new ScreenColor().autoBarColor();
            // System.out.println(KeepShellPublic.doCmdSync("dumpsys activity r | grep mActivityComponent | cut -f2 -d '=' | cut -f1 -d '/'"));
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
