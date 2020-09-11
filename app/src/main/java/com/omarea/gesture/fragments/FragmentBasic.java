package com.omarea.gesture.fragments;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.omarea.gesture.AdbProcessExtractor;
import com.omarea.gesture.EnhancedModeGuide;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.remote.RemoteAPI;
import com.omarea.gesture.util.GlobalState;

import java.util.List;

public class FragmentBasic extends FragmentSettingsBase {
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
            if (GlobalState.enhancedMode) {
                setResultCode(0);
                setResultData("EnhancedMode √");
            } else {
                setResultCode(5);
                setResultData("EnhancedMode ×");
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gesture_settings_basic, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Activity activity = getActivity();

        final CompoundButton enable_service = activity.findViewById(R.id.enable_service);
        enable_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceRunning()) {
                    // System.exit(0);
                    try {
                        Intent intent = new Intent(getString(R.string.action_service_disable));
                        getActivity().sendBroadcast(intent);
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                restartService();
                            }
                        }, 1000);
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }
                    String msg = getString(R.string.service_active_desc) + getString(R.string.app_name);
                    Gesture.toast(msg, Toast.LENGTH_LONG);
                }
            }
        });

        bindSeekBar(R.id.bar_hover_time, SpfConfig.CONFIG_HOVER_TIME, SpfConfig.CONFIG_HOVER_TIME_DEFAULT, true);
        bindSeekBar(R.id.vibrator_time, SpfConfig.VIBRATOR_TIME, SpfConfig.VIBRATOR_TIME_DEFAULT, true);
        bindSeekBar(R.id.vibrator_amplitude, SpfConfig.VIBRATOR_AMPLITUDE, SpfConfig.VIBRATOR_AMPLITUDE_DEFAULT, true);
        bindSeekBar(R.id.vibrator_time_long, SpfConfig.VIBRATOR_TIME_LONG, SpfConfig.VIBRATOR_TIME_LONG_DEFAULT, true);
        bindSeekBar(R.id.vibrator_amplitude_long, SpfConfig.VIBRATOR_AMPLITUDE_LONG, SpfConfig.VIBRATOR_AMPLITUDE_LONG_DEFAULT, true);
        bindCheckable(R.id.vibrator_quick_slide, SpfConfig.VIBRATOR_QUICK_SLIDE, SpfConfig.VIBRATOR_QUICK_SLIDE_DEFAULT);

        // 震动 跟随系统默认
        final View vibrator_custom = getActivity().findViewById(R.id.vibrator_custom);
        final Switch vibrator_use_system = getActivity().findViewById(R.id.vibrator_use_system);
        vibrator_use_system.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.edit().putBoolean(SpfConfig.VIBRATOR_USE_SYSTEM, vibrator_use_system.isChecked()).apply();
                vibrator_custom.setVisibility(vibrator_use_system.isChecked() ? View.GONE : View.VISIBLE);
            }
        });
        vibrator_use_system.setChecked(config.getBoolean(SpfConfig.VIBRATOR_USE_SYSTEM, SpfConfig.VIBRATOR_USE_SYSTEM_DEFAULT));
        vibrator_custom.setVisibility(vibrator_use_system.isChecked() ? View.GONE : View.VISIBLE);

        getActivity().findViewById(R.id.faq_click_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/helloklf/EdgeGesture/blob/master/docs/FAQ.md");
            }
        });
        getActivity().findViewById(R.id.steps_click_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/helloklf/EdgeGesture/blob/master/docs/EnhancedMode.md");
            }
        });

        ImageView enhanced_mode = activity.findViewById(R.id.enhanced_mode);
        enhanced_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AdbProcessExtractor().updateAdbProcessState(getActivity(), true);

                GlobalState.enhancedMode = RemoteAPI.isOnline();
                if (GlobalState.enhancedMode) {
                    Gesture.toast("别点啦！增强模式已经好了", Toast.LENGTH_SHORT);
                    updateView();
                } else {
                    String shell = new AdbProcessExtractor().extract(activity);
                    if (shell != null) {
                        new EnhancedModeGuide().show(activity, shell);
                    } else {
                        Gesture.toast("无法提取外接程序文件", Toast.LENGTH_SHORT);
                    }
                }
            }
        });

        updateView();
        activity.registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.action_adb_process)));
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent();
            //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        } catch (Exception ex) {
            //
        }
    }

    private void updateView() {
        Activity activity = getActivity();

        ((Checkable) activity.findViewById(R.id.enable_service)).setChecked(serviceRunning());

        GlobalState.enhancedMode = RemoteAPI.isOnline();
        ImageView enhanced_mode = activity.findViewById(R.id.enhanced_mode);
        if (GlobalState.enhancedMode) {
            enhanced_mode.setImageDrawable(activity.getDrawable(R.drawable.adb_on));
        } else {
            enhanced_mode.setImageDrawable(activity.getDrawable(R.drawable.adb_off));
        }
    }

    @Override
    protected void restartService() {
        updateView();

        super.restartService();
    }

    private boolean serviceRunning(Context context, String serviceName) {
        AccessibilityManager m = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = m.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.getId().endsWith(serviceName)) {
                return true;
            }
        }
        return false;
    }

    private boolean serviceRunning() {
        return serviceRunning(getActivity(), "AccessibilityServiceGesture");
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}
