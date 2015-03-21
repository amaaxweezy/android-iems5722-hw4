package com.iems5722.translateapp;

import android.app.Activity;
import android.os.Bundle;
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

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    protected TranslationRecordAdapter myAdapter;
    protected ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instance_translation);

        // Get the reference for our list view
        this.myListView = (ListView) findViewById(R.id.translationRecords);

        // Define a empty string list at first
        List<String> translationRecords = new ArrayList<String>();

        // Instantiate an adapter
        myAdapter = new TranslationRecordAdapter(this, translationRecords);

        // Attach the adapter to our view
        myListView.setAdapter(myAdapter);

        // add click listener to submit button to call
        Button submitButton = (Button) this.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Submit button has been clicked");
                }
                MainActivity.this.translateText("HTTP");
            }
        });
    }

//    private void showTranslateRecords() {
//        // Delegate the workload for showing records to ListTranslateRecord
//
//        Intent intent = new Intent(MainActivity.this, ListTranslateRecord.class);
//        startActivity(intent);
//    }

//    private void shareText() {
//        // Share translated text to other application by using INTENT
//
//        // Get translated text
//        TextView translateTxtView = (TextView) this.findViewById(R.id.translated_txt_view);
//        String translatedTxt = translateTxtView.getText().toString();
//
//        // Create a send intent
//        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Translated text");
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, translatedTxt);
//        this.startActivity(Intent.createChooser(sharingIntent, "Share via"));
//    }

    protected void translateText(String protocol) {
        // Delegate workloads for looking up dictionary to Class::OnlineWordDictionary with
        // given protocol
        //
        // @param protocol String

        // get user input
        EditText translateEdt = (EditText) this.findViewById(R.id.inputBox);
        String inputTxt = translateEdt.getText().toString();
        String outputTxt;

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("User input %s", inputTxt));
        }

        // show some feedback to user: translated text, error message, dialog etc
        if (inputTxt.equals("")) {
            // Empty input
            outputTxt = "Input is empty";
            this.showTranslateEmptyToast(outputTxt);
        } else {
            try {
                // Trim text first
                inputTxt = inputTxt.trim();

                // Encode input text in URL format
                inputTxt = URLEncoder.encode(inputTxt, "UTF-8");

                // Send request to the online word dictionary
                OnlineWordDictionary myDictionary = new OnlineWordDictionary(
                        this,
                        protocol,
                        inputTxt
                );
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

    public void addTranslationRecord(String input, String output) {
        // An API for pushing string into our listView
        //
        // @param result String

        this.myAdapter.add(input);
        this.myAdapter.add(output);
        this.myAdapter.notifyDataSetChanged();
        this.myListView.setSelection(this.myAdapter.getCount() - 1);
    }

    public void showTranslateErrorDialog(String err) {
        // Show error box with given message if there are any errors
        //
        // @param err String


        Bundle args = new Bundle();
        args.putString("errorMsg", err);
        TranslateErrorDialog dialog = new TranslateErrorDialog();
        dialog.setArguments(args);
        dialog.show(this.getFragmentManager(), "translationErrors");
    }


    // Options menu - not needed for this app
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
