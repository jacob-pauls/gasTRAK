package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: ProfileErrorDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;

public class ProfileErrorDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "ProfileErrorDialog";

    // build dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // alert dialog to handle if a gas station is not in the database or if a default profile is not set
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        Log.d(TAG, "ProfileErrorDialog: boolean status: " + MapsActivity.checkForProfile());
        build.setTitle("WARNING")
                .setMessage("You have not created a vehicle profile or set a current vehicle profile as a default. gasTRAK uses vehicle data to " +
                        "perform trips and check stations! Please create a vehicle profile and set it as the default profile" +
                        " to enable the app's main functions!")
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
