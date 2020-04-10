package com.omarea.gesture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EnhancedModeGuide {
    public void show(Context context, String shell) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_enhanced_mode_guide, null);
        EditText editText = view.findViewById(R.id.enhanced_mode_shell);
        editText.setText("adb shell " + shell);

        new AlertDialog.Builder(context).setView(view).setPositiveButton(context.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                })
                .create().show();
    }
}
