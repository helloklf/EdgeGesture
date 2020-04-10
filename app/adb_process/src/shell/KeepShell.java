package shell;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hello on 2018/01/23.
 */
public class KeepShell {
    private Process p = null;
    private OutputStream out = null;
    private BufferedReader reader = null;
    private ReentrantLock mLock = new ReentrantLock();
    private long LOCK_TIMEOUT = 10000L;
    private long enterLockTime = 0L;
    private byte[] br = "\n\n".getBytes(Charset.defaultCharset());

    public KeepShell() {
    }

    //尝试退出命令行程序
    public void tryExit() {
        try {
            if (out != null)
                out.close();
            if (reader != null)
                reader.close();
        } catch (Exception ex) {
        }
        try {
            p.destroy();
        } catch (Exception ex) {
        }
        enterLockTime = 0L;
        out = null;
        reader = null;
        p = null;
    }

    private void getRuntimeShell() {
        if (p != null)
            return;
        Thread getSu = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mLock.lockInterruptibly();
                    enterLockTime = System.currentTimeMillis();
                    p = ShellExecutor.getRuntime();
                    out = p.getOutputStream();
                    reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                while (true) {
                                    errorReader.readLine();
                                }
                            } catch (Exception ex) {
                                System.out.println("Gesture ADB Error " + ex.getMessage());
                            }
                        }
                    }).start();
                } catch (Exception ex) {
                } finally {
                    enterLockTime = 0L;
                    mLock.unlock();
                }
            }
        });
        getSu.start();
        try {
            getSu.join(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (p == null && getSu.getState() != Thread.State.TERMINATED) {
            enterLockTime = 0L;
            getSu.interrupt();
        }
    }

    //执行脚本
    public String doCmdSync(String cmd) {
        if (mLock.isLocked() && enterLockTime > 0 && System.currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
            tryExit();
        }
        CharSequence uuid = UUID.randomUUID().toString().subSequence(0, 8);
        getRuntimeShell();
        if (out != null) {
            String startTag = "--start--" + uuid + "--";
            String endTag = "--end--" + uuid + "--";
            // Log.e("shell-lock", cmd)
            try {
                try {
                    mLock.lockInterruptibly();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "error";
                }

                OutputStream out = p.getOutputStream();
                if (out != null) {
                    try {
                        out.write(br);
                        out.write(("echo '" + startTag + "'").getBytes(Charset.defaultCharset()));
                        out.write(br);
                        out.write(cmd.getBytes(Charset.defaultCharset()));
                        out.write(br);
                        out.write("echo \"\"".getBytes(Charset.defaultCharset()));
                        out.write(br);
                        out.write(("echo '" + endTag + "'").getBytes(Charset.defaultCharset()));
                        out.write(br);
                        out.flush();
                    } catch (Exception ex) {
                    }

                    StringBuilder results = new StringBuilder();
                    boolean unstart = true;
                    while (reader != null) {
                        String line = reader.readLine();
                        if (line == null || line.contains("--end--")) {
                            break;
                        } else if (line.equals(startTag)) {
                            unstart = false;
                        } else if (!unstart) {
                            results.append(line);
                            results.append("\n");
                        }
                    }
                    // Log.e("shell-unlock", cmd)
                    // Log.d("Shell", cmd.toString() + "\n" + "Result:"+results.toString().trim())
                    return results.toString().trim();
                } else {
                    return "error";
                }
            } catch (Exception e) {
                tryExit();
                return "error";
            } finally {
                enterLockTime = 0L;
                mLock.unlock();
            }
        } else {
            tryExit();
            return "error";
        }
    }
}
