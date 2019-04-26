package com.omarea.gesture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


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
        hide_start_icon.setChecked(p.getComponentEnabledSetting(startActivity) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

        bindSwitch(R.id.allow_bottom, SpfConfig.CONFIG_BOTTOM_ALLOW, SpfConfig.CONFIG_BOTTOM_ALLOW_DEFAULT);
        bindSwitch(R.id.allow_left, SpfConfig.CONFIG_LEFT_ALLOW, SpfConfig.CONFIG_LEFT_ALLOW_DEFAULT);
        bindSwitch(R.id.allow_right, SpfConfig.CONFIG_RIGHT_ALLOW, SpfConfig.CONFIG_RIGHT_ALLOW_DEFAULT);

        bindSeekBar(R.id.bar_height_left, SpfConfig.CONFIG_LEFT_HEIGHT, SpfConfig.CONFIG_LEFT_HEIGHT_DEFAULT);
        bindSeekBar(R.id.bar_height_right, SpfConfig.CONFIG_RIGHT_HEIGHT, SpfConfig.CONFIG_RIGHT_HEIGHT_DEFAULT);

        bindColorPicker(R.id.bar_color_bottom, SpfConfig.CONFIG_BOTTOM_COLOR, SpfConfig.CONFIG_BOTTOM_COLOR_DEFAULT);
        bindColorPicker(R.id.bar_color_left, SpfConfig.CONFIG_LEFT_COLOR, SpfConfig.CONFIG_LEFT_COLOR_DEFAULT);
        bindColorPicker(R.id.bar_color_right, SpfConfig.CONFIG_RIGHT_COLOR, SpfConfig.CONFIG_RIGHT_COLOR_DEFAULT);
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

    private void bindSeekBar(int id, final String key, int defValue) {
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
                updateView();
            }
        });
    }

    private void bindColorPicker(int id, final String key, int defValue) {
        final Button button = findViewById(id);
        button.setBackgroundColor(config.getInt(key, defValue));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void updateView() {
        try {
            Intent intent = new Intent(getString(R.string.action_config_changed));
            sendBroadcast(intent);
        } catch (Exception ex) {

        }
    }
}
