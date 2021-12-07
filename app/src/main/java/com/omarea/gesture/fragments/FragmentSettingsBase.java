package com.omarea.gesture.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.omarea.gesture.Gesture;
import com.omarea.gesture.model.ActionModel;
import com.omarea.gesture.dialog.DialogHandlerEX;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.ui.ActionPicker;
import com.omarea.gesture.util.UITools;

public class FragmentSettingsBase extends Fragment {
    protected SharedPreferences config;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        config = getActivity().getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void restartService() {
        try {
            Intent intent = new Intent(getString(R.string.action_config_changed));
            getActivity().sendBroadcast(intent);
        } catch (Exception ignored) {
        }
    }


    protected void bindHandlerPicker(int id, final String key, final int defValue) {
        final Button button = getActivity().findViewById(id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHandlerPicker(key, defValue);
            }
        });
    }

    private void openHandlerPicker(final String key, final int defValue) {
        final int currentValue = config.getInt(key, defValue);
        new ActionPicker(this.getActivity()).open(new ActionPicker.ActionPickerCallback() {
            @Override
            public void onConfirm(ActionModel action, String data) {
                config.edit().putInt(key, action.actionCode).apply();

                if (action.extraRequired) {
                    new DialogHandlerEX().openDialog(getActivity(), key, action.actionCode);
                }
                restartService();
            }
        }, currentValue);
    }

    protected void updateActionText(int id, String key, int defaultAction) {
        ((Button) getActivity().findViewById(id)).setText(Gesture.gestureActions.getOption(config.getInt(key, defaultAction)));
    }

    protected void bindCheckable(int id, final String key, boolean defValue) {
        final CompoundButton switchItem = getActivity().findViewById(id);
        switchItem.setChecked(config.getBoolean(key, defValue));
        switchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.edit().putBoolean(key, ((Checkable) v).isChecked()).apply();
                restartService();
            }
        });
    }

    protected void bindVisibility(int compoundButton, int targetView) {
        final View view = getActivity().findViewById(targetView);
        CompoundButton button = ((CompoundButton)getActivity().findViewById(compoundButton));
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        view.setVisibility(button.isChecked() ? View.VISIBLE : View.GONE);
    }

    protected void setViewBackground(View view, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setGradientType(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(UITools.dp2px(getActivity(), 15));
        drawable.setColor(color);
        drawable.setStroke(2, 0xff888888);
        view.setBackground(drawable);
    }

    /**
     * 选择颜色
     *
     * @param key
     * @param defValue
     */
    private void openColorPicker(final String key, final int defValue, final String title) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.gesture_color_picker, null);
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        alphaBar.setOnSeekBarChangeListener(listener);
        redBar.setOnSeekBarChangeListener(listener);
        greenBar.setOnSeekBarChangeListener(listener);
        blueBar.setOnSeekBarChangeListener(listener);

        new AlertDialog.Builder(getActivity())
                .setTitle(title == null ? "" : title)
                .setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int color = Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
                        config.edit().putInt(key, color).apply();
                        restartService();
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

    protected void bindColorPicker(int id, final String key, final int defValue, final String title) {
        final Button button = getActivity().findViewById(id);

        setViewBackground(button, config.getInt(key, defValue));

        // button.setBackgroundColor(config.getInt(key, defValue));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker(key, defValue, title);
            }
        });
    }

    protected void bindSeekBar(int id, final String key, int defValue, final boolean updateView) {
        final SeekBar seekBar = getActivity().findViewById(id);
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
                    restartService();
                }
            }
        });
    }
}
