package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: VehicleProfileDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class VehicleProfileDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "VehicleProfileDialog";

    // variables
    private EditText vehicleName;
    private EditText vehicleMake;
    private EditText vehicleModel;
    private EditText vehicleYear;
    private EditText vehicleFE;
    private EditText vehicleMaxTank;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // inflate the view so it can be passed to the dialog
        LayoutInflater inflate = getActivity().getLayoutInflater();
        View view  = inflate.inflate(R.layout.layout_dialog_vehicle_profile, null);

        // set EditTexts
        vehicleName = view.findViewById(R.id.vehicleNameET);
        vehicleMake = view.findViewById(R.id.vehicleMakeET);
        vehicleModel = view.findViewById(R.id.vehicleModelET);
        vehicleYear = view.findViewById(R.id.vehicleYearET);
        vehicleFE = view.findViewById(R.id.vehicleFEET);
        vehicleMaxTank = view.findViewById(R.id.vehicleTankET);

        // build dialog
        build.setView(view)
                .setTitle("Add Vehicle Profile:")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // retrieve the values
                        String name = vehicleName.getText().toString();
                        String make = vehicleMake.getText().toString();
                        String model = vehicleModel.getText().toString();
                        String year = vehicleYear.getText().toString();
                        String fe = vehicleFE.getText().toString();
                        String tank = vehicleMaxTank.getText().toString();
                        Log.d(TAG, "VehicleProfileDialog: data: Name: " + name +
                                                "\n Make: " + make +
                                                "\n Model: " + model +
                                                "\n Year: " + year +
                                                "\n Fuel Efficiency: " + fe +
                                                "\n Max Tank: " + tank);
                        // insert
                        MapsActivity.insertVehicleEntry(name, make, model, year, fe, tank);
                    }
                });
        return build.create();
    }
}
