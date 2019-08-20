package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: DownloadUrl.java

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {

    // hold name
    private static final String TAG = "DownloadUrl";

    public String readURL(String passedUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            // open the HTTP connection to make google place requests
            // make a connection and instantiate a url with given data
            URL url = new URL(passedUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // append the stream to a SringBuffer and then to a String
            inputStream = urlConnection.getInputStream();
            BufferedReader bR = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sB = new StringBuffer();

            String sBResults = "";
            while ((sBResults = bR.readLine()) != null) {
                sB.append(sBResults);
            }

            data = sB.toString();
            bR.close();
        } catch (MalformedURLException e) {
            Log.e(TAG, "readURL: MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "readURL: IOException: " + e.getMessage());
        } finally {
            // always close and disconnect connection regardless of exceptions
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
