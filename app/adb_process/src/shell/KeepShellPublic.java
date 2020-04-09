package shell;

import java.util.List;

/**
 * Created by Hello on 2018/01/23.
 */
public class KeepShellPublic {
    private static KeepShell keepShell = null;

    //执行脚本
    public static String doCmdSync(String cmd) {
        if (keepShell == null) {
            keepShell = new KeepShell();
        }
        return keepShell.doCmdSync(cmd);
    }

    public static void tryExit() {
        if (keepShell != null) {
            keepShell.tryExit();
            keepShell = null;
        }
    }
}
