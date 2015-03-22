package com.iems5722.translateapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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

    // A reference to the db helper
    protected InstantTranslatorActivity.DatabaseHelper myDbHelper;

    public void setQueryWords(String queryWords) {
        // Setter for queryWords
        //
        // @param queryWords String
        //
        //   URL encoded text which going to query.
        //
        this.queryWords = queryWords;
    }

    public AnotherWordDictionary(
            InstantTranslatorActivity activity, InstantTranslatorActivity.DatabaseHelper myDbHelper
    ) {
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

        // Initialize database helper
        this.myDbHelper = myDbHelper;
    }

    protected HashMap<String, String> doInBackground(Void... voids) {
        // Translate given queryWords
        //
        // @return HashMap<String, String>

        // A reference which stores the operation result
        HashMap<String, String> ret;

        // Lookup local database for given queryWord first
        ret = this.myDbLookup();

        // Checkout whether we have such translation or not
        if (ret == null) {
            // If not, lookup from server
            ret = this.myHTTPLookUp();

            // Checkout the translation
            String translation = ret.get(this.queryWords);

            // Save the translation to our database if this is not null
            if (translation != null) {
                this.saveTranslation(this.queryWords, translation);
            }
        }

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

    protected boolean saveTranslation(String queryWords, String translation) {
        // Helper method for saving translation into our database
        //
        // @param String queryWords
        //
        // @param String translation
        //
        // @return boolean

        boolean ret;

        SQLiteDatabase db = this.myDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InstantTranslatorActivity.MainTable.COLUMN_NAME_QUERY_WORDS, queryWords);
        values.put(InstantTranslatorActivity.MainTable.COLUMN_NAME_TRANSLATION, translation);

        long rowID = db.insert(InstantTranslatorActivity.MainTable.TABLE_NAME, null, values);

        if (rowID > 0) {
            ret = true;
            if (BuildConfig.DEBUG) {
                Log.d(
                        TAG,
                        String.format(
                                "Successfully insert translation |%s| -> |%s| into database",
                                queryWords,
                                translation
                        )
                );
            }
        } else {
            ret = false;
            Log.e(
                    TAG,
                    String.format(
                            "Failure to insert translation |%s| -> |%s| into database",
                            queryWords,
                            translation
                    )
            );
        }

        return ret;
    }

    protected HashMap<String, String> myDbLookup() {
        // Lookup queryWords from local database
        //
        // @return HashMap<String, String>
        //
        //   key -> queryWords, value -> translation
        //
        //   NOTE return NULL if there is no such record in the database

        // A reference which stores the operation result
        HashMap<String, String> ret = null;

        // A reference to the queryWords
        String queryWords = this.queryWords;

        // Specify columns we want for later query
        String[] projection = {InstantTranslatorActivity.MainTable.COLUMN_NAME_TRANSLATION};

        // Speciify filter which is COLUMN querwords equals to OUR querywords
        String whereClause = String.format(
                "\"%s\"=\"%s\"",
                InstantTranslatorActivity.MainTable.COLUMN_NAME_QUERY_WORDS,
                queryWords
        );

        // Instantiate a builder for SQL query
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Define the table we are going to search
        qb.setTables(InstantTranslatorActivity.MainTable.TABLE_NAME);

        // Fetch database reference
        SQLiteDatabase db = this.myDbHelper.getReadableDatabase();

        // Search
        Cursor cursor = qb.query(db, projection, whereClause, null, null, null, null, null);

        // It will be true if there is such record
        boolean isSuccess = cursor.moveToFirst();

        if (isSuccess) {
            // There is a record for such translation

            // Get back the column index from column name
            int columnIndex = cursor.getColumnIndex(
                    InstantTranslatorActivity.MainTable.COLUMN_NAME_TRANSLATION
            );

            // Get back the translation
            String translation = cursor.getString(columnIndex);

            // Create a HashMap for saving result
            ret = new HashMap<>();
            ret.put(queryWords, translation);
        }

        // Close the connection
        cursor.close();

        return ret;
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
