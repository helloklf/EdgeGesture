package com.omarea.gesture;

public class SpfConfig {
    public final static String BACK_HOME_ANIMATION = "BACK_HOME_ANIMATION";
    public final static String APP_SWITCH_ANIMATION = "APP_SWITCH_ANIMATION";
    public final static int ANIMATION_DEFAULT = 0;
    public final static int ANIMATION_BASIC = 1;
    public final static int ANIMATION_CUSTOM = 2;
    public final static int ANIMATION_FAST = 3;
    public static final String ConfigFile = "main";
    public static final String AppSwitchBlackList = "app_switch_black_list";
    // 悬停时间
    public static final String CONFIG_HOVER_TIME = "CONFIG_HOVER_TIME_MS";
    public static final int CONFIG_HOVER_TIME_DEFAULT = 180;
    // 边缘手势视觉反馈颜色
    public static final String CONFIG_EDGE_COLOR = "CONFIG_LEFT_COLOR";
    public static final int CONFIG_EDGE_COLOR_DEFAULT = 0xee101010;
    // 左侧手势区域
    public static final String CONFIG_LEFT_ALLOW_PORTRAIT = "CONFIG_LEFT_ALLOW_PORTRAIT";
    public static final boolean CONFIG_LEFT_ALLOW_PORTRAIT_DEFAULT = true;
    public static final String CONFIG_LEFT_ALLOW_LANDSCAPE = "CONFIG_LEFT_ALLOW_LANDSCAPE";
    public static final boolean CONFIG_LEFT_ALLOW_LANDSCAPE_DEFAULT = false;
    public static final String CONFIG_LEFT_HEIGHT = "CONFIG_LEFT_HEIGHT";
    public static final int CONFIG_LEFT_HEIGHT_DEFAULT = 65; // 高度百分比
    public static final String CONFIG_LEFT_EVENT = "CONFIG_LEFT_EVENT";
    public static final int CONFIG_LEFT_EVENT_DEFAULT = GestureActions.GLOBAL_ACTION_BACK;
    public static final String CONFIG_LEFT_EVENT_HOVER = "CONFIG_LEFT_EVENT_HOVER";
    public static final int CONFIG_LEFT_EVENT_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_RECENTS;
    // 右侧手势区域
    public static final String CONFIG_RIGHT_ALLOW_PORTRAIT = "CONFIG_RIGHT_ALLOW_PORTRAIT";
    public static final boolean CONFIG_RIGHT_ALLOW_PORTRAIT_DEFAULT = true;
    public static final String CONFIG_RIGHT_ALLOW_LANDSCAPE = "CONFIG_RIGHT_ALLOW_LANDSCAPE";
    public static final boolean CONFIG_RIGHT_ALLOW_LANDSCAPE_DEFAULT = false;
    public static final String CONFIG_RIGHT_HEIGHT = "CONFIG_RIGHT_HEIGHT";
    public static final int CONFIG_RIGHT_HEIGHT_DEFAULT = 65; // 高度百分比
    public static final String CONFIG_RIGHT_EVENT = "CONFIG_RIGHT_EVENT";
    public static final int CONFIG_RIGHT_EVENT_DEFAULT = GestureActions.GLOBAL_ACTION_BACK;
    public static final String CONFIG_RIGHT_EVENT_HOVER = "CONFIG_RIGHT_EVENT_HOVER";
    public static final int CONFIG_RIGHT_EVENT_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_RECENTS;
    // 底部手势区域
    public static final String CONFIG_BOTTOM_ALLOW_PORTRAIT = "CONFIG_BOTTOM_ALLOW_PORTRAIT";
    public static final boolean CONFIG_BOTTOM_ALLOW_PORTRAIT_DEFAULT = true;
    public static final String CONFIG_BOTTOM_ALLOW_LANDSCAPE = "CONFIG_BOTTOM_ALLOW_LANDSCAPE";
    public static final boolean CONFIG_BOTTOM_ALLOW_LANDSCAPE_DEFAULT = false;
    public static final String CONFIG_BOTTOM_WIDTH = "CONFIG_BOTTOM_WIDTH";
    public static final int CONFIG_BOTTOM_WIDTH_DEFAULT = 100;
    public static final String CONFIG_BOTTOM_EVENT = "CONFIG_BOTTOM_EVENT";
    public static final int CONFIG_BOTTOM_EVENT_DEFAULT = GestureActions.GLOBAL_ACTION_HOME;
    public static final String CONFIG_BOTTOM_EVENT_HOVER = "CONFIG_BOTTOM_EVENT_HOVER";
    public static final int CONFIG_BOTTOM_EVENT_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_RECENTS;
    // 热区灵敏度
    public static final String CONFIG_HOT_SIDE_WIDTH = "CONFIG_SIDE_WIDTH";
    public static final int CONFIG_HOT_SIDE_WIDTH_DEFAULT = 12; // 侧边热区宽度
    public static final String CONFIG_HOT_BOTTOM_HEIGHT = "CONFIG_HOT_BOTTOM_HEIGHT";
    public static final int CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT = 9; // 底部热区高度
    public static final String VIBRATOR_USE_SYSTEM = "VIBRATOR_USE_SYSTEM"; // 是否使用系统默认的震动设置
    public static final boolean VIBRATOR_USE_SYSTEM_DEFAULT = true; // 是否使用系统默认的震动设置 默认值
    public static final String VIBRATOR_TIME = "VIBRATOR_TIME"; // 震动时长
    public static final int VIBRATOR_TIME_DEFAULT = 10; // 震动时长 默认值
    public static final String VIBRATOR_AMPLITUDE = "VIBRATOR_AMPLITUDE"; // 震动强度
    public static final int VIBRATOR_AMPLITUDE_DEFAULT = 255; // 震动强度 默认值
    public static final String VIBRATOR_TIME_LONG = "VIBRATOR_TIME_LONG"; // 震动时长
    public static final int VIBRATOR_TIME_LONG_DEFAULT = 16; // 震动时长 默认值
    public static final String VIBRATOR_AMPLITUDE_LONG = "VIBRATOR_AMPLITUDE_LONG"; // 震动强度
    public static final int VIBRATOR_AMPLITUDE_LONG_DEFAULT = 255; // 震动强度 默认值
    public static final String VIBRATOR_QUICK_SLIDE = "VIBRATOR_QUICK_SLIDE"; // 是否开启快速滑动的震动反馈
    public static final boolean VIBRATOR_QUICK_SLIDE_DEFAULT = false; // 是否开启快速滑动的震动反馈 默认值
    // iOS小横条开关
    public static final String LANDSCAPE_IOS_BAR = "landscape_ios_bar";
    public static final boolean LANDSCAPE_IOS_BAR_DEFAULT = true;
    public static final String PORTRAIT_IOS_BAR = "portrait_ios_bar";
    public static final boolean PORTRAIT_IOS_BAR_DEFAULT = false;

