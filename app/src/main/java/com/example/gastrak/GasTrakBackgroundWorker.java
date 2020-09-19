package com.example.gastrak;

// Jacob Pauls
// 300273666
// GasTrakBackgroundWorker.java

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Map;

public class GasTrakBackgroundWorker extends Worker {

    // hold name
    private static final String TAG = "GTBackgroundWorker";

    // construct the worker object
    public GasTrakBackgroundWorker (
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Doing work through work manager.");
        // retrieve data
        String price = getInputData().getString("price");
        String gasStation = getInputData().getString("gasStation");
        String placeId = getInputData().getString("placeId");
        // so that the work manager doesn't trigger without a placeId
        // NOTE: checkOperation fails if application is in the background. tried my best to circumvent this to no avail.
        if (placeId != null) {
            Log.d(TAG, "doWork: Notification Data: Price: " + price + " -> Gas Station: " + gasStation + " -> PlaceId: " + placeId);
            Log.d(TAG, "doWork: Executing operation. Check onReceive and onHandleIntent for operation results.");
            checkOperation(placeId, price);
        }
        return Result.success();
    }

    // method to check the status of the gas station
    private void checkOperation(String placeId, String price) {
        MySQLController mySQLController = new MySQLController(ContextWorkaround.getContext());
        mySQLController.checkIfGasStationMeetsPriceCondition(placeId, price);
    }
}
