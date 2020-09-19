package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: TripDialog.java

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class TripDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "TripDialog";

    // variables
    private EditText eTGasPrice;
    private EditText etQunatity;

    // passed values
    public static String passedName;
    public static String passedAddress;

    // setters for passed values
    public static void setPassedName(String name) {
        passedName = name;
    }
    public static void setAddress(String address) {
        passedAddress = address;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create a dialog builder
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // inflate the view so it can be passed to the dialog
        LayoutInflater inflate = getActivity().getLayoutInflater();
        View view  = inflate.inflate(R.layout.layout_dialog_trip, null);
        eTGasPrice = view.findViewById(R.id.edit_gasprice);
        etQunatity = view.findViewById(R.id.edit_quantity);

        // build dialog
        build.setView(view)
                .setTitle("Trip Complete!")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String gasPrice = eTGasPrice.getText().toString();
                        String quantity = etQunatity.getText().toString();
                        Log.d(TAG, "TripDialog: data: Name: " + passedName +
                                                            "\n Address: " + passedAddress +
                                                            "\n Price: " + gasPrice +
                                                            "\n Quantity: " + quantity);
                        MapsActivity.insertTripEntry(passedName, passedAddress, gasPrice, quantity);
                    }
                });
        // return the built dialog
        return build.create();
    }

    public interface TextDialogListener {
        void submitUserEntry(String name, String address, String price, String distance);
    }
}
