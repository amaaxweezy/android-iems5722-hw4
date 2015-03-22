package com.iems5722.translateapp;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class InstantTranslatorActivity extends Activity {
    // An activity implement a translator with instant chat like style

    private static final String TAG = InstantTranslatorActivity.class.getClass().getSimpleName();

    public static final class MainTable implements BaseColumns {
        // A concrete contract class which defines the layout of our database

        // This class cannot be instantiate
        private MainTable() {
            // Empty
        }

        // Define the name of the table
        public static final String TABLE_NAME = "translation";

        // Define the column querywords
        public static final String COLUMN_NAME_QUERY_WORDS = "querywords";

        // Define the column translation
        public static final String COLUMN_NAME_TRANSLATION = "translation";
    }

    protected static class DatabaseHelper extends SQLiteOpenHelper {
        // A concrete class for dealing with SQLite database easily

        // Define the name of our database
        public static final String DATABASE_NAME = "Translator.db";

        // Define the version of our database
        public static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the data table

            db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " ("
                    + MainTable._ID + " INTEGER PRIMARY KEY,"
                    + MainTable.COLUMN_NAME_QUERY_WORDS + " TEXT" + ","
                    + MainTable.COLUMN_NAME_TRANSLATION + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Not sure what is this :D

            // Logs that the database is being upgraded
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);

            // Recreates the database with a new version
            onCreate(db);
        }
    }

    // A reference for our adapter so that we can still get it back after onCreate()
    protected TranslationRecordAdapter myAdapter;

    // A reference for our listView so that we can still get it back after onCreate()
    protected ListView myListView;

    // A reference for our inputBox
    protected EditText myInputBox;

    // A reference to the Dictionary
    protected AnotherWordDictionary myDictionary;

    // A reference to the dbHelper
    protected DatabaseHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ask super class to do initialization first
        super.onCreate(savedInstanceState);

        // Define which layout we are going to use
        setContentView(R.layout.instant_translation);

        // Define a empty string list at first
        List<String> translationRecords = new ArrayList<>();

        // Instantiate an adapter
        this.myAdapter = new TranslationRecordAdapter(this, translationRecords);

        // Get the reference for our list view
        this.myListView = (ListView) findViewById(R.id.translationRecords);

        // Attach the adapter to our view
        this.myListView.setAdapter(myAdapter);

        // Get user input
        this.myInputBox = (EditText) this.findViewById(R.id.inputBox);

        // Instantiate the dbHelper
        this.myDbHelper = new DatabaseHelper(this);

        // add click listener to submit button to call
        Button submitButton = (Button) this.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Submit button has been clicked");
                }
                InstantTranslatorActivity.this.translateText();
            }
        });
    }

    @Override
    protected void onResume() {
        // Ask super class to work first
        super.onResume();

        // Get an instance from the library for parsing the intent
        ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);

        // Further process if this activity was started by ACTION_SEND intent
        if (intentReader.isSingleShare()) {
            // Get words from intent
            String s = intentReader.getText().toString();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("Shared string inside an intent: |%s|", s));
            }

            // Write word into our inputBox
            this.myInputBox.setText(s);

            // Force cursor at the end of the word
            this.myInputBox.setSelection(this.myInputBox.getText().length());
        }

    }

    protected void translateText() {
        // Delegate workloads to Class::AnotherWordDictionary

        // Get user input
        String inputTxt = this.myInputBox.getText().toString();

        // A reference to the output text
        String outputTxt;

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("User input %s", inputTxt));
        }

        // Clear the input box
        this.myInputBox.setText("");

        // Determine whether the given input is empty or not
        if (inputTxt.equals("")) {
            // Input is empty
            outputTxt = "Input is empty";
            this.showTranslateEmptyToast(outputTxt);

        } else {
            // Input is not empty
            try {
                // Trim text first
                inputTxt = inputTxt.trim();

                // Encode input text in URL format
                inputTxt = URLEncoder.encode(inputTxt, "UTF-8");

                // Instantiate the Dictionary
                //
                // NOTE: It must be created again since thread object cannot be reused
                this.myDictionary = new AnotherWordDictionary(this, this.myDbHelper);

                // Set query words
                this.myDictionary.setQueryWords(inputTxt);

                // Start query in asynchronous way
                this.myDictionary.execute();

            } catch (UnsupportedEncodingException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Input text cannot be encoded in URL format");
                }
            }
        }
    }

    protected void showTranslateEmptyToast(String err) {
        // Pop up a toast with message err
        //
        // @param err String

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this.getApplicationContext(), err, duration);
        toast.show();
    }

    public void showTranslateErrorDialog(String err) {
        // Show error box with given message
        //
        // @param err String

        Bundle args = new Bundle();
        args.putString("errorMsg", err);
        TranslateErrorDialog dialog = new TranslateErrorDialog();
        dialog.setArguments(args);
        dialog.show(this.getFragmentManager(), "translationErrors");
    }

    public void showTranslationRecord(String input, String output) {
        // An API for showing what words have been queried and the replies from server
        //
        // @param input string
        // @param output string

        // Add query words firstly
        this.myAdapter.add(input);

        // Add replies secondly
        this.myAdapter.add(output);

        // Notify the adapter to update listView
        this.myAdapter.notifyDataSetChanged();

        // Always show the bottom (latest) line
        this.myListView.setSelection(this.myAdapter.getCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instant_translator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
