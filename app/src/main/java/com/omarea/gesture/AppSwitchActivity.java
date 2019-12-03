package com.omarea.gesture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AppSwitchActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Intent currentIntent = getIntent();
            boolean customAnimation = false;
            if (currentIntent.hasExtra("animation") && currentIntent.getIntExtra("animation", SpfConfig.HOME_ANIMATION_DEFAULT) == SpfConfig.HOME_ANIMATION_CUSTOM) {
                customAnimation = true;
            }

            if (currentIntent.hasExtra("next")) {
                String appPackageName = currentIntent.getStringExtra("next");
                Intent intent = getPackageManager().getLaunchIntentForPackage(appPackageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
                intent.setClassName("", "");
            } else if (currentIntent.hasExtra("prev")) {
                String appPackageName = currentIntent.getStringExtra("prev");
                Intent intent = getPackageManager().getLaunchIntentForPackage(appPackageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
            } else if (currentIntent.hasExtra("home")) {
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                if (customAnimation) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                if (customAnimation) {
                    overridePendingTransition(R.anim.home_enter, R.anim.app_exit);
                }
            }
            finish();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
