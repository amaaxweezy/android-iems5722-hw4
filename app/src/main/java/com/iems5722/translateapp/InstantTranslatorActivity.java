package com.iems5722.translateapp;

import android.app.Activity;
import android.os.Bundle;
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

    private static final String TAG = InstantTranslatorActivity.class.getClass().getSimpleName();

    // A reference for our adapter so that we can still get it back after onCreate()
    protected TranslationRecordAdapter myAdapter;

    // A reference for our listView so that we can still get it back after onCreate()
    protected ListView myListView;

    // A reference for our inputBox
    protected EditText myInputBox;

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

                // Send request to the online word dictionary
                AnotherWordDictionary myDictionary = new AnotherWordDictionary(
                        this,
                        inputTxt
                );

                // Start query in asynchronous way
                myDictionary.execute();

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
