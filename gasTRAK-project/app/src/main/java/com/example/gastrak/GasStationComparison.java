package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: GasStationComparison.java

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class GasStationComparison {

    // hold name
    private static final String TAG = "GasStationComparison";

    // variable for gas tank size
    public static double profileGasTankSize;

    // --------------------------- Comparison/Dataflow Methods --------------------------- //
    // method to calculate gas station cost
    public static Double calculateGasStationCostValue(LatLng curr, LatLng station, String stationAddress) {
        // configure Location objects
        Location currentLocation = new Location("Current Location");
        Location gasStation = new Location("Gas Station");
        // 1: compute distance between - data used in step 3
        float distance = MapsActivity.calcDistance(curr, station);
        // 2: calculate cost to fill the tank
        //    REPLACE WITH USER TANK INFO
        String price = MapsActivity.retrieveGasPrice(stationAddress);
        double gasPrice = Double.parseDouble(price);
        // total cost = fullTank (L) * price ($/L)
        // use the set profileTankSize based on which profile the user has selected as default
        double totalCost = profileGasTankSize * gasPrice;
        Log.d(TAG, "calculateGasStationCostValue: totalCost: " + profileGasTankSize + " * " + gasPrice + " = " + totalCost);
        /* 3: FUEL EFFICIENCY DATA (for subsequent trips)
         - calculate the amount of gas needed to get there = (distance [km] / fuel efficiency [km/L])
         - on trips after the first, pull last trip data (gas price) = lastGasPrice
         - formula: gasInTankNow = (gasNeededToGetThere * lastGasPrice)
         - as such: totalCost = (totalCost + gasInTankNow) <-- will add the cost of actually DRIVING there to the algorithm
        */
        //4: return
        return totalCost;
    }

    public static int returnHueValue(ArrayList<Double> arrayList) {
        return 0;
    }
}
