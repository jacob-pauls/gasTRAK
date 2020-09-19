package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: ViewVehicleProfileDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ViewVehicleProfileDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "VehicleProfileDialog";

    // boolean to check if there is a default profile
    private boolean isDefaultProfileSet = false;

    // variables
    private TextView vehicleName;
    private TextView vehicleMake;
    private TextView vehicleModel;
    private TextView vehicleYear;
    private TextView vehicleFE;
    private TextView vehicleTankSize;

    // data
    public static String passedId;
    public static String passedName;
    public static String passedMake;
    public static String passedModel;
    public static String passedYear;
    public static String passedFE;
    public static String passedTankSize;

    // setters
    public static void setVehicleId(String Id) {
        passedId = Id;
    }
    public static void setVehicleName(String name) {
        passedName = name;
    }
    public static void setVehicleMake(String make) {
        passedMake = make;
    }
    public static void setVehicleModel(String model) {
        passedModel = model;
    }
    public static void setVehicleYear(String year) {
        passedYear = year;
    }
    public static void setVehicleFE (String fe) {
        passedFE = fe;
    }
    public static void setVehicleTankSize (String tank) {
        passedTankSize = tank;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // inflate the view so it can be passed to the dialog
        LayoutInflater inflate = getActivity().getLayoutInflater();
        View view  = inflate.inflate(R.layout.layout_dialog_view_vehicle_profile, null);
        // build dialog
        build.setView(view)
                .setTitle("Vehicle Profile:")
                .setPositiveButton("Make Default Profile", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // replace as default
                        isDefaultProfileSet = true;
                        MapsActivity.updateVehicleEntry(passedId);
                    }
                })
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
        // assign places to store data
        vehicleName = view.findViewById(R.id.vehicleNameTV);
        vehicleMake = view.findViewById(R.id.vehicleMakeTV);
        vehicleModel = view.findViewById(R.id.vehicleModelTV);
        vehicleYear = view.findViewById(R.id.vehicleYearTV);
        vehicleFE = view.findViewById(R.id.vehicleFETV);
        vehicleTankSize = view.findViewById(R.id.tankSizeTV);
        // pass data
        vehicleName.setText(passedName);
        vehicleMake.setText(passedMake);
        vehicleModel.setText(passedModel);
        vehicleYear.setText(passedYear);
        vehicleFE.setText(passedFE);
        vehicleTankSize.setText(passedTankSize);
        return build.create();
    }
}
