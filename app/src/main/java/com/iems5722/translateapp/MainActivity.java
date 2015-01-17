package com.iems5722.translateapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

        // try get word out of dictionary

        // show some feedback to user: translated text, error message, dialog etc

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