    // iOS小横条动作r
    public static final String IOS_BAR_SLIDE_LEFT = "ios_bar_slide_left";
    public static final int IOS_BAR_SLIDE_LEFT_DEFAULT = GestureActions.VITUAL_ACTION_NEXT_APP;
    public static final String IOS_BAR_SLIDE_RIGHT = "ios_bar_slide_right";
    public static final int IOS_BAR_SLIDE_RIGHT_DEFAULT = GestureActions.VITUAL_ACTION_PREV_APP;
    public static final String IOS_BAR_SLIDE_UP = "ios_bar_slide_up";
    public static final int IOS_BAR_SLIDE_UP_DEFAULT = GestureActions.GLOBAL_ACTION_HOME;
    public static final String IOS_BAR_SLIDE_UP_HOVER = "ios_bar_slide_up_hover";
    public static final int IOS_BAR_SLIDE_UP_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_RECENTS;
    public static final String IOS_BAR_TOUCH = "ios_bar_touch"; // 轻触
    public static final int IOS_BAR_TOUCH_DEFAULT = GestureActions.GLOBAL_ACTION_BACK;
    public static final String IOS_BAR_PRESS = "ios_bar_press"; // 长按
    public static final int IOS_BAR_PRESS_DEFAULT = GestureActions.GLOBAL_ACTION_HOME;

    public static final String IOS_BAR_CONSECUTIVE = "ios_bar_consecutive"; // 连续切换
    public static final boolean IOS_BAR_CONSECUTIVE_DEFAULT = false;

