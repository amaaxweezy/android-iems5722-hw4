package com.iems5722.translateapp;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mondwan on 21/3/15.
 */
public class TranslationRecordAdapter extends ArrayAdapter<String> {
    // A constant for records aligned to left
    public static final int ALIGN_LEFT_TYPE = 1;

    // A constant for records aligned to right
    public static final int ALIGN_RIGHT_TYPE = 0;

    // Define a tag for logging
    private static final String TAG = TranslationRecordAdapter.class.getClass().getSimpleName();

    public TranslationRecordAdapter(Activity context, List<String> list) {
        super(context, R.layout.translation_record_line, list);
    }

    @Override
    public int getViewTypeCount() {
        // Return the total number of view types. this value should never change at runtime
        //
        // @return int
        //
        //   Number of types

        return 2;
    }

    @Override
    public int getItemViewType(int pos) {
        // Return the type of record based on it's position
        //
        // @param pos int
        //
        //   Position from this.getPosition(item)
        //
        // @return int
        //
        //   Type of the view

        int type = pos % 2;
        int ret = -1;

        if (type == ALIGN_LEFT_TYPE) {
            ret = ALIGN_LEFT_TYPE;
        } else if (type == ALIGN_RIGHT_TYPE) {
            ret = ALIGN_RIGHT_TYPE;
        }

        return ret;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = this.getItemViewType(position);

        int layoutResource = -1;

        if (viewType == ALIGN_LEFT_TYPE) {
            layoutResource = R.layout.translation_record_line;
        } else if (viewType == ALIGN_RIGHT_TYPE) {
            layoutResource = R.layout.translation_record_line;
        }

        TextView textView;
        Context ctx = this.getContext();

        if (convertView != null) {
            textView = (TextView) convertView.findViewById(R.id.recordLine);
            textView.setText(this.getItem(position));
        } else {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );
            convertView = inflater.inflate(layoutResource, parent, false);
            textView = (TextView) convertView.findViewById(R.id.recordLine);
            textView.setText(this.getItem(position));
        }

        if (viewType == ALIGN_RIGHT_TYPE) {
            textView.setGravity(Gravity.END | Gravity.BOTTOM);
        } else if (viewType == ALIGN_LEFT_TYPE) {
            textView.setGravity(Gravity.START | Gravity.BOTTOM);
        }

        return convertView;
    }
}
