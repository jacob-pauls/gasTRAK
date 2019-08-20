package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: ProfileErrorDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class ProfileErrorDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "ProfileErrorDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // alert dialog to handle if a gas station is not in the database
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        build.setTitle("WARNING")
                .setMessage("You have not created a vehicle profile! gasTRAK uses vehicle data to " +
                            "perform trips and check stations! Please create a vehicle profile to enable the app's main functions!")
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
