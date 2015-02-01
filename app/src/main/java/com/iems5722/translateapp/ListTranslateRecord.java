package com.iems5722.translateapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class ListTranslateRecord extends ListActivity {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    protected ArrayAdapter<String> adapter;

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
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("Item pos |%d| id|%d|", position, id));
                }

                return true;
            }
        });
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
