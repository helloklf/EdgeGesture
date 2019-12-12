package com.omarea.gesture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.omarea.gesture.util.Handlers;

public class DialogHandlerEX {
    public void openDialog(Context context, final String key, int customActionCode) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).setCancelable(false);

        final String fullKey = SpfConfigEx.prefix_shell + key;
        final SharedPreferences configFile = context.getSharedPreferences(SpfConfigEx.configFile, Context.MODE_PRIVATE);
        final SharedPreferences.Editor config = configFile.edit();
        config.remove(SpfConfigEx.prefix_app + key);
        config.remove(SpfConfigEx.prefix_shell + key);

        switch (customActionCode) {
            case Handlers.CUSTOM_ACTION_APP: {

                break;
            }
            case Handlers.CUSTOM_ACTION_SHELL: {
                alertDialog.setTitle(context.getString(R.string.custom_shell));
                View view = LayoutInflater.from(context).inflate(R.layout.layout_ex_shell, null);
                final EditText editText = view.findViewById(R.id.ex_shell);
                editText.setText(configFile.getString(fullKey, ""));

                alertDialog.setView(view);
                alertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.putString(fullKey, editText.getText().toString()).apply();
                    }
                });

                break;
            }
            default: {
                return;
            }
        }

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
}
