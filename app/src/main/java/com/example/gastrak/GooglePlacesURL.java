package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: GooglePlacesURL.java

// Object to create a GooglePlacesURL

import android.util.Log;

public class GooglePlacesURL {

    // hold name
    private static final String TAG = "GooglePlacesURL";

    // fields for the URL object
    private double lat;
    private double lng;
    private int radius;
    private String searchString;
    private String searchType;
    private boolean sensor;
    private String apikey;

    public GooglePlacesURL(Double lat, Double lng, int radius, String searchType, boolean sensor, String apikey) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.searchType = searchType;
        this.sensor = sensor;
        this.apikey = apikey;
    }

    public String returnGooglePlacesURLForNearbySearch() {
        StringBuilder gpURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        // build string and return constructed url
        gpURL.append("location="+lat+","+lng);
        gpURL.append("&radius="+radius);
        gpURL.append("&type="+searchType);
        gpURL.append("&sensor="+sensor);
        gpURL.append("&key="+apikey);
        Log.d(TAG, "returnGooglePlacesURLForNearbySearch: Successfully constructed URL: " + gpURL.toString());
        return gpURL.toString();
    }

}
