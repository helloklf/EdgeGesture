import shell.RemoteAPI;

public class Main {
    static final Object lock = new Object();

    public static void main(String[] args) {
        /*
        try {
            new ScreenColor().autoBarColor();
            // System.out.println(KeepShellPublic.doCmdSync("dumpsys activity r | grep mActivityComponent | cut -f2 -d '=' | cut -f1 -d '/'"));
            // System.out.println(KeepShellPublic.doCmdSync("screencap > /sdcard/screen"));
        } catch (Exception ex) {
            System.out.println("错误！" + ex.getMessage());
        }

        if (KeepShellPublic.doCmdSync("pm list packages com.omarea.gesture").contains("com.omarea.gesture")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        wait(2000);
                    } catch (Exception ignored) {
                    }
                    KeepShellPublic.doCmdSync("am broadcast -a com.omarea.gesture.ConfigChanged");
                }
            }).start();
        }
        */

        // System.out.println("Gesture ADB Process!");

        RemoteAPI remoteAPI = new RemoteAPI();
        remoteAPI.start();
    }
}
