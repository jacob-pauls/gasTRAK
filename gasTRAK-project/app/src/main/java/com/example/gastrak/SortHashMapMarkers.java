package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: SortHashMapMarkers.java
// This was algorithm was primarily sourced from GeeksForGeeks.com
// Link: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortHashMapMarkers {

    // hold name
    private static final String TAG = "SortHashMapMarkers";

    // method to sort hashmap values
    public static HashMap<MarkerOptions, Double> sortHashMap(HashMap<MarkerOptions, Double> hashMap) {
        // create a linked list to store the data
        List<HashMap.Entry<MarkerOptions, Double>> list = new LinkedList<HashMap.Entry<MarkerOptions, Double>>(hashMap.entrySet());
        // sort the list
        Collections.sort(list, new Comparator<HashMap.Entry<MarkerOptions, Double>>() {
            @Override
            public int compare(HashMap.Entry<MarkerOptions, Double> t1, HashMap.Entry<MarkerOptions, Double> t2) {
                return (t1.getValue()).compareTo(t2.getValue());
            }
        });
        // put the data from the sorted list into the hashmap
        HashMap<MarkerOptions, Double> temp = new LinkedHashMap<MarkerOptions, Double>();
        for (HashMap.Entry<MarkerOptions, Double> i : list) {
            temp.put(i.getKey(), i.getValue());
        }
        return temp;
    }
}
