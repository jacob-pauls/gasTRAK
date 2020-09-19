package com.example.gastrak;

// Jacob Pauls
// 300273666
// MySQLDataRetrieval.java

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MySQLDataRetrieval extends IntentService {

    // hold name
    private static final String TAG = "MySQLDataRetrieval";

    // hold server address ('http://PUBLIC_DNS:port/')
    // This server address slot below should point to a server-side node instance, with the specified endpoints and functionality below
    private static final String SERVER_ADDRESS = "";

    public MySQLDataRetrieval() {
        super(MySQLDataRetrieval.class.getName());
    }

    // --------------------------- Intent Handling/Parameter Building --------------------------- //
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // scoped strings
        String builtURLParameters;
        String serverAddress;
        String returnedData = "";

        // retrieve opCode
        int opCode = intent.getIntExtra("opCode", 404);
        Log.d(TAG, "onHandleIntent: opCode:" + opCode);

        // assign price for user entry
        String price = "0.00";

        // check opCode
        // 101: GET request
        if (opCode == 101) {
            String passedId = intent.getStringExtra("passedId");
            serverAddress = SERVER_ADDRESS + "showGasPrices/" + passedId;
            Log.d(TAG, "onHandleIntent: serverAddress called:" + serverAddress);
            returnedData = getDataOperation(serverAddress);
        }
        // 102: POST request
        if (opCode == 102) {
            String[] insertData = intent.getStringArrayExtra("insertData");
            builtURLParameters = buildURLParameters(insertData, intent);
            Log.d(TAG, "onHandleIntent: Constructed URL parameters: " + builtURLParameters);
            serverAddress = SERVER_ADDRESS + "postNewPrice/";
            Log.d(TAG, "onHandleIntent: serverAddress called:" + serverAddress);
            returnedData = postDataOperation(serverAddress, builtURLParameters);
        }
        // 103: PUT request
        if (opCode == 103) {
            String passedId = intent.getStringExtra("passedId");
            String[] updateData = intent.getStringArrayExtra("updateData");
            builtURLParameters = buildURLParameters(updateData, intent);
            Log.d(TAG, "onHandleIntent: Constructed URL parameters: " + builtURLParameters);
            serverAddress = SERVER_ADDRESS + "putExistingPrice/" + passedId;
            Log.d(TAG, "onHandleIntent: serverAddress called:" + serverAddress);
            returnedData = putDataOperation(serverAddress, builtURLParameters);
        }
        // 104: GET request (check for update)
        if (opCode == 104) {
            String passedId = intent.getStringExtra("passedId");
            price = intent.getStringExtra("price");
            serverAddress = SERVER_ADDRESS + "showGasPrices/" + passedId + "/" + price;
            Log.d(TAG, "onHandleIntent: serverAddress called:" + serverAddress);
            returnedData = getDataOperation(serverAddress);
        }

        // send the returned data via broadcast
        Log.d(TAG, "onHandleIntent: returnedData: " + returnedData);
        Intent broadcast = new Intent();
        broadcast.setAction(MySQLNodeConnection.STATUS_DONE);
        broadcast.putExtra("returnedData", returnedData);
        broadcast.putExtra("opCode", opCode);
        if (opCode == 104) {
            broadcast.putExtra("userEnteredPrice", price);
        }
        sendBroadcast(broadcast);
    }

    // constructs the data passed to the server
    private String buildURLParameters(String fields[], Intent intent) {
        String columns[] = intent.getStringArrayExtra("columns");
        StringBuilder stringBuilder = new StringBuilder();
        String value = null;
        for (int i = 0; i < columns.length; i++) {
            try {
                value = URLEncoder.encode(fields[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "buildURLParameters: UnsupportedEncodingException: " + e.getMessage());
            }
            // separate each parameter with '&'
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            stringBuilder.append(columns[i]).append("=").append(value);
        }
        Log.d(TAG, "buildURLParameters: returned: " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    // --------------------------- Request Methods --------------------------- //

    // --- GET Request --- //
    private String getDataOperation(String serverAddress) {
        // intialize connection
        HttpURLConnection httpURLConnection = null;
        try {
            // make connection to server
            URL url = new URL(serverAddress);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();
            int statusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "getDataOperation: statusCode: " + statusCode);
            switch (statusCode) {
                case 200:
                case 201:
                    // format returned data if necessary
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String newLine;
                    while ((newLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(newLine + "\n");
                    }
                    bufferedReader.close();
                    Log.d(TAG, "getDataOperation: String Built: " + stringBuilder.toString());
                    return stringBuilder.toString();
                case 404:
                    Log.d(TAG, "getDataOperation: getDataOperation request failed.");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "getDataOperation: Exception: " + e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "getDataOperation: Disconnection Exception: " + e.getMessage());
                }
            }
        }
        return null;
    }

    // --- POST Request --- //
    private String postDataOperation(String serverAddress, String urlParameters) {
        // initialize connection
        HttpURLConnection httpURLConnection = null;
        try {
            // make connection to server
            URL url = new URL(serverAddress);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            // construct the final address if necessary
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
            outputStreamWriter.write(urlParameters);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            httpURLConnection.connect();
            int statusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "postDataOperation: statusCode: " + statusCode);
            switch (statusCode) {
                case 200:
                case 201:
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String newLine;
                    while ((newLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(newLine + "\n");
                    bufferedReader.close();
                    return stringBuilder.toString();
                    }
                case 404:
                    Log.d(TAG, "postDataOperation: getDataOperation request failed.");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "postDataOperation: Exception: " + e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "postDataOperation: Disconnection Exception: " + e.getMessage());
                }
            }
        }
        return null;
    }

    // --- PUT Request --- //
    private String putDataOperation(String serverAddress, String urlParameters) {
        // initialize connection
        HttpURLConnection httpURLConnection = null;
        try {
            // make connection to server
            URL url = new URL(serverAddress);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            // construct the final address
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
            outputStreamWriter.write(urlParameters);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            httpURLConnection.connect();
            int statusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "putDataOperation: statusCode: " + statusCode);
            switch (statusCode) {
                case 200:
                case 201:
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String newLine;
                    while ((newLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(newLine + "\n");
                        bufferedReader.close();
                        return stringBuilder.toString();
                    }
                case 404:
                    Log.d(TAG, "putDataOperation: getDataOperation request failed.");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "putDataOperation: Exception: " + e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "putDataOperation: Disconnection Exception: " + e.getMessage());
                }
            }
        }
        return null;
    }
}
