package com.omarea.gesture.shell;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import com.omarea.gesture.util.GlobalState;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ScreenColor {
    private static Process exec;
    private static Thread thread;
    private static final Object threadRun = "";

    public static void updateBarColor() {
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    do {
                        try {
                            Thread.sleep(50);
                            long start = System.currentTimeMillis();
                            if (new ScreenColor().screenIsLightColor()) {
                                Log.d(">>>>", "变黑色");
                                GlobalState.iosBarColor = Color.BLACK;
                            } else {
                                Log.d(">>>>", "变白色");
                                GlobalState.iosBarColor = Color.WHITE;
                            }
                            if (GlobalState.updateBar != null) {
                                GlobalState.updateBar.run();
                            }
                            Log.d(">>>>", "time " + (System.currentTimeMillis() - start));
                        } catch (Exception ex) {
                        }
                        try {
                            synchronized (threadRun) {
                                threadRun.wait();
                            }
                        } catch (Exception ex) {
                            thread = null;
                            break;
                        }
                    } while (true);
                }
            });
            thread.start();
        } else {
            synchronized (threadRun) {
                threadRun.notify();
            }
        }
    }

    public boolean screenIsLightColor() {
        int pixel = getScreenBottomColor();
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);

        return (redValue > 180 && blueValue > 180 && greenValue > 180);
    }

    private boolean isWhiteTopColor(byte[] rawImage) {
        int r = 0, g = 0, b = 0, a = 0;
        int index = 12 + (540 * 4) ; // 前面12位不属于像素信息，跳过12位，并以屏幕分辨率位1080p取顶部中间那个像素的颜色（32位色，每个像素4byte）
        r = rawImage[index];
        g = rawImage[index + 1];
        b = rawImage[index + 2];
        a = rawImage[index + 3];
        if (r == -1) {
            r = 255;
        }
        if (g == -1) {
            g = 255;
        }
        if (b == -1) {
            b = 255;
        }
        return (r > 180 && b > 180 && g > 180);
    }

    private int getScreenBottomColor() {
        boolean usePng = false;
        byte[] bytes = getScreenCapBytes(usePng);
        if (usePng) {
           Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
           int y = bitmap.getHeight() - 15;
           int x = bitmap.getWidth() / 2 - 1;
           return bitmap.getPixel(x, y);
        } else {
            // 如果状态栏都是白色的，那这个界面肯定是白色啦
            if (isWhiteTopColor(bytes)) {
                return Color.WHITE;
            }

            int r = 0, g = 0, b = 0, a = 0;
            // int index = 12; // 1080 * (2340 - 15) + 540;
            int index = bytes.length - (540 * 4) - 5;// 后面5byte 不知道是什么，总之也不是像素信息
            r = bytes[index];
            g = bytes[index + 1];
            b = bytes[index + 2];
            a = bytes[index + 3];
            if (r == -1) {
                r = 255;
            }
            if (g == -1) {
                g = 255;
            }
            if (b == -1) {
                b = 255;
            }
            Log.d(">>>>", "raw:" + bytes.length);
            Log.d(">>>>", "rgba(" + r + "," + g + "," + b + "," + a + ")");
            return Color.argb(a, r, g, b);
        }
    }

    private static final byte[] endTag = "echo '----end tag----'\n".getBytes();

    /***
     *
     * @param usePng 是否编码为png格式
     * @return
     */
    private byte[] getScreenCapBytes(boolean usePng) {
        int cacheSize = 4096 * 100;
        byte[] tempBuffer = new byte[cacheSize];
        StringBuilder buffer = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(2160 * 3840 * 4); // 默认缓冲区大小为4K 32bit 的大小
        //LogUtils.i("获取屏幕图片bytes");

        try {
            if (exec == null) {
                exec = Runtime.getRuntime().exec("su");
            }

            OutputStream outputStream = exec.getOutputStream();
            if (usePng) {
                outputStream.write("screencap -p\n".getBytes());
            } else {
                outputStream.write("screencap\n".getBytes());
            }
            outputStream.write(endTag);
            outputStream.flush();
            try {
                final InputStream inputStream = exec.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                //清空缓存内容
                buffer.setLength(0);
                int count;
                while ((count = bufferedInputStream.read(tempBuffer)) > 0) {
                    if (count == 16) {
                        // "----end tag----\n" = 16byte
                        Log.d(">>>> read end tag", "count " + count);
                        break;
                    } else {
                        Log.d(">>>> read", "count " + count);
                        byteBuffer.put(tempBuffer, 0, count);
                    }
                }
            } catch (final IOException e) {
                exec.destroy();
                exec = null;
            }
        } catch (IOException e) {
            exec.destroy();
            exec = null;
        }
        byteBuffer.flip();
        byte[] out = new byte[byteBuffer.limit()];
        byteBuffer.get(out, 0, out.length);
        return out;
    }
}