package com.omarea.gesture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;


public class SettingsActivity extends Activity {
    private SharedPreferences config;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        config = getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);

        final PackageManager p = getPackageManager();
        final ComponentName startActivity = new ComponentName(this.getApplicationContext(), StartActivity.class);
        final Switch hide_start_icon = findViewById(R.id.hide_start_icon);
        hide_start_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (hide_start_icon.isChecked()) {
                        p.setComponentEnabledSetting(startActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    } else {
                        p.setComponentEnabledSetting(startActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    }
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        int activityState = p.getComponentEnabledSetting(startActivity);
        hide_start_icon.setChecked(activityState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED && activityState != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        bindSwitch(R.id.allow_bottom, SpfConfig.CONFIG_BOTTOM_ALLOW, SpfConfig.CONFIG_BOTTOM_ALLOW_DEFAULT);
        bindSwitch(R.id.allow_left, SpfConfig.CONFIG_LEFT_ALLOW, SpfConfig.CONFIG_LEFT_ALLOW_DEFAULT);
        bindSwitch(R.id.allow_right, SpfConfig.CONFIG_RIGHT_ALLOW, SpfConfig.CONFIG_RIGHT_ALLOW_DEFAULT);

        bindSeekBar(R.id.bar_width_bottom, SpfConfig.CONFIG_BOTTOM_WIDTH, SpfConfig.CONFIG_BOTTOM_WIDTH_DEFAULT, true);
        bindSeekBar(R.id.bar_height_left, SpfConfig.CONFIG_LEFT_HEIGHT, SpfConfig.CONFIG_LEFT_HEIGHT_DEFAULT, true);
        bindSeekBar(R.id.bar_height_right, SpfConfig.CONFIG_RIGHT_HEIGHT, SpfConfig.CONFIG_RIGHT_HEIGHT_DEFAULT, true);

        bindSeekBar(R.id.edge_side_width, SpfConfig.CONFIG_HOT_SIDE_WIDTH, SpfConfig.CONFIG_HOT_SIDE_WIDTH_DEFAULT, true);
        bindSeekBar(R.id.edge_bottom_height, SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT, SpfConfig.CONFIG_HOT_BOTTOM_HEIGHT_DEFAULT, true);

        bindSeekBar(R.id.bar_hover_time, SpfConfig.CONFIG_HOVER_TIME, SpfConfig.CONFIG_HOVER_TIME_DEFAULT,  true);
        bindSeekBar(R.id.vibrator_time, SpfConfig.VIBRATOR_TIME, SpfConfig.VIBRATOR_TIME_DEFAULT, true);
        bindSeekBar(R.id.vibrator_amplitude, SpfConfig.VIBRATOR_AMPLITUDE, SpfConfig.VIBRATOR_AMPLITUDE_DEFAULT, true);

        bindColorPicker(R.id.bar_color_bottom, SpfConfig.CONFIG_BOTTOM_COLOR, SpfConfig.CONFIG_BOTTOM_COLOR_DEFAULT);
        bindColorPicker(R.id.bar_color_left, SpfConfig.CONFIG_LEFT_COLOR, SpfConfig.CONFIG_LEFT_COLOR_DEFAULT);
        bindColorPicker(R.id.bar_color_right, SpfConfig.CONFIG_RIGHT_COLOR, SpfConfig.CONFIG_RIGHT_COLOR_DEFAULT);

        bindHandlerPicker(R.id.tap_bottom, SpfConfig.CONFIG_BOTTOM_EVBET, SpfConfig.CONFIG_BOTTOM_EVBET_DEFAULT);
        bindHandlerPicker(R.id.hover_bottom, SpfConfig.CONFIG_BOTTOM_EVBET_HOVER, SpfConfig.CONFIG_BOTTOM_EVBET_HOVER_DEFAULT);
        bindHandlerPicker(R.id.tap_left, SpfConfig.CONFIG_LEFT_EVBET, SpfConfig.CONFIG_LEFT_EVBET_DEFAULT);
        bindHandlerPicker(R.id.hover_left, SpfConfig.CONFIG_LEFT_EVBET_HOVER, SpfConfig.CONFIG_LEFT_EVBET_HOVER_DEFAULT);
        bindHandlerPicker(R.id.tap_right, SpfConfig.CONFIG_RIGHT_EVBET, SpfConfig.CONFIG_RIGHT_EVBET_DEFAULT);
        bindHandlerPicker(R.id.hover_right, SpfConfig.CONFIG_RIGHT_EVBET_HOVER, SpfConfig.CONFIG_RIGHT_EVBET_HOVER_DEFAULT);
    }

    private void updateView() {
        findViewById(R.id.bar_color_bottom).setBackgroundColor(config.getInt(SpfConfig.CONFIG_BOTTOM_COLOR, SpfConfig.CONFIG_BOTTOM_COLOR_DEFAULT));
        findViewById(R.id.bar_color_left).setBackgroundColor(config.getInt(SpfConfig.CONFIG_LEFT_COLOR, SpfConfig.CONFIG_LEFT_COLOR_DEFAULT));
        findViewById(R.id.bar_color_right).setBackgroundColor(config.getInt(SpfConfig.CONFIG_RIGHT_COLOR, SpfConfig.CONFIG_RIGHT_COLOR_DEFAULT));

        ((Button)findViewById(R.id.tap_bottom)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_BOTTOM_EVBET, SpfConfig.CONFIG_BOTTOM_EVBET_DEFAULT)));
        ((Button)findViewById(R.id.hover_bottom)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_BOTTOM_EVBET_HOVER, SpfConfig.CONFIG_BOTTOM_EVBET_HOVER_DEFAULT)));
        ((Button)findViewById(R.id.tap_left)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_LEFT_EVBET, SpfConfig.CONFIG_LEFT_EVBET_DEFAULT)));
        ((Button)findViewById(R.id.hover_left)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_LEFT_EVBET_HOVER, SpfConfig.CONFIG_LEFT_EVBET_HOVER_DEFAULT)));
        ((Button)findViewById(R.id.tap_right)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_RIGHT_EVBET, SpfConfig.CONFIG_RIGHT_EVBET_DEFAULT)));
        ((Button)findViewById(R.id.hover_right)).setText(Handlers.getOption(config.getInt(SpfConfig.CONFIG_RIGHT_EVBET_HOVER, SpfConfig.CONFIG_RIGHT_EVBET_HOVER_DEFAULT)));

        try {
            Intent intent = new Intent(getString(R.string.action_config_changed));
            sendBroadcast(intent);
        } catch (Exception ignored) {
        }
    }

    private void bindSwitch(int id, final String key, boolean defValue) {
        final Switch switchItem = findViewById(id);
        switchItem.setChecked(config.getBoolean(key, defValue));
        switchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.edit().putBoolean(key, ((Switch)v).isChecked()).apply();
                updateView();
            }
        });
    }

    private void bindSeekBar(int id, final String key, int defValue, final boolean updateView) {
        final SeekBar seekBar = findViewById(id);
        seekBar.setProgress(config.getInt(key, defValue));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.edit().putInt(key, (seekBar.getProgress())).apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (updateView) {
                    updateView();
                }
            }
        });
    }

    private void bindColorPicker(int id, final String key, final int defValue) {
        final Button button = findViewById(id);
        button.setBackgroundColor(config.getInt(key, defValue));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker(key, defValue);
            }
        });
    }

    private void bindHandlerPicker(int id, final String key, final int defValue) {
        final Button button = findViewById(id);
        button.setText(Handlers.getOption(config.getInt(key, defValue)));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHandlerPicker(key, defValue);
            }
        });
    }

    /**
     * 选择响应动作
     * @param key
     * @param defValue
     */
    private void openHandlerPicker(final String key, final int defValue) {
        String[] items = Handlers.getOptions();
        final ArrayList<Integer> values = Handlers.getValues();

        int currentValue = config.getInt(key, defValue);
        int index = values.indexOf(currentValue);
;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.handler_picker))
                .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.edit().putInt(key, values.get(which)).apply();
                        updateView();

                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * 选择颜色
     * @param key
     * @param defValue
     */
    private void openColorPicker(final String key, final int defValue) {
        View view = getLayoutInflater().inflate(R.layout.color_picker, null);
        int currentColor = config.getInt(key, defValue);
        final SeekBar alphaBar = view.findViewById(R.id.color_alpha);
        final SeekBar redBar = view.findViewById(R.id.color_red);
        final SeekBar greenBar = view.findViewById(R.id.color_green);
        final SeekBar blueBar = view.findViewById(R.id.color_blue);
        final Button colorPreview = view.findViewById(R.id.color_preview);

        alphaBar.setProgress(Color.alpha(currentColor));
        redBar.setProgress(Color.red(currentColor));
        greenBar.setProgress(Color.green(currentColor));
        blueBar.setProgress(Color.blue(currentColor));
        colorPreview.setBackgroundColor(currentColor);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int color = Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
                colorPreview.setBackgroundColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {  }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {  }
        };
        alphaBar.setOnSeekBarChangeListener(listener);
        redBar.setOnSeekBarChangeListener(listener);
        greenBar.setOnSeekBarChangeListener(listener);
        blueBar.setOnSeekBarChangeListener(listener);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.effect_color_picker))
                .setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int color = Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
                        config.edit().putInt(key, color).apply();
                        colorPreview.setBackgroundColor(color);
                        updateView();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalState.testMode = true;
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalState.testMode = false;
        updateView();
    }
}
