package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: PreviousTripsActivity.java

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.gastrak.MapsActivity.gpDB;
import static com.example.gastrak.MapsActivity.nv;
import static com.example.gastrak.MapsActivity.showDB;

public class PreviousTripsActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "PreviousTripsActivity";

    // icons and features
    private ImageView backButton;

    // variables
    private ListView previousTripsList;
    private TextView noShowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_trips);
        showTrips();
        init();
    }

    // --------------------------- Initialization Methods --------------------------- //
    // initialize any GUI components
    private void init() {
        Log.d(TAG, "init: Initializing...");
        // back button listener
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nv.setCheckedItem(R.id.mi_main);
                finish();
            }
        });
    }

    // --------------------------- Database/Dataflow Methods --------------------------- //
    // show trips in the trip list view
    private void showTrips() {
        // list view click listener
        previousTripsList = findViewById(R.id.previousTripsList);
        noShowText = findViewById(R.id.previousTripsTV);
        ArrayList<String> theList = new ArrayList<>();
        Cursor curr = gpDB.getAllRows(TripsEntry.TABLE_NAME);
        if (curr.getCount() == 0) {
            previousTripsList.setVisibility(View.INVISIBLE);
            noShowText.setVisibility(View.VISIBLE);
        } else {
            while(curr.moveToNext()) {
                String title = "\n" + curr.getString(0) + ". " + curr.getString(1) + " \n    " + curr.getString(2) + " \n    " + curr.getString(4) + "    -    $" + curr.getString(6) + "\n";
                theList.add(title);
                ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, theList);
                previousTripsList.setAdapter(listAdapter);
            }
        }
        curr.close();
    }
}
