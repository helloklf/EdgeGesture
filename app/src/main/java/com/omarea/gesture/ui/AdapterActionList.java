package com.omarea.gesture.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.omarea.gesture.R;
import com.omarea.gesture.model.ActionModel;

public class AdapterActionList extends BaseAdapter {
    private final ActionModel[] actions;
    private int selectedIndex = -1;
    public AdapterActionList(ActionModel[] actions, int currentSelected) {
        this.actions = actions;

        int index = -1;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].actionCode == currentSelected) {
                index = i;
                break;
            }
        }
        selectedIndex = index;
    }

    @Override
    public int getCount() {
        return actions.length;
    }

    @Override
    public Object getItem(int position) {
        return actions[position];
    }

    @Override
    public long getItemId(int position) {
        return ((ActionModel)getItem(position)).actionCode;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ActionModel actionModel = ((ActionModel) getItem(position));
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action, null);
        TextView textView = view.findViewById(R.id.item_title);
        if (actionModel.extraRequired) {
            view.findViewById(R.id.item_icon).setVisibility(View.VISIBLE);
        }
        textView.setText(actionModel.title);
        if (position == selectedIndex) {
            // textView.setTextColor(textView.getHighlightColor());
            textView.setTextColor(textView.getContext().getResources().getColor(R.color.colorAccent));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == selectedIndex) {
                    return;
                }
                selectedIndex = position;
                notifyDataSetChanged();
            }
        });
        return view;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ActionModel getSelectedItem() {
        if (selectedIndex > -1) {
            return (ActionModel)getItem(selectedIndex);
        }
        return null;
    }
}
