package com.iems5722.translateapp;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class ListTranslateRecord extends ListActivity
        implements DeleteRecordConfirmDialog.ClickListener {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    protected ArrayAdapter<String> adapter;

    protected int selectedRowPosition = -1;

    public void setSelectedRowPosition(int selectedRowPosition) {
        this.selectedRowPosition = selectedRowPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data from reading file
        ArrayList<String> items = this.readTranslationRecord();

        // Bind those data to the adapter
        this.adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        // Bind the adapter to our activity
        this.setListAdapter(this.adapter);

        // Register a long click handler on our listview
        ListView view = this.getListView();
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(
                    AdapterView<?> parent, View view, int position, long id
            ) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("Item pos |%d| id|%d|", position, id));
                }

                ListTranslateRecord.this.setSelectedRowPosition(position);

                DeleteRecordConfirmDialog dialog = new DeleteRecordConfirmDialog();
                dialog.show(ListTranslateRecord.this.getFragmentManager(), "deleteRecordConfirm");

                return true;
            }
        });
    }

    public void confirmDeleteHandler(DialogFragment dialog) {
        // Confirm delete handler. It will going to delete the selected record.
        //
        // @param dialog DialogFragment

        if (BuildConfig.DEBUG) {
            Log.d(
                    TAG,
                    String.format(
                            "Confirm delete row with position |%d|",
                            this.selectedRowPosition
                    )
            );
        }

        // Get the target row
        String targetRow = this.adapter.getItem(this.selectedRowPosition);

        // Delete that row
        this.adapter.remove(targetRow);

        // Update text files
        this.updateTranslationRecord();
    }

    protected void updateTranslationRecord() {
        // Helper method for updating translation records

        String fileName = "translate_record";
        try {
            FileOutputStream outputStream = this.openFileOutput(
                    fileName, Context.MODE_PRIVATE);

            for (int i = 0; i < adapter.getCount(); i++) {
                String rowTxt = adapter.getItem(i);

                outputStream.write(String.format("%s\n", rowTxt).getBytes());
            }
            outputStream.close();

        } catch (IOException e) {
            Log.e(TAG, String.format("Update error |%s|", e.getMessage()));
        }
    }

    protected ArrayList<String> readTranslationRecord() {
        // Helper method for reading translation records
        //
        // @return ArrayList<String>

        ArrayList<String> ret = new ArrayList<>();
        String fileName = "translate_record";

        try {
            FileInputStream inputStream = openFileInput(fileName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                ret.add(line);
            }

            reader.close();
        } catch (java.io.IOException e) {
            Log.e(TAG, String.format("Opening record error |%s|", e.getMessage()));
        }

        return ret;
    }
}
