package com.omarea.gesture;

class SpfConfig {
    static final String ConfigFile = "main";

    // 悬停时间
    static final String CONFIG_HOVER_TIME = "CONFIG_HOVER_TIME_MS";
    static final int CONFIG_HOVER_TIME_DEFAULT = 180;

    // 左侧手势区域
    static final String CONFIG_LEFT_ALLOW_PORTRAIT = "CONFIG_LEFT_ALLOW_PORTRAIT";
    static final boolean CONFIG_LEFT_ALLOW_PORTRAIT_DEFAULT = true;
    static final String CONFIG_LEFT_ALLOW_LANDSCAPE = "CONFIG_LEFT_ALLOW_LANDSCAPE";
    static final boolean CONFIG_LEFT_ALLOW_LANDSCAPE_DEFAULT = false;
    static final String CONFIG_LEFT_HEIGHT = "CONFIG_LEFT_HEIGHT";
    static final int CONFIG_LEFT_HEIGHT_DEFAULT = 65; // 高度百分比
    static final String CONFIG_LEFT_COLOR = "CONFIG_LEFT_COLOR";
    static final int CONFIG_LEFT_COLOR_DEFAULT = 0xee101010;
    static final String CONFIG_LEFT_EVENT = "CONFIG_LEFT_EVENT";
    static final int CONFIG_LEFT_EVENT_DEFAULT = Handlers.GLOBAL_ACTION_BACK;
    static final String CONFIG_LEFT_EVENT_HOVER = "CONFIG_LEFT_EVENT_HOVER";
    static final int CONFIG_LEFT_EVENT_HOVER_DEFAULT = Handlers.GLOBAL_ACTION_RECENTS;

    // 右侧手势区域
    static final String CONFIG_RIGHT_ALLOW_PORTRAIT = "CONFIG_RIGHT_ALLOW_PORTRAIT";
    static final boolean CONFIG_RIGHT_ALLOW_PORTRAIT_DEFAULT = true;
    static final String CONFIG_RIGHT_ALLOW_LANDSCAPE = "CONFIG_RIGHT_ALLOW_LANDSCAPE";
    static final boolean CONFIG_RIGHT_ALLOW_LANDSCAPE_DEFAULT = false;
    static final String CONFIG_RIGHT_HEIGHT = "CONFIG_RIGHT_HEIGHT";
    static final int CONFIG_RIGHT_HEIGHT_DEFAULT = 65; // 高度百分比
    static final String CONFIG_RIGHT_COLOR = "CONFIG_RIGHT_COLOR";
    static final int CONFIG_RIGHT_COLOR_DEFAULT = 0xee101010;
    static final String CONFIG_RIGHT_EVENT = "CONFIG_RIGHT_EVENT";
    static final int CONFIG_RIGHT_EVENT_DEFAULT = Handlers.GLOBAL_ACTION_BACK;
    static final String CONFIG_RIGHT_EVENT_HOVER = "CONFIG_RIGHT_EVENT_HOVER";
    static final int CONFIG_RIGHT_EVENT_HOVER_DEFAULT = Handlers.GLOBAL_ACTION_RECENTS;

    // 底部手势区域
    static final String CONFIG_BOTTOM_ALLOW_PORTRAIT = "CONFIG_BOTTOM_ALLOW_PORTRAIT";
    static final boolean CONFIG_BOTTOM_ALLOW_PORTRAIT_DEFAULT = true;
    static final String CONFIG_BOTTOM_ALLOW_LANDSCAPE = "CONFIG_BOTTOM_ALLOW_LANDSCAPE";
    static final boolean CONFIG_BOTTOM_ALLOW_LANDSCAPE_DEFAULT = false;
    static final String CONFIG_BOTTOM_WIDTH = "CONFIG_BOTTOM_WIDTH";
    static final int CONFIG_BOTTOM_WIDTH_DEFAULT = 100;
    static final String CONFIG_BOTTOM_COLOR = "CONFIG_BOTTOM_COLOR";
    static final int CONFIG_BOTTOM_COLOR_DEFAULT = 0xee101010;
    static final String CONFIG_BOTTOM_EVENT = "CONFIG_BOTTOM_EVENT";
    static final int CONFIG_BOTTOM_EVENT_DEFAULT = Handlers.GLOBAL_ACTION_HOME;
    static final String CONFIG_BOTTOM_EVENT_HOVER = "CONFIG_BOTTOM_EVENT_HOVER";
    static final int CONFIG_BOTTOM_EVENT_HOVER_DEFAULT = Handlers.GLOBAL_ACTION_RECENTS;

