package com.omarea.gesture;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.shell.KeepShellPublic;
import com.omarea.gesture.util.GlobalState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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

    public String extract(Context context) {
        if (extractFile(context, "adb_process.dex") && extractFile(context, "up.sh")) {
            File cacheDir = context.getExternalCacheDir();
            return cacheDir.getAbsolutePath() + "/up.sh";
        }
        return null;
    }

    // 尝试连接AdbProcess，或使用root权限激活AdbProcess并连接
    public boolean updateAdbProcessState(Context context, boolean useRootStartService) {
        SharedPreferences config = context.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        // 检测外部程序运行状态或使用root权限主动激活外部进程
        boolean rootMode = config.getBoolean(SpfConfig.ROOT, SpfConfig.ROOT_DEFAULT);
        GlobalState.enhancedMode = RemoteAPI.isOnline();
        if (useRootStartService && rootMode && !GlobalState.enhancedMode) {
            String file = extract(context);
            if (file != null) {
                String shell = "sh " + file + " >/dev/null 2>&1 &";
                KeepShellPublic.doCmdSync(shell);
                GlobalState.enhancedMode = RemoteAPI.isOnline();
            }
        }
        return GlobalState.enhancedMode;
    }
}
