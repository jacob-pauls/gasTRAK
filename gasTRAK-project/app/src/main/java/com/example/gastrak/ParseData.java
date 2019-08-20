package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: ParseData.java

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseData {

    // hold name
    private static final String TAG = "ParseData";

    // store a single retrieved nearby place in a HashMap
    private HashMap<String, String> getPlace(JSONObject gpJSON) {
        // HashMap object
        HashMap<String, String>  gpMap = new HashMap<>();
        // JSON attributes
        String placeName = "na";
        String vicinity = "na";
        String latitude = "";
        String longitude = "";
        String reference = "";
        try {
            // retrieve the JSON data and place data
            if (!gpJSON.isNull("name")) {
                placeName = gpJSON.getString("name");
            }
            if (!gpJSON.isNull("vicinity")) {
                vicinity = gpJSON.getString("vicinity");
            }
            latitude = gpJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = gpJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = gpJSON.getString("reference");
            gpMap.put("place_name", placeName);
            gpMap.put("vicinity", vicinity);
            gpMap.put("lat", latitude);
            gpMap.put("lng", longitude);
            gpMap.put("reference", reference);
        } catch (JSONException e) {
            Log.e(TAG, "getPlace: JSONException: " + e.getMessage());
        }
        return gpMap;
    }

    // retrieve a list of HashMaps, containing each nearby place
    private List<HashMap<String, String>> getPlaces(JSONArray jsArray) {
        List<HashMap<String, String>> fullPlaceList = new ArrayList<>();
        HashMap<String, String> placeMap = null;
        for (int i = 0; i < jsArray.length(); i++) {
            try {
                placeMap = getPlace((JSONObject) jsArray.get(i));
                fullPlaceList.add(placeMap);
            } catch (JSONException e) {
                Log.e(TAG, "getPlaces: JSONException: " + e.getMessage());
            }
        }
        return fullPlaceList;
    }

    // parces json data
    public List<HashMap<String, String>> parse(String jsData) {
        JSONArray jsArray = null;
        JSONObject jsObject;
        try {
            jsObject = new JSONObject(jsData);
            jsArray = jsObject.getJSONArray("results");
        } catch (JSONException e) {
            Log.e(TAG, "parse: JSONException: " + e.getMessage());
        }
        return getPlaces(jsArray);
    }

}
