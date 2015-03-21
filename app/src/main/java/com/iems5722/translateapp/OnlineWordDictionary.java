package com.iems5722.translateapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

public class OnlineWordDictionary extends AsyncTask<Void, Void, HashMap<String, String>> {

    private static final String TAG = MainActivity.class.getClass().getSimpleName();

    // Default protocol will be TCP
    protected String myProtocol = "TCP";

    // Caller Activity reference
    protected MainActivity activity = null;

    // Store user input which will be looked up later
    protected String inputTxt = "";

    // A boolean indicate there are any errors related to the server
    protected boolean isServerError = false;

    // An error message will be shown on dialog in case there are errors
    protected String errorMessage = "";

    public OnlineWordDictionary(MainActivity mainActivity, String protocol, String inputTxt) {
        // Constructor of the OnlineWordDictionary
        //
        // @param protocol String
        //   Should be either TCP or HTTP
        //
        // @param inputTxt String
        //
        //   URL encoded text which going to query.
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

        HashMap<String, String> ret;

        if (this.myProtocol.equals("TCP")) {
            // TCP
            ret = this.myTCPLookUp();
        } else {
            // HTTP
            ret = this.myHTTPLookUp();
        }

        return ret;
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

            String translatedTxt = map.get(this.inputTxt);

            outputStream.write(String.format(
                    "%s: %s\n",
                    this.inputTxt,
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

        String inputTxt = this.inputTxt;

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

    protected HashMap<String, String> myTCPLookUp() {
        // Look up dictionary via TCP

        HashMap<String, String> ret = new HashMap<>();

        // Do TCP connection here
        String inputTxt = this.inputTxt;

        String host = "iems5722v.ie.cuhk.edu.hk";
        int port = 3001;

        try {
            Socket s = new Socket(host, port);

            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("Connect to |%s|", host));
            }

            DataOutputStream outs = new DataOutputStream(s.getOutputStream());

            // Fill in true to flush output stream
            PrintWriter myWriter = new PrintWriter(outs, true);
            myWriter.println(this.inputTxt);

            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("Send data |%s| to |%s|", inputTxt, host));
            }

            BufferedReader res = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String translatedTxt = res.readLine();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("Received data |%s| from |%s|", translatedTxt, host));
            }

            // Make dictionary only if server translate successfully
            if (!translatedTxt.matches(".*Error.*")) {
                ret.put(inputTxt, translatedTxt);
            }

            res.close();

        } catch (IOException e) {
            Log.e(TAG, String.format("Server error |%s|", e.getMessage()));

            this.isServerError = true;
            this.errorMessage = e.getMessage();
        }

        return ret;
    }

    protected void onPostExecute(HashMap<String, String> myMap) {
//        // Save Translation
//        this.saveTranslation(myMap);


        if (this.isServerError) {
            this.activity.showTranslateErrorDialog(this.errorMessage);
        } else {

            try {
                String inputTxt = this.inputTxt;

                // Update UI according with the given map
                String outputTxt = myMap.get(inputTxt);

                inputTxt = URLDecoder.decode(inputTxt, "UTF-8");

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("Mapping |%s| -> |%s|", inputTxt, outputTxt));
                }

                if (outputTxt == null) {
                    // Invalid translation
                    outputTxt = String.format("Input |%s| cannot be translated", inputTxt);
                    this.activity.showTranslateErrorDialog(outputTxt);
                } else {
                    // Correct translation
                    this.activity.addTranslationRecord(inputTxt, outputTxt);
                }
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
}
