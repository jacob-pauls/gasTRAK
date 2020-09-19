package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: MidTripDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class MidTripDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "MidTripDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // build dialog
        build.setTitle("Now Driving...")
                .setMessage("Put your phone down. Please watch the road.")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TripDialog dialog = new TripDialog();
                        dialog.show(getFragmentManager(), "Trip Dialog");
                    }
                });
        // return dialog
        return build.create();
    }
}
