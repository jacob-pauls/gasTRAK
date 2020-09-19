package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: DatabaseErrorDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class DatabaseErrorDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "DatabaseErrorDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // alert dialog to handle if a gas station is not in the database
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        build.setTitle("WARNING")
                .setMessage("This station was not found. Please enter in more specific information and try again.")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .show();
        return build.create();
    }
}
