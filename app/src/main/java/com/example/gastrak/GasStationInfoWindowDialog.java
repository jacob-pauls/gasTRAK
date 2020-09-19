package com.example.gastrak;

// Jacob Pauls
// 300273666
// GasStationInfoWindowDialog.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GasStationInfoWindowDialog extends AppCompatDialogFragment {

    // hold name
    private static final String TAG = "GSInfoWindowDialog";

    // variables
    private EditText enteredPrice;
    private TextView gasStationName;
    private String gasStation;
    private String placeId;
    public static boolean hasNotificationBeenSet = false;
    public static WorkManager workManager;

    // get/set gas stations
    public String getGasStation() {
        return gasStation;
    }
    public void setGasStation(String gasStation) {
        this.gasStation = gasStation;
    }

    // get/set gas statin id
    public String getPlaceId() {
        return placeId;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        // inflate the view so it can be passed to the dialog
        LayoutInflater inflate = getActivity().getLayoutInflater();
        View view  = inflate.inflate(R.layout.layout_dialog_gas_station_info_window, null);

        // set EditText
        enteredPrice = (EditText) view.findViewById(R.id.priceDialogET);

        // set gas station name
        gasStationName = (TextView) view.findViewById(R.id.gasStationName);
        String gasStationNameAssignment = getGasStation();
        if (gasStationNameAssignment.equals("")) {
            gasStationNameAssignment = "ERROR: PLEASE CANCEL";
            gasStationName.setText(gasStationNameAssignment);
        } else {
            gasStationName.setText(gasStationNameAssignment);
        }

        // build dialog
        build.setView(view)
             .setTitle("Set Gas Station Notification")
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     // do nothing
                 }
             })
             .setPositiveButton("Set Notification", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     // retrieve price
                     String price = enteredPrice.getText().toString();
                     Log.d(TAG, "GasStationInfoWindowDialog: build: price: " + price);

                     // check placeId
                     Log.d(TAG, "GasStationInfoWindowDialog: placeId: " + getPlaceId());

                     // set constraints for the worker
                     Constraints contstraints = new Constraints.Builder()
                             // work will only execute if battery isn't low
                             .setRequiresBatteryNotLow(true)
                             .build();

                     // pass data to the worker
                     Data inputData = new Data.Builder()
                             .putString("placeId", getPlaceId())
                             .putString("price", price)
                             .putString("gasStation", getGasStation())
                             .build();

                     // set a periodic work request to occur every 15 minutes (under this will not occur)
                     PeriodicWorkRequest notificationRequest = new PeriodicWorkRequest.Builder(GasTrakBackgroundWorker.class, 15, TimeUnit.MINUTES)
                             .setConstraints(contstraints)
                             .setInputData(inputData)
                             .build();

                     // verify the ID
                     UUID notificationRequestId = notificationRequest.getId();
                     String idString = notificationRequestId.toString();
                     Log.d(TAG, "idString: " + idString);

                     // instantiate instance and queue work
                     workManager = WorkManager.getInstance();
                     // if a notification has been set, clear all work (only run one job at a time)
                     if (hasNotificationBeenSet) {
                         workManager.cancelAllWork();
                     }
                     workManager.enqueue(notificationRequest);
                     hasNotificationBeenSet = true;
                 }
             });
        return build.create();
    }
}
