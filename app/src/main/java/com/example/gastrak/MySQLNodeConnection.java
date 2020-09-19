package com.example.gastrak;

// Jacob Pauls
// 300273666
// MySQLNodeConnection

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySQLNodeConnection extends BroadcastReceiver {

    // hold name
    private static final String TAG = "MySQLNodeConnection";

    // broadcast finish code
    public static final String STATUS_DONE = "OP_FINISHED";

    // hold data
    private Context context;

    public MySQLNodeConnection (Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(STATUS_DONE)) {
            int opCode = intent.getIntExtra("opCode", 404);
            String returnedData = intent.getStringExtra("returnedData");
            //Toast.makeText(context, "returnedData: " + returnedData + " occurred.", Toast.LENGTH_LONG).show();
            // perform receiver output for a GET request
            if (opCode == 101) {
                Log.d(TAG, "onReceive: returnedData: " + returnedData);
                // we only retrieve one result at a time
                try {
                    JSONArray jsonArray = new JSONArray(returnedData);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    // extract data from JSON
                    String PlaceId = jsonObject.getString("PlaceId");
                    String UploadedPrice = jsonObject.getString("UploadedPrice");
                    String User = jsonObject.getString("User");
                    String DateTimeUpdated = jsonObject.getString("DateTimeUpdated");
                    // push to database
                    MapsActivity maps = new MapsActivity();
                    maps.updateGasStationPriceInSQLite(PlaceId, UploadedPrice, User, DateTimeUpdated);
                } catch(JSONException e) {
                    Log.e(TAG, "onReceive: JSONException: " + e.getMessage());
                }
            }
            if (opCode == 104) {
                Log.d(TAG, "onReceive: returnedData: " + returnedData);
                try {
                    JSONArray jsonArray = new JSONArray(returnedData);
                    if (jsonArray.length() > 0) {
                        // do work
                        Log.d(TAG, "onReceive: Returned a result!");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String PlaceId = jsonObject.getString("PlaceId");
                        // retrieve gas station name
                        String gasStationName = MapsActivity.retrieveGasNameWithPlaceId(PlaceId);
                        String gasStationPrice = intent.getStringExtra("userEnteredPrice");
                        // send notification to the phone if this returns a result
                        GasTrakNotificationManager notificationManager = new GasTrakNotificationManager(context, 0);
                        notificationManager.sendNotification(gasStationName, gasStationPrice);
                        Log.d(TAG, "onReceive: Notification sent. Job killed.");
                        // cancel the job
                        if (GasStationInfoWindowDialog.hasNotificationBeenSet) {
                            GasStationInfoWindowDialog.workManager.cancelAllWork();
                        }
                    } else {
                        Log.d(TAG, "onReceive: Returned an empty set. Worker will sleep for 15 minutes.");
                    }
                } catch(JSONException e) {
                    Log.e(TAG, "onReceive: JSONException: " + e.getMessage());
                }
            }
        }
    }
}
