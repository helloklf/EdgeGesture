package com.omarea.gesture.daemon;

import android.annotation.SuppressLint;
import android.content.Context;

import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.daemon.RemoteAPI;
import com.omarea.gesture.shell.KeepShellPublic;
import com.omarea.gesture.util.GlobalState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class AdbProcessExtractor {

    // example: setADBReadable(context, "/data/data/com.omarea.vtools/files/scene-daemon")
    @SuppressLint("SetWorldReadable")
    private void setADBReadable(Context context, String absPath) {
        String packageName = context.getPackageName();
        File file = new File(absPath);
        if (file.exists() && absPath.contains(packageName)) {
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, true);
            if (!absPath.endsWith(packageName)) {
                String parent = file.getParent();
                if (parent != null && !parent.isEmpty()) {
                    setADBReadable(context, parent);
                }
            }
        }
    }

    private boolean extractFile(Context context, String file) {
        try {
            InputStream inputStream = context.getAssets().open(file);
            File outFile = new File(getOutDir(context, file));
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[10240];
            while (true) {
                int len = inputStream.read(buffer);
                if (len > 0) {
                    fileOutputStream.write(buffer, 0, len);
                } else {
                    break;
                }
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            setADBReadable(context, outFile.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] readRawResBytes(Context context, int rawResId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(rawResId);
            return readBytes(inputStream);
        } catch (Exception ex) {
            return new byte[0];
        }
    }
    private byte[] readAssetsBytes(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            return readBytes(inputStream);
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    private byte[] readBytes(InputStream inputStream) {
        try {
            // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, i);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    private String getOutDir(Context context, String fileName) {
        File cacheDir = context.getFilesDir();
        cacheDir.mkdirs();

        return (cacheDir.getAbsolutePath() + "/" + fileName);
    }

    private boolean extractShellScript(Context context, String outName) {
        try {
            byte[] content = new String(
                readRawResBytes(context, R.raw.up), Charset.defaultCharset()
            ).replaceAll("\r\n", "\n").replaceAll("\r", "\n").getBytes();

            File outFile = new File(getOutDir(context, outName));
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            fileOutputStream.write(content);

            fileOutputStream.flush();
            fileOutputStream.close();

            setADBReadable(context, outFile.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extract(Context context) {
        if (extractFile(context, "adb_process.dex") && extractShellScript(context, "up.sh")) {
            return getOutDir(context, "up.sh");
        }
        return null;
    }

    // 尝试连接AdbProcess，或使用root权限激活AdbProcess并连接
    public boolean updateAdbProcessState(Context context, boolean useRootStartService) {
        // 检测外部程序运行状态或使用root权限主动激活外部进程
        boolean rootMode = Gesture.config.getBoolean(SpfConfig.ROOT, SpfConfig.ROOT_DEFAULT);
        GlobalState.enhancedMode = RemoteAPI.isOnline();
        if (useRootStartService && rootMode && !GlobalState.enhancedMode) {
            String file = extract(context);
            if (file != null) {
                String shell = "sh " + file + " >/dev/null 2>&1 &";
                KeepShellPublic.doCmdSync(shell);
                Gesture.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GlobalState.enhancedMode = RemoteAPI.isOnline();
                    }
                }, 5000);
            }
        }
        return GlobalState.enhancedMode;
    }
}
