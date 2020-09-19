package com.example.gastrak;

// Jacob Pauls
// 300273666
// MySQLController.java

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

public class MySQLController {

    // hold name
    private static final String TAG = "MySQLController";

    // hold opCode for requests
    private int opCode;
    // hold context
    private Context context;

    public MySQLController(Context context) {
        this.context = context;
    }

    // --------------------------- Basic Operations (GET, POST, PUT) --------------------------- //

    public void refreshGasStationFromMySQLDatabase(String placeId) {
        Log.d(TAG, "refreshGasStationFromMySQLDatabase: Retrieval called. Intent service started.");
        // code to define insert operation
        opCode = 101;
        // construct intent and start service
        Intent intent = new Intent(context, MySQLDataRetrieval.class);
        intent.putExtra("opCode", opCode);
        intent.putExtra("passedId", placeId);
        context.startService(intent);
    }

    public void insertGasStationToMySQLDatabase(String placeId, String price, String user) {
        Log.d(TAG, "insertGasStationToMySQLDatabase: Insertion called. Intent service started.");
        // code to define insert operation
        opCode = 102;
        // construct intent and start service
        String[] requiredColumns = {"PlaceId", "UploadedPrice", "User"};
        String[] passedData = {placeId, price, user};
        Intent intent = new Intent(context, MySQLDataRetrieval.class);
        intent.putExtra("opCode", opCode);
        intent.putExtra("columns", requiredColumns);
        intent.putExtra("insertData", passedData);
        context.startService(intent);
    }

    public void updateGasStationInMySQLDatabase(String placeId, String price, String user) {
        Log.d(TAG, "updateGasStationInMySQLDatabase: Update called. Intent service started");
        // code to define update operation
        opCode = 103;
        // construct intent and start service
        String[] requiredColumns = {"UploadedPrice", "User"};
        String[] updateData = {price, user};
        Intent intent = new Intent(context, MySQLDataRetrieval.class);
        intent.putExtra("opCode", opCode);
        intent.putExtra("columns", requiredColumns);
        intent.putExtra("passedId", placeId);
        intent.putExtra("updateData", updateData);
        context.startService(intent);
    }

    // --------------------------- Misc. Operations --------------------------- //

    // operation for custom notifications
    public void checkIfGasStationMeetsPriceCondition(String placeId, String price) {
        Log.d(TAG, "checkIfGasStationMeetsPriceCondition: Check called. Intent service started.");
        // code to define check operation
        opCode = 104;
        // construct intent and start service
        Intent intent = new Intent(context, MySQLDataRetrieval.class);
        intent.putExtra("opCode", opCode);
        intent.putExtra("passedId", placeId);
        intent.putExtra("price", price);
        if (isGasTrakOpen(context)) {
            Log.d(TAG, "checkIfGasStationMeetsPriceCondition: gasTRAK is running in the foreground. Service started.");
            context.startService(intent);
        } else {
            Log.d(TAG, "checkIfGasStationMeetsPriceCondition: gasTRAK is running in the background. Default notification sent.");
            // gastrak is in the background, pass default notification
            String gasStation = MapsActivity.retrieveGasNameWithPlaceId(placeId);
            GasTrakNotificationManager gtNotification = new GasTrakNotificationManager(context, 0);
            gtNotification.sendDefaultNotification(gasStation);
        }
    }

    // check status (if app is in background the worker cannot pass context correctly)
    private boolean isGasTrakOpen(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String appPackage = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(appPackage)) {
                return true;
            }
        }
        return false;
    }

}