    // 热区灵敏度
    static final String CONFIG_HOT_SIDE_WIDTH = "CONFIG_SIDE_WIDTH";
    static final int CONFIG_HOT_SIDE_WIDTH_DEFAULT = 12; // 侧边热区宽度
    static final String CONFIG_HOT_BOTTOM_HEIGHT = "CONFIG_HOT_BOTTOM_HEIGHT";
    static final int CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT = 9; // 底部热区高度

    static final String VIBRATOR_TIME = "VIBRATOR_TIME"; // 震动时长
    static final int VIBRATOR_TIME_DEFAULT = 10; // 震动时长 默认值
    static final String VIBRATOR_AMPLITUDE = "VIBRATOR_AMPLITUDE"; // 震动强度
    static final int VIBRATOR_AMPLITUDE_DEFAULT = 255; // 震动强度 默认值

    // iOS小白条开关
    static final String LANDSCAPE_IOS_BAR = "landscape_ios_bar";
    static final boolean LANDSCAPE_IOS_BAR_DEFAULT = true;
    static final String PORTRAIT_IOS_BAR = "portrait_ios_bar";
    static final boolean PORTRAIT_IOS_BAR_DEFAULT = false;

    // iOS小白条动作
    static final String IOS_BAR_SLIDE_LEFT = "ios_bar_slide_left";
    static final int IOS_BAR_SLIDE_LEFT_DEFAULT = Handlers.GLOBAL_ACTION_BACK;
    static final String IOS_BAR_SLIDE_RIGHT = "ios_bar_slide_right";
    static final int IOS_BAR_SLIDE_RIGHT_DEFAULT = Handlers.GLOBAL_ACTION_BACK;
    static final String IOS_BAR_SLIDE_UP = "ios_bar_slide_up";
    static final int IOS_BAR_SLIDE_UP_DEFAULT = Handlers.GLOBAL_ACTION_HOME;
    static final String IOS_BAR_SLIDE_UP_HOVER = "ios_bar_slide_up_hover";
    static final int IOS_BAR_SLIDE_UP_HOVER_DEFAULT = Handlers.GLOBAL_ACTION_RECENTS;

    // iOS小白条样式
    static final String IOS_BAR_WIDTH_LANDSCAPE = "ios_bar_width_landscape";
    static final int IOS_BAR_WIDTH_DEFAULT_LANDSCAPE = 30; // 百分比
    static final String IOS_BAR_WIDTH_PORTRAIT = "ios_bar_width_portrait";
    static final int IOS_BAR_WIDTH_DEFAULT_PORTRAIT = 30; // 百分比
    static final String IOS_BAR_ALPHA_FADEOUT = "ios_bar_alpha_fadeout";
    static final int IOS_BAR_ALPHA_FADEOUT_DEFAULT = 20; // 百分比
    static final String IOS_BAR_COLOR_LANDSCAPE = "ios_bar_color_landscape";
    static final int IOS_BAR_COLOR_LANDSCAPE_DEFAULT = 0xfff8f8f8; // 颜色
    static final String IOS_BAR_COLOR_PORTRAIT = "ios_bar_color_portrait";
    static final int IOS_BAR_COLOR_PORTRAIT_DEFAULT = 0xfff8f8f8; // 颜色
    static final String IOS_BAR_COLOR_SHADOW = "ios_bar_color_shadow";
    static final int IOS_BAR_COLOR_SHADOW_DEFAULT = 0x88000000; // 默认阴影颜色
    static final String IOS_BAR_SIZE_SHADOW = "ios_bar_alpha_shadow";
    static final int IOS_BAR_SIZE_SHADOW_DEFAULT = 4; // ?dp
    static final String IOS_BAR_MARGIN_BOTTOM = "ios_bar_margin_bottom";
    static final int IOS_BAR_MARGIN_BOTTOM_DEFAULT = 5; // ?dp
    static final String IOS_BAR_HEIGHT = "ios_bar_height";
    static final int IOS_BAR_HEIGHT_DEFAULT = 8; // ?dp

    // 硬件加速
    public static String HARDWARE_ACCELERATED = "HARDWARE_ACCELERATED";
    public static boolean HARDWARE_ACCELERATED_DEFAULT = false;
}
