package com.iems5722.translateapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;


public class ListTranslateRecord extends ListActivity {

    protected ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{"a", "b"}
        );

        this.setListAdapter(this.adapter);
    }
}
