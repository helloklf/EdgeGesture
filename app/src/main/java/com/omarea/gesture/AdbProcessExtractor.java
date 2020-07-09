package com.omarea.gesture;

import android.content.Context;

import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.shell.KeepShellPublic;
import com.omarea.gesture.util.GlobalState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AdbProcessExtractor {
    private boolean extractFile(Context context, String file) {
        try {
            File cacheDir = context.getExternalCacheDir();
            cacheDir.mkdirs();

            cacheDir.setExecutable(true);
            cacheDir.setWritable(true);

            InputStream inputStream = context.getAssets().open(file);
            File outFile = new File((cacheDir.getAbsolutePath() + "/" + file));
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            byte[] datas = new byte[10240];
            while (true) {
                int len = inputStream.read(datas);
                if (len > 0) {
                    fileOutputStream.write(datas, 0, len);
                } else {
                    break;
                }
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            outFile.setExecutable(true, false);
            outFile.setReadable(true, false);
            outFile.setWritable(true, false);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean extractShellScript(Context context, String file) {
        try {
            File cacheDir = context.getExternalCacheDir();
            cacheDir.mkdirs();
            cacheDir.setExecutable(true);
            cacheDir.setWritable(true);

            InputStream inputStream = context.getAssets().open(file);
            File outFile = new File((cacheDir.getAbsolutePath() + "/" + file));
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            do {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                } else {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
            } while (true);
            fileOutputStream.write(stringBuffer.toString().replaceAll("\r\n", "\n").replaceAll("\r", "\n").getBytes());

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            outFile.setExecutable(true, false);
            outFile.setReadable(true, false);
            outFile.setWritable(true, false);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extract(Context context) {
        if (extractFile(context, "adb_process.dex") && extractShellScript(context, "up.sh")) {
            File cacheDir = context.getExternalCacheDir();
            return cacheDir.getAbsolutePath() + "/up.sh";
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
