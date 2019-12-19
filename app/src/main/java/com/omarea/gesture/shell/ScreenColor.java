package com.omarea.gesture.shell;

import android.graphics.Color;
import android.util.Log;

import com.omarea.gesture.util.GlobalState;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ScreenColor {
    private static final Object threadRun = "";
    private static final Object screencapRead = "";
    private static Process exec;
    private static Thread thread;
    private static long updateTime = 0;
    private static boolean hasNext = false;

    static class RunThread extends Thread{
        @Override
        public void run() {
            do {
                try {
                    updateTime = System.currentTimeMillis();
                    long start = System.currentTimeMillis();
                    updateTime = -1;
                    writeCommand();

                    try {
                        synchronized (screencapRead) {
                            screencapRead.wait();
                        }
                    } catch (Exception ex) {
                        break;
                    }
                    Log.d(">>>>", "time " + (System.currentTimeMillis() - start));
                } catch (Exception ignored) {
                }
                try {
                    synchronized (threadRun) {
                        if (ScreenColor.hasNext) {
                            threadRun.wait(1000);
                            ScreenColor.hasNext = false;
                        } else {
                            threadRun.wait();
                        }
                    }
                } catch (Exception ex) {
                    break;
                }
            } while (true);
            thread = null;
        }

        /***
         *
         * @return
         */
        private void writeCommand() {
            try {
                if (exec == null) {
                    exec = Runtime.getRuntime().exec("su");
                    new ReadThread().start();
                }

                OutputStream outputStream = exec.getOutputStream();
                outputStream.write("screencap 2> /dev/null\n".getBytes());

                outputStream.flush();

            } catch (IOException e) {
                Log.d(">>>>", "frame IOException");
                exec.destroy();
                exec = null;
            }
        }
    }

    public static void updateBarColor(boolean hasNext) {
        // 如果距离上次执行已经超过6秒，认位颜色获取进程已经崩溃，将其结束重启
        if (updateTime > -1 && System.currentTimeMillis() - updateTime > 6000) {
            try {
                if (exec != null) {
                    exec.destroy();
                    exec = null;
                }
            } catch (Exception ignored) {
            }
            try {
                if (thread != null) {
                    thread.interrupt();
                    thread = null;
                }
            } catch (Exception ignored) {
            }
        }
        ScreenColor.hasNext = hasNext;

        if (thread == null) {
            thread = new RunThread();
            thread.start();
        } else {
            synchronized (threadRun) {
                threadRun.notify();
            }
        }
    }

    static class ReadThread extends Thread {
        private BufferedInputStream bufferedInputStream;
        ReadThread(InputStream inputStream) {
            bufferedInputStream = new BufferedInputStream(inputStream);
        }
        ReadThread() {
        }

        private void setBarColor(byte[] bytes) {
            int pixel = getScreenBottomColor(bytes);
            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);

            Log.d(">>>>", "rgb(" + r + "," + g + "," + b + ")");

            if ((r > 180 && g > 180 && b > 180)) {
                Log.d(">>>>", "变黑色");
                GlobalState.iosBarColor = Color.BLACK;
            } else {
                Log.d(">>>>", "变白色");
                GlobalState.iosBarColor = Color.WHITE;
            }
            if (GlobalState.updateBar != null) {
                GlobalState.updateBar.run();
            }
            // Log.d(">>>>", "time " + (System.currentTimeMillis() - start));

            try {
                synchronized (screencapRead) {
                    screencapRead.notify();
                }
            } catch (Exception ex) {
            }

            updateTime = -1;
            // return (r > 180 && g > 180 && b > 180);
        }

        private boolean isWhiteTopColor(byte[] rawImage) {
            int r = 0, g = 0, b = 0, a = 0;
            int index = 12 + (540 * 4); // 前面12位不属于像素信息，跳过12位，并以屏幕分辨率位1080p取顶部中间那个像素的颜色（32位色，每个像素4byte）
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

        private int getScreenBottomColor(byte[] bytes) {
            // 如果状态栏都是白色的，那这个界面肯定是白色啦
            if (isWhiteTopColor(bytes)) {
                return Color.WHITE;
            }

            int r = 0, g = 0, b = 0, a = 0;
            // int index = 12; // 1080 * (2340 - 15) + 540;
            int index = bytes.length - 8;// 后面4byte 不知道是什么，总之也不是像素信息
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
            Log.d(">>>>", "raw rgba(" + r + "," + g + "," + b + "," + a + ")");
            return Color.argb(a, r, g, b);
        }

        @Override
        public void run() {
            int cacheSize = 1024 * 1024 * 4; // 4M
            byte[] tempBuffer = new byte[cacheSize];
            int size = GlobalState.displayHeight * GlobalState.displayWidth * 4 + (12 + 4); // 截图大小为 分辨率 * 32bit(4Byte) + 头尾(16Byte)
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            //LogUtils.i("获取屏幕图片bytes");

            try {
                final InputStream inputStream = exec.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                int count;
                int totalCount = 0;
                while ((count = bufferedInputStream.read(tempBuffer)) > 0) {
                    totalCount += count;
                    int remaining = byteBuffer.remaining();
                    if (count > remaining) {
                        Log.e(">>>>", "" + totalCount);
                        // exec.destroy();
                        byteBuffer.put(tempBuffer, 0, remaining);

                        byteBuffer.flip();
                        byte[] out = new byte[byteBuffer.limit()];
                        byteBuffer.get(out, 0, out.length);
                        setBarColor(out);

                        byteBuffer.clear();
                        byteBuffer.put(tempBuffer, remaining, tempBuffer.length - remaining);
                        totalCount = 0;
                    } else {
                        byteBuffer.put(tempBuffer, 0, count);
                    }

                    if (totalCount == size) {
                        // 读满缓冲区，则认为已经截完了一张图片
                        byteBuffer.flip();
                        byte[] out = new byte[byteBuffer.limit()];
                        byteBuffer.get(out, 0, out.length);
                        totalCount = 0;
                        byteBuffer.clear();
                        setBarColor(out);
                    }
                }
                Log.d(">>>>", "frame end");
            } catch (final IOException e) {
                Log.d(">>>>", "frame IOException");
            }
        }
    }
}