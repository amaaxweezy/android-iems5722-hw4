package com.iems5722.translateapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

public class AnotherWordDictionary extends AsyncTask<Void, Void, HashMap<String, String>> {

    private static final String TAG = OnlineWordDictionary.class.getClass().getSimpleName();

    // Caller Activity reference
    protected InstantTranslatorActivity activity = null;

    // Store user input which will be looked up later
    protected String queryWords = "";

    // A boolean indicate there are any errors related to the server
    protected boolean isServerError = false;

    // An error message will be shown on dialog in case there are errors
    protected String errorMessage = "";

    public void setQueryWords(String queryWords) {
        // Setter for queryWords
        //
        // @param queryWords String
        //
        //   URL encoded text which going to query.
        //
        this.queryWords = queryWords;
    }

    public AnotherWordDictionary(InstantTranslatorActivity activity) {
        // Constructor of the OnlineWordDictionary
        //
        // @param activity InstantTranslatorActivity
        //
        //   A reference for us to modify UI component
        //
        // @NOTE: If filling in protocol other than TCP or HTTP, assume running with TCP

        // Call super class constructor for initializing this instance
        super();

        // Store activity reference
        this.activity = activity;
    }

    protected HashMap<String, String> doInBackground(Void... voids) {
        // Fetch OnlineWordDictionary depends on the protocol
        //
        // @param urls Array of URL
        //
        // @return HashMap<String, String>

        HashMap<String, String> ret;

        ret = this.myHTTPLookUp();

        return ret;
    }

    protected void onPostExecute(HashMap<String, String> myMap) {
        // Show server's reply on UI component
        //
        // @param myMap HashMap<String, String>
        //
        //   A key-value pair map where key is the queryWords while the value is the
        //   translated_words
        //
        //   NOTE: queryWords are encoded in URL format

//        // Save Translation
//        this.saveTranslation(myMap);

        // Checkout whether we got an error from server or not
        if (this.isServerError) {
            // Show error dialog if there are errors somehow
            this.activity.showTranslateErrorDialog(this.errorMessage);
        } else {
            // Update the translation's list view
            try {
                // Get the queryWords
                String query_words = this.queryWords;

                // Update UI according with the given map
                String translation = myMap.get(query_words);

                // Decode the URL format
                query_words = URLDecoder.decode(query_words, "UTF-8");

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("Mapping |%s| -> |%s|", query_words, translation));
                }

                // Override the translation if server is not able to do translation
                if (translation == null) {
                    // Invalid translation
                    translation = String.format("Input |%s| cannot be translated", query_words);
                }

                // Update translation's list view
                this.activity.showTranslationRecord(query_words, translation);
            } catch (UnsupportedEncodingException e) {
                this.activity.showTranslateErrorDialog(e.getMessage());
            }
        }
    }

    protected void onCancelled(HashMap<String, String> map) {
        // Handler for cancelling the online request
        String outputTxt = "You have cancelled the lookup request";
        this.activity.showTranslateErrorDialog(outputTxt);
    }

    protected void saveTranslation(HashMap<String, String> map) {
        // Helper method for saving translation
        //
        // @param map HashMap<String, String>
        //   <input, output>

        String fileName = "translate_record";
        try {
            FileOutputStream outputStream = this.activity.openFileOutput(
                    fileName, Context.MODE_APPEND);

            String translatedTxt = map.get(this.queryWords);

            outputStream.write(String.format(
                    "%s: %s\n",
                    this.queryWords,
                    translatedTxt == null ? "Translate Error" : translatedTxt
            ).getBytes());

            outputStream.close();

        } catch (IOException e) {
            Log.e(TAG, String.format("Saving error |%s|", e.getMessage()));
        }
    }

    protected HashMap<String, String> myHTTPLookUp() {
        // Look up dictionary via HTTP

        HashMap<String, String> ret = new HashMap<>();

        String inputTxt = this.queryWords;

        try {
            URL serverURL = new URL(
                    String.format(
                            "http://192.168.1.113:8080/?words=%s",
                            inputTxt
                    )
            );
            HttpURLConnection urlSocket = (HttpURLConnection) serverURL.openConnection();

            try {
                InputStream in = new BufferedInputStream(urlSocket.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String response = reader.readLine();

                if (BuildConfig.DEBUG) {
                    Log.d(
                            TAG,
                            String.format(
                                    "Received data |%s| from |%s|",
                                    response,
                                    serverURL.toString()
                            )
                    );
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String message = jsonObject.getString("message");
                    String output = jsonObject.getString("output");

                    if (BuildConfig.DEBUG) {
                        Log.d(
                                TAG,
                                String.format(
                                        "json.message |%s|\n json.output |%s|",
                                        message,
                                        output
                                )
                        );
                    }

                    // Make dictionary only if server translate successfully
                    if (message.matches("OK")) {
                        ret.put(inputTxt, output);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, String.format("JSON error |%s|", e.getMessage()));

                    this.isServerError = true;
                    this.errorMessage = e.getMessage();
                } finally {
                    reader.close();
                }
            } finally {
                urlSocket.disconnect();
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, String.format("URL error |%s|", e.getMessage()));

            this.isServerError = true;
            this.errorMessage = e.getMessage();
        } catch (IOException e) {
            Log.e(TAG, String.format("Server error |%s|", e.getMessage()));

            this.isServerError = true;
            this.errorMessage = e.getMessage();
        }


        return ret;
    }
}
