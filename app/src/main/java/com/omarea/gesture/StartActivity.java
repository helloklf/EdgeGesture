package com.omarea.gesture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class StartActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
    }
}
