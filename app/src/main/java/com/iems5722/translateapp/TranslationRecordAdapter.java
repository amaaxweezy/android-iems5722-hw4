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

    private class ViewHolder {
        TextView textView;
    }

    // A constant for records aligned to left
    public static final int ALIGN_LEFT_TYPE = 1;

    // A constant for records aligned to right
    public static final int ALIGN_RIGHT_TYPE = 0;

    // Define a tag for logging
    private static final String TAG = TranslationRecordAdapter.class.getClass().getSimpleName();


    public TranslationRecordAdapter(Activity context, List<String> list) {
        // Constructor
        //
        // @param context Activity
        // @param list List<String>

        // Pass initialization works to super class
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
        // Default value
        int layoutResource = -1;

        // Determine which way should text aligned
        int viewType = this.getItemViewType(position);
        if (viewType == ALIGN_LEFT_TYPE) {
            layoutResource = R.layout.translation_record_line;
        } else if (viewType == ALIGN_RIGHT_TYPE) {
            layoutResource = R.layout.translation_record_line;
        }

        // References as stated in variable name
        ViewHolder viewHolder;
        TextView textView;

        // Determine whether convertView existed or not
        if (convertView != null) {
            // Reuse element inside convertView
            viewHolder = (ViewHolder) convertView.getTag();

            // Setup the reference for later process
            textView = viewHolder.textView;
        } else {
            // Get the inflater reference
            Context ctx = this.getContext();
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );

            // Create a view with elements defined by our XML
            convertView = inflater.inflate(layoutResource, parent, false);

            // Find the TextView
            textView = (TextView) convertView.findViewById(R.id.recordLine);

            // Save them in our viewHolder
            viewHolder = new ViewHolder();
            viewHolder.textView = textView;

            // Save our holder into to view element for later run
            convertView.setTag(viewHolder);
        }

        // Customize gravity option depends on the view type
        if (viewType == ALIGN_RIGHT_TYPE) {
            textView.setGravity(Gravity.END | Gravity.BOTTOM);
        } else if (viewType == ALIGN_LEFT_TYPE) {
            textView.setGravity(Gravity.START | Gravity.BOTTOM);
        }

        // Show our translation record
        textView.setText(this.getItem(position));

        return convertView;
    }
}
