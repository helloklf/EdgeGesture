package com.omarea.gesture.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.model.ActionModel;

public class ActionPicker {
    private Activity activity;

    public ActionPicker(Activity activity) {
        this.activity = activity;
    }

    @FunctionalInterface
    public static interface ActionPickerCallback {
        void onConfirm(ActionModel action, String data);
    }

    public void open(final ActionPickerCallback callback, int current) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity, R.style.custom_alert_dialog).create();
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_actions_picker, null);
        alertDialog.setView(dialogView);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        if (window != null) {
            View view = window.getDecorView();
            // window.setBackgroundDrawableResource(android.R.color.transparent);
            view.setSystemUiVisibility(activity.getWindow().getDecorView().getWindowSystemUiVisibility());
        }

        final ListView listView = dialogView.findViewById(R.id.action_list);
        View.OnClickListener clickDismiss = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        };
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(clickDismiss);
        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterActionList adapterActionList = (AdapterActionList)listView.getAdapter();
                ActionModel actionModel = adapterActionList.getSelectedItem();
                if (actionModel != null) {
                    callback.onConfirm(actionModel, actionModel.title);
                    alertDialog.dismiss();
                }
            }
        });

        final ActionModel[] items = Gesture.gestureActions.getOptions();
        listView.setAdapter(new AdapterActionList(items, current));
    }
}
