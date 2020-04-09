package shell;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;

public class ScreenColor {
    private BufferedInputStream bufferedInputStream;

    // 像素采集
    private static class PixelGather {
        private int frameSize;
        private int position;
        private static final int fileHeader = 12; // 文件头部长度
        private static final int fileFooter = 4; // FIXME:文件脚部长度（Android 8.1 即SDK27 以前没有这4Byte！）
        private ByteBuffer buffer; // 真实的缓冲区
        // 除了头部和脚部，则每4Byte(RGBA)代表一个像素，例如左上角第一个像素就是 bytes[16] ~ bytes[19]

        // 用于取样的像素在帧数据中的位置
        // 采样的像素点数量建议设为 单数，因为最终会对比 暗色/亮色 点的数量
        private int[] samplingPixel = {
                fileHeader + (GlobalState.displayWidth / 4 * 4), // y: 0, x: 0.25
                fileHeader + (GlobalState.displayWidth / 4 * 3 * 4), // y: 0, x: 0.75
                fileHeader + (((GlobalState.displayWidth * GlobalState.displayHeight) - (GlobalState.displayWidth / 4)) * 4), // y: 1, x : 0.25
                fileHeader + (((GlobalState.displayWidth * GlobalState.displayHeight) - (GlobalState.displayWidth / 2)) * 4), // y: 1, x : 0.5
                fileHeader + (((GlobalState.displayWidth * GlobalState.displayHeight) - (GlobalState.displayWidth / 4 * 3)) * 4) // y: 1, x : 0.75
        };

        private PixelGather(int frameSize) {
            this.frameSize = frameSize;
            // 32位的像素，每个占用 4 字节
            this.buffer = ByteBuffer.allocate(samplingPixel.length * 4);
        }

        static PixelGather frameBuffer(int height, int width) {
            return new PixelGather(fileHeader + (height * width * 4) + fileFooter);
        }

        void put(byte[] bytes, int offset, int count) {
            if (position + count > frameSize) {
                throw new IndexOutOfBoundsException("数据写入量超出缓冲区可用空间");
            }
            if (count != 0) {
                int rangeLeft = position;
                int rangeRight = position + count;

                for (int pixel : samplingPixel) {
                    sampling(bytes, offset, count, pixel);
                }
            }
            position += count;
        }

        void sampling(byte[] bytes, int offset, int count, int pixel) {
            int rangeLeft = position;
            int rangeRight = position + count;

            // 判断像素是否在区域内
            if (pixel + 3 >= rangeLeft && pixel + 3 <= rangeRight) {
                // Log.d("AAAAA", "position " + position + "  rangeLeft " + rangeLeft + " rangeRight" + rangeRight);
                int targetIndex = position > pixel ? position : pixel;
                for (; targetIndex <= pixel + 3; targetIndex++) {
                    buffer.put(bytes[targetIndex - position + offset]);
                }
            }
        }

        int remaining() {
            return this.frameSize - position;
        }

        void clear() {
            position = 0;
            buffer.clear();
        }

        byte[] array() {
            return buffer.array();
        }
    }

    private int computeBarColor(byte[] bytes) {
        if (bytes.length == 0) {
            return Integer.MIN_VALUE;
        }

        int lightPixelCount = 0;
        int darkPixelCount = 0;
        for (int i = 0; i < bytes.length; i += 4) {
            if (pixelIsLightColor(bytes, i)) {
                lightPixelCount++;
            } else {
                darkPixelCount++;
            }
        }

        if (lightPixelCount > darkPixelCount) {
            System.out.println("Gesture ADB Process: iOS White Bar Color Set To {Color.Black}");
            return 0xFF000000;
        } else {
            System.out.println("Gesture ADB Process: iOS White Bar Color Set To {Color.White}");
            return 0xFFFFFFFF;
        }
    }

    private boolean pixelIsLightColor(byte[] rawImage, int index) {
        if (index > -1 && (index + 3) <= rawImage.length) {
            int r = 0, g = 0, b = 0, a = 0;
            r = rawImage[index];
            g = rawImage[index + 1];
            b = rawImage[index + 2];
            a = rawImage[index + 3];
            if (r < 0) {
                r = 255;
            }
            if (g < 0) {
                g = 255;
            }
            if (b < 0) {
                b = 255;
            }
            return (r > 180 && b > 180 && g > 180);
        }
        // Log.d(">>>>", "pixel overflow, index:" + index + "  array:" + rawImage.length);
        return false;
    }

    public int autoBarColor() {
        try {
            Process exec = Runtime.getRuntime().exec("screencap");
            bufferedInputStream = new BufferedInputStream(exec.getInputStream());

            int cacheSize = 1024 * 1024; // 1M
            byte[] tempBuffer = new byte[cacheSize];
            PixelGather byteBuffer = PixelGather.frameBuffer(GlobalState.displayHeight, GlobalState.displayWidth);

            int count;
            while ((count = bufferedInputStream.read(tempBuffer)) > 0) {
                int remaining = byteBuffer.remaining();
                if (count > remaining) { // 读取了超过一帧
                    byteBuffer.put(tempBuffer, 0, remaining);
                    break;
                } else if (count == remaining) { // 刚好读满一帧
                    byteBuffer.put(tempBuffer, 0, count);
                    break;
                } else { // 不到一帧，继续读
                    byteBuffer.put(tempBuffer, 0, count);
                }
            }
            // 更新颜色
            return computeBarColor(byteBuffer.array());
        } catch (Exception e) {
            System.out.println("Gesture ADB BarColor " + e.getMessage());
        }
        return Integer.MIN_VALUE;
    }
}