package com.iems5722.translateapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define translate button onClick handler
        View.OnClickListener onTranslateButtonClickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the button reference
                Button btn = (Button) v;

                // Get the name of that button
                String nameOfBtn = btn.getText().toString();

                // Get protocol from analysing the name of the button
                String protocol = nameOfBtn.matches((".*TCP.*")) ? "TCP" : "HTTP";

                if (BuildConfig.DEBUG) {
                    Log.d(
                            TAG,
                            String.format("%s Translate button has been clicked", nameOfBtn)
                    );
                    Log.d(
                            TAG,
                            String.format("Protocol = |%s|", protocol)
                    );
                }

                // Wire translateText() if the button has been clicked
                MainActivity.this.translateText(protocol);
            }
        };

        // add click listener to TCP button to call translateText()
        Button TCPTranslateButton = (Button) this.findViewById(R.id.tcp_translate_btn);
        TCPTranslateButton.setOnClickListener(onTranslateButtonClickHandler);

        // add click listener to HTTP button to call translateText()
        Button HTTPTranslateButton = (Button) this.findViewById(R.id.http_translate_btn);
        HTTPTranslateButton.setOnClickListener(onTranslateButtonClickHandler);

        // add click listener to record button to call
        Button recordButton = (Button) this.findViewById(R.id.record_btn);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Translate records has been clicked");
                }

                // Wire up onClick handler to our activity method
                MainActivity.this.showTranslateRecords();
            }
        });

        // add click listener to share button ()
        Button shareButton = (Button) this.findViewById(R.id.share_btn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wire shareText() if the button has been clicked
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Share button has been clicked");
                }
                MainActivity.this.shareText();
            }
        });
    }

    private void showTranslateRecords() {
        // Delegate the workload for showing records to ListTranslateRecord

        Intent intent = new Intent(MainActivity.this, ListTranslateRecord.class);
        startActivity(intent);
    }

    private void shareText() {
        // Share translated text to other application by using INTENT

        // Get translated text
        TextView translateTxtView = (TextView) this.findViewById(R.id.translated_txt_view);
        String translatedTxt = translateTxtView.getText().toString();

        // Create a send intent
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Translated text");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, translatedTxt);
        this.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void translateText(String protocol) {
        // Delegate workloads for looking up dictionary to Class::OnlineWordDictionary with
        // given protocol
        //
        // @param protocol String

        // get user input
        EditText translateEdt = (EditText) this.findViewById(R.id.translate_edt);
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
            // Send request to the online word dictionary
            OnlineWordDictionary myDictionary = new OnlineWordDictionary(this, protocol, inputTxt);
            myDictionary.execute();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
