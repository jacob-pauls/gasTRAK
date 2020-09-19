package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: FuelEfficiencyActivity.java

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.gastrak.MapsActivity.nv;

public class FuelEfficiencyActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "FuelEfficiencyActivity";

    // icons and features
    private ImageView backButton;
    private Button calcButton;
    private EditText kmEntry;
    private EditText tankSizeEntry;
    private TextView fETV;
    private TextView fENumTV;
    private TextView sideTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_efficiency);
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
        // initialize text objects
        kmEntry = findViewById(R.id.kmEntry);
        tankSizeEntry = findViewById(R.id.tankSizeEntry);
        fETV = findViewById(R.id.fETV);
        fENumTV = findViewById(R.id.fENumTV);
        sideTV = findViewById(R.id.sideTV);
        // calculate button listener
        calcButton = findViewById(R.id.calcFuelEfficiency);
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // retrieve values
                String km = kmEntry.getText().toString();
                String fuel = tankSizeEntry.getText().toString();
                // calculate
                float answer = (Float.parseFloat(km) / Float.parseFloat(fuel));
                String ansString = String.format("%.02f", answer);
                // show efficiency
                fETV.setVisibility(View.VISIBLE);
                sideTV.setVisibility(View.VISIBLE);
                fENumTV.setText(ansString);
            }
        });
    }
}