    // iOS小横条样式
    public static final String IOS_BAR_WIDTH_LANDSCAPE = "ios_bar_width_landscape";
    public static final int IOS_BAR_WIDTH_DEFAULT_LANDSCAPE = 30; // 百分比
    public static final String IOS_BAR_WIDTH_PORTRAIT = "ios_bar_width_portrait";
    public static final int IOS_BAR_WIDTH_DEFAULT_PORTRAIT = 37; // 百分比
    public static final String IOS_BAR_ALPHA_FADEOUT_PORTRAIT = "ios_bar_alpha_fadeout_portrait";
    public static final int IOS_BAR_ALPHA_FADEOUT_PORTRAIT_DEFAULT = 20; // 百分比
    public static final String IOS_BAR_ALPHA_FADEOUT_LANDSCAPE = "ios_bar_alpha_fadeout_landscape";
    public static final int IOS_BAR_ALPHA_FADEOUT_LANDSCAPE_DEFAULT = 15; // 百分比
    public static final String IOS_BAR_COLOR_LANDSCAPE = "ios_bar_color_landscape";
    public static final int IOS_BAR_COLOR_LANDSCAPE_DEFAULT = 0xffffffff; // 颜色
    public static final String IOS_BAR_COLOR_PORTRAIT = "ios_bar_color_portrait";
    public static final int IOS_BAR_COLOR_PORTRAIT_DEFAULT = 0xff222222; // 颜色
    public static final String IOS_BAR_COLOR_SHADOW = "ios_bar_color_shadow";
    public static final int IOS_BAR_COLOR_SHADOW_DEFAULT = 0x88000000; // 默认阴影颜色
    public static final String IOS_BAR_SHADOW_SIZE = "ios_bar_shadow_size2";
    public static final int IOS_BAR_SHADOW_SIZE_DEFAULT = 0; // ?dp
    public static final String IOS_BAR_COLOR_STROKE = "ios_bar_color_stroke";
    public static final int IOS_BAR_COLOR_STROKE_DEFAULT = 0xffffffff; // 默认描边颜色
    public static final String IOS_BAR_STROKE_SIZE = "ios_bar_stroke_size";
    public static final int IOS_BAR_STROKE_SIZE_DEFAULT = 0; // ?dp
    public static final String IOS_BAR_MARGIN_BOTTOM_PORTRAIT = "ios_bar_margin_bottom_portrait";
    public static final int IOS_BAR_MARGIN_BOTTOM_PORTRAIT_DEFAULT = 11; // ?dp
    public static final String IOS_BAR_MARGIN_BOTTOM_LANDSCAPE = "ios_bar_margin_bottom_landscape";
    public static final int IOS_BAR_MARGIN_BOTTOM_LANDSCAPE_DEFAULT = 6; // ?dp
    public static final String IOS_BAR_HEIGHT = "ios_bar_height";
    public static final int IOS_BAR_HEIGHT_DEFAULT = 5; // ?dp
    public static final String IOS_BAR_LOCK_HIDE = "ios_bar_lock_hide";
    public static final Boolean IOS_BAR_LOCK_HIDE_DEFAULT = false;
    public static final String IOS_BAR_AUTO_COLOR = "ios_bar_auto_color_root";
    public static final Boolean IOS_BAR_AUTO_COLOR_DEFAULT = false;
    public static final String IOS_BAR_POP_BATTERY = "ios_bar_pop_battery";
    public static final Boolean IOS_BAR_POP_BATTERY_DEFAULT = false;

    public static final String GAME_OPTIMIZATION = "GAME_OPTIMIZATION"; // 游戏防误触
    public static final Boolean GAME_OPTIMIZATION_DEFAULT = true;
    public static final String ROOT = "root"; // ROOT增强
    public static final Boolean ROOT_DEFAULT = false;
    public static final String THREE_SECTION_WIDTH = "THREE_SECTION_WIDTH";
    public static final int THREE_SECTION_WIDTH_DEFAULT = 100;
    public static final String THREE_SECTION_HEIGHT = "THREE_SECTION_HEIGHT";
    public static final int THREE_SECTION_HEIGHT_DEFAULT = 9; // ?dp
    public static final String THREE_SECTION_COLOR = "THREE_SECTION_COLOR";
    public static final int THREE_SECTION_COLOR_DEFAULT = 0xee101010; // 颜色
    // 三段式手势
    public static String THREE_SECTION_LANDSCAPE = "THREE_SECTION_LANDSCAPE";
    public static boolean THREE_SECTION_LANDSCAPE_DEFAULT = false;
    public static String THREE_SECTION_PORTRAIT = "THREE_SECTION_PORTRAIT";
    public static boolean THREE_SECTION_PORTRAIT_DEFAULT = false;
    public static String THREE_SECTION_LEFT_SLIDE = "THREE_SECTION_LEFT_SLIDE";
    public static int THREE_SECTION_LEFT_SLIDE_DEFAULT = GestureActions.GLOBAL_ACTION_BACK;
    public static String THREE_SECTION_CENTER_SLIDE = "THREE_SECTION_CENTER_SLIDE";
    public static int THREE_SECTION_CENTER_SLIDE_DEFAULT = GestureActions.GLOBAL_ACTION_HOME;
    public static String THREE_SECTION_RIGHT_SLIDE = "THREE_SECTION_RIGHT_SLIDE";
    public static int THREE_SECTION_RIGHT_SLIDE_DEFAULT = GestureActions.GLOBAL_ACTION_RECENTS;
    public static String THREE_SECTION_LEFT_HOVER = "THREE_SECTION_LEFT_HOVER";
    public static int THREE_SECTION_LEFT_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_NONE;
    public static String THREE_SECTION_CENTER_HOVER = "THREE_SECTION_CENTER_HOVER";
    public static int THREE_SECTION_CENTER_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_NONE;
    public static String THREE_SECTION_RIGHT_HOVER = "THREE_SECTION_RIGHT_HOVER";
    public static int THREE_SECTION_RIGHT_HOVER_DEFAULT = GestureActions.GLOBAL_ACTION_NONE;

    public static String LOW_POWER_MODE = "LOW_POWER_MODE"; // 低功耗模式
    public static boolean LOW_POWER_MODE_DEFAULT = false;

    public static String WINDOW_WATCH = "WINDOW_WATCH"; // 窗口监测
    public static boolean WINDOW_WATCH_DEFAULT = true;

    public static String INPUT_METHOD_AVOID = "INPUT_METHOD_AVOID"; // 输入法避让
    public static boolean INPUT_METHOD_AVOID_DEFAULT = false;
}
