package com.iems5722.translateapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get references to layout objects

        // add click listener to button to call translateText()
        Button translateButton = (Button) this.findViewById(R.id.translate_btn);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wire translateText() if the button has been clicked
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Translate button has been clicked");
                }
                MainActivity.this.translateText();
            }
        });
    }

    // translate look up
    private void translateText() {
        // get user input
        EditText translateEdt = (EditText) this.findViewById(R.id.translate_edt);
        String inputTxt = translateEdt.getText().toString();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("User input %s", inputTxt));
        }

        // try get word out of dictionary
        WordDictionary myDictionary = new WordDictionary();
        Map<String, String> myMap = myDictionary.getDictionary();
        String outputTxt = myMap.get(inputTxt);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Mapping |%s| -> |%s|", inputTxt, outputTxt));
        }

        // show some feedback to user: translated text, error message, dialog etc

        if (inputTxt.equals("")) {
            // Empty input
            outputTxt = "Input is empty";
            this.showTranslateErrorDialog(outputTxt);
        } else if (outputTxt == null) {
            // Invalid translation
            outputTxt = String.format("Input |%s| cannot be translated", inputTxt);
            this.showTranslateErrorDialog(outputTxt);
        } else {
            // Correct translation

        }
    }

    protected void showTranslateErrorDialog(String err) {
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
