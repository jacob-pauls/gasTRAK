package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: GetNearbyPlaces.java

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gastrak.MapsActivity.clickGPButton;
import static com.example.gastrak.MapsActivity.dropMarker;
import static com.example.gastrak.MapsActivity.hashMapMarkers;

public class GetNearbyPlaces extends AsyncTask<Object, String, String> {

    // hold name
    private static final String TAG = "GetNearbyPlaces";

    // global variables
    String gpData;
    GoogleMap mMap;
    String url;

    private void showNearbyPlaces(List<HashMap<String, String>> fullNearbyPlaceList) {
        for (int i = 0; i < fullNearbyPlaceList.size(); i++) {
            HashMap<String, String> gp = fullNearbyPlaceList.get(i);

            // assign attributes from hash map
            String placeName = gp.get("place_name");
            String vicinity = gp.get("vicinity");
            float la = Float.parseFloat(gp.get("lat"));
            float ln = Float.parseFloat(gp.get("lng"));

            // set the marker with title and address (vicinity)
            LatLng laln = new LatLng(la, ln);
            String markerTitle = placeName;
            String markerSnippet = vicinity;
            MapsActivity.dropMarker(laln, markerTitle, markerSnippet);
        }
    }

    // overridden AsyncTask methods
    // doInBackground: returns google places data through passed URL
    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            // read the url and assign the found data
            gpData = downloadUrl.readURL(url);
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "doInBackground: gpData returned: " + gpData);
        return gpData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> fullNearbyPlaceList = null;
        ParseData parseObject = new ParseData();
        fullNearbyPlaceList = parseObject.parse(s);
        showNearbyPlaces(fullNearbyPlaceList);

        // once all results are completed, sort the hash map
        HashMap<MarkerOptions, Double> sortedMap = SortHashMapMarkers.sortHashMap(hashMapMarkers);
        // need to compute HUE values
        int maxHue = 120;
        int hashMapSize = sortedMap.size();
        int hueScale = maxHue / hashMapSize;
        // print the sorted hash map
        for (MarkerOptions name : sortedMap.keySet()) {
            String key = name.toString();
            String value = sortedMap.get(name).toString();
            Log.d(TAG, "onPostExecute: hashMapMarkers print: Key: " + key + " Value: " + value);
            Log.d(TAG, "onPostExecute: hashMapMarkers hueScale: " + hueScale);
        }

        // reassign ALL values
        Double value;
        Double keepValue;
        String markerTitle;
        String markerSnippet;
        LatLng markerPosition;
        float markerHue = 0;
        // track count
        int count = 0;
        // cycle through hash map
        for (MarkerOptions name : sortedMap.keySet()) {
            // retain the value
            value = sortedMap.get(name).doubleValue();
            keepValue = value;
            // assign title, snippet, position
            markerTitle = name.getTitle();
            markerSnippet = name.getSnippet();
            markerPosition = name.getPosition();
            // calculate hue
            markerHue = maxHue - (hueScale*count);
            // add back in
            dropMarker(markerPosition, markerTitle, markerSnippet, markerHue);
            // increment count
            count++;
        }
        // reset boolean to control google maps button
        clickGPButton = false;
        // reset hash maps
        sortedMap.clear();
        hashMapMarkers.clear();
    }

}
