package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: ConfirmDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ConfirmDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "ConfirmDialog";

    // variables
    private TextView stationTV;
    private TextView priceTV;
    private TextView distanceTV;
    private TextView addressTV;

    // passed values
    public static String passedName;
    public static String passedAddress;
    public static String passedPrice;
    public static String passedDistance;

    // setters for passed values
    public static void setPassedName(String name) { passedName = name; }
    public static void setAddress(String address) {
        passedAddress = address;
    }
    public static void setPrice(String price) {
        passedPrice = price;
    }
    public static void setDistance(String distance) {
        passedDistance = distance;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // inflate the view so it can be passed to the dialog
        LayoutInflater inflate = getActivity().getLayoutInflater();
        View view  = inflate.inflate(R.layout.layout_dialog_confirm, null);

        // build dialog
        build.setView(view)
                .setTitle("Confirm Trip:")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MidTripDialog dialog = new MidTripDialog();
                        dialog.show(getFragmentManager(), "MidTripDialog Dialog");
                        // set the data in the final dialog
                        TripDialog.setPassedName(passedName);
                        TripDialog.setAddress(passedAddress);
                    }
                });
        // assign places to store value
        stationTV = view.findViewById(R.id.stationNameTV);
        addressTV = view.findViewById(R.id.stationAddressTV);
        priceTV = view.findViewById(R.id.priceTV);
        distanceTV = view.findViewById(R.id.distanceTV);
        // retrieve data
        stationTV.setText(passedName);
        addressTV.setText(passedAddress);
        priceTV.setText(passedPrice);
        distanceTV.setText(passedDistance);

        // return dialog
        return build.create();
    }
}
