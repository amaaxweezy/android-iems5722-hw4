package com.iems5722.translateapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by mondwan on 17/1/15.
 */
public class TranslateErrorDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        String errorMsg = this.getArguments().getString("errorMsg");

        builder.setTitle("Translation Error");
        builder.setMessage(errorMsg);
        builder.setPositiveButton(
                R.string.my_translate_error_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
