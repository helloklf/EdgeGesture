package com.omarea.gesture.shell;

import java.io.IOException;

public class ShellExecutor {
    private static Process getProcess(String run) throws IOException {
        return Runtime.getRuntime().exec(run);
    }

    public static Process getSuperUserRuntime() throws IOException {
        return getProcess("su");
    }

    public static Process getRuntime() throws IOException {
        return getProcess("sh");
    }
}
