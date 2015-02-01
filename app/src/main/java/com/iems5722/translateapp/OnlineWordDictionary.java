package com.iems5722.translateapp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;

public class OnlineWordDictionary extends AsyncTask<Void, Void, HashMap<String, String>> {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    // Default protocol will be TCP
    protected String myProtocol = "TCP";

    // Caller Activity reference
    protected MainActivity activity = null;

    // Store user input which will be looked up later
    protected String inputTxt = "";

    public OnlineWordDictionary(MainActivity mainActivity, String protocol, String inputTxt) {
        // Constructor of the OnlineWordDictionary
        //
        // @param protocol String
        //   Should be either TCP or HTTP
        //
        // @NOTE: If filling in protocol other than TCP or HTTP, assume running with TCP

        // Call super class constructor for initializing this instance
        super();

        // Change protocol if necessary
        this.myProtocol = protocol.equals("HTTP") ? "HTTP" : "TCP";

        // Store activity reference
        this.activity = mainActivity;

        // Store user input
        this.inputTxt = inputTxt;
    }

    protected HashMap<String, String> doInBackground(Void... voids) {
        // Fetch OnlineWordDictionary depends on the protocol
        //
        // @param urls Array of URL
        //
        // @return HashMap<String, String>

        HashMap<String, String> ret = new HashMap<String, String>();

        ret.put("one", "一");

        return ret;
    }

    protected void onPostExecute(HashMap<String, String> myMap) {
        String inputTxt = this.inputTxt;

        // Update UI according with the given map
        String outputTxt = myMap.get(inputTxt);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Mapping |%s| -> |%s|", inputTxt, outputTxt));
        }

        if (outputTxt == null) {
            // Invalid translation
            outputTxt = String.format("Input |%s| cannot be translated", inputTxt);
            this.activity.showTranslateErrorDialog(outputTxt);
        } else {
            // Correct translation
            TextView translateTxtView = (TextView)
                    this.activity.findViewById(R.id.translated_txt_view);
            translateTxtView.setText(outputTxt);
        }
    }

    protected void onCancelled(HashMap<String, String> map) {
        // Handler for cancelling the online request
        String outputTxt = "You have cancelled the lookup request";
        this.activity.showTranslateErrorDialog(outputTxt);
    }
}
