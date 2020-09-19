package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: GasStationFeatures.java

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GasStationFeatures {

    // hold name
    private static final String TAG = "GasStationFeatures";
    public static boolean isSearch = false;

    // finds gas station using geolocation (edit text passes)
    public static void findGasStation(Context context, EditText locationSearch) {
        Log.d(TAG, "findGasStation: geolocating...");
        // hold the user's desired location
        String searchString = locationSearch.getText().toString();
        Geocoder gc = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        try{
            list = gc.getFromLocationName(searchString, 10);
            Log.d(TAG, "findGasStation: list.size(): " + list.size());
        } catch (IOException e){
            Log.e(TAG, "findGasStation: IOException: " + e.getMessage());
        }
        // if we have any search results
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "findGasStation: Found address location: " + address.toString());
            isSearch = true;
            String markerTitle = locationSearch.getText().toString();
            String markerSnippet = address.getFeatureName() + " " + address.getThoroughfare() + ", " + address.getLocality();
            MapsActivity.updateCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f, markerTitle, markerSnippet);
        }
        locationSearch.setText("");
    }

    // finds gas station using geolocation (lat lng passes)
    public static void findGasStation(Context context, LatLng locationLatLng, String stationName) {
        Log.d(TAG, "findGasStation: geolocating...");
        // hold the user's desired location
        Geocoder gc = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        try{
            double latitude = locationLatLng.latitude;
            double longitude = locationLatLng.longitude;
            list = gc.getFromLocation(latitude, longitude, 10);
            Log.d(TAG, "findGasStation: list.size(): " + list.size());
        } catch (IOException e){
            Log.e(TAG, "findGasStation: IOException: " + e.getMessage());
        }
        // if we have any search results
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "findGasStation: Found address location: " + address.toString());
            isSearch = true;
            String markerTitle = stationName;
            String markerSnippet = address.getFeatureName() + " " + address.getThoroughfare() + ", " + address.getLocality();
            MapsActivity.updateCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f, markerTitle, markerSnippet);
        }
    }


    public static void retrieveAndSendPlaceObjectData(Place placeSearchResult) {
        Log.d(TAG, "retrieveAndSendPlaceObjectData: Place sent from AutocompleteSupportFragment: " + placeSearchResult);
        String markerTitle = placeSearchResult.getName();
        String markerSnippet = placeSearchResult.getName() + ", " + placeSearchResult.getAddress();
        MapsActivity.updateCamera(placeSearchResult.getLatLng(), 15f, markerTitle, markerSnippet);
    }

    public static String findGasStationInsertPrice(Context context, EditText addressEntry) {
        Log.d(TAG, "findGasStationInsertPrice: Method called.");
        String searchString  = addressEntry.getText().toString();
        Geocoder gc = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        try{
            list = gc.getFromLocationName(searchString, 1);
        } catch (IOException e){
            Log.e(TAG, "findGasStationInsertPrice: IOException: " + e.getMessage());
        }
        // if we have any search results
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "findGasStationInsertPrice: Found address location: " + address.toString());
            String addressFound = address.getFeatureName() + " " + address.getThoroughfare() + ", " + address.getLocality();
            return addressFound;
        }
        // not handled yet, but if it cant geolocate it will return the empty string. nothing will happen
        return "";
    }
}
