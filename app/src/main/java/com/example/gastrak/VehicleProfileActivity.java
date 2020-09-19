package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: VehicleProfileActivity.java

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.gastrak.MapsActivity.gpDB;
import static com.example.gastrak.MapsActivity.nv;
import static com.example.gastrak.MapsActivity.retrieveVehicleData;

public class VehicleProfileActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "VehicleProfileActivity";

    // icons and features
    private ImageView backButton;
    private ImageView addButton;
    private ListView listView;
    private TextView noShowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_profile);
        showProfiles();
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
        // add button listener
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show the add dialog
                VehicleProfileDialog vpd = new VehicleProfileDialog();
                vpd.show(getSupportFragmentManager(), "Vehicle Profile Dialog");

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // retrieves name of profile
                Object holdObj = listView.getItemAtPosition(position);
                String profile = holdObj.toString();
                Log.d(TAG, "ListView: onItemClick: " + profile);
                // retrieve data
                retrieveVehicleData(profile);
                // calls profile view dialog
                ViewVehicleProfileDialog vvpd = new ViewVehicleProfileDialog();
                vvpd.show(getSupportFragmentManager(), "View Vehicle Profile Dialog");
            }
        });
    }

    // --------------------------- Database/Dataflow Methods --------------------------- //
    public void showProfiles() {
        listView = findViewById(R.id.profileList);
        noShowText = findViewById(R.id.profileTV);
        // make array list to hold elements
        ArrayList<String> theList = new ArrayList<>();
        // have cursor enter in the name of each profile as a list element
        Cursor curr = gpDB.getAllRows(VehiclesEntry.TABLE_NAME);
        if (curr.getCount() == 0) {
            listView.setVisibility(View.INVISIBLE);
            noShowText.setVisibility(View.VISIBLE);
        } else {
            while(curr.moveToNext()) {
                String title = curr.getString(1);
                theList.add(title);
                ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, theList);
                listView.setAdapter(listAdapter);
            }
        }
        curr.close();
    }
}
