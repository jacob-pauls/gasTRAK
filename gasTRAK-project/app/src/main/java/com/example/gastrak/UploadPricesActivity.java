package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: UploadPricesActivity.java

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.example.gastrak.MapsActivity.gpDB;
import static com.example.gastrak.MapsActivity.nv;

public class UploadPricesActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "UploadPricesActivity";

    // icons and features
    private ImageView backButton;
    private Button addDBEntry;
    private EditText addressEntry;
    private EditText fuelEntry;

    // date format
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss z");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_prices);
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
        // db entry button listener
        addressEntry = findViewById(R.id.addressEntry);
        fuelEntry = findViewById(R.id.fuelEntry);
        addDBEntry = findViewById(R.id.addDBEntry);
        addDBEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Adding entry to DB...");
                setGasPrice(addressEntry, fuelEntry);
                hideMobileKeyboard();
            }
        });
        // listeners for both EditText fields
        addressEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // if the action command is completed, searchable, or the user hits enter, etc.
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // perform search
                    Log.d(TAG, "onClick: Adding entry to DB...");
                    setGasPrice(addressEntry, fuelEntry);
                    hideMobileKeyboard();
                }
                return false;
            }
        });
        fuelEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // if the action command is completed, searchable, or the user hits enter, etc.
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // perform search
                    Log.d(TAG, "onClick: Adding entry to DB...");
                    setGasPrice(addressEntry, fuelEntry);
                    hideMobileKeyboard();
                }
                return false;
            }
        });

    }

    // --------------------------- Database/Dataflow Methods --------------------------- //
    // set the price for a given gas station
    public void setGasPrice(EditText addressEntry, EditText priceEntry) {
        Log.d(TAG, "setGasPrice: Setting gas price...");
        // retrieve address, price, and current time
        String address = GasStationFeatures.findGasStationInsertPrice(UploadPricesActivity.this, addressEntry);
        String price = priceEntry.getText().toString();
        Date currentTime = Calendar.getInstance().getTime();
        String time = format.format(currentTime);
        // if a result is shown, commit to update
        if (MapsActivity.checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_STATIONADDRESS, address)) {
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            // value set for price and time updates
            cv.put(PricesEntry.COLUMN_NAME_PRICE, price);
            cv.put(PricesEntry.COLUMN_NAME_TIME, time);
            Log.d(TAG, "setGasPrice: Performing price and time updates...");
            wdb.update(PricesEntry.TABLE_NAME, cv, PricesEntry.COLUMN_NAME_STATIONADDRESS + " = ?", new String[] {address});
            wdb.close();
            GasStationFeatures.findGasStation(UploadPricesActivity.this, addressEntry);
            nv.setCheckedItem(R.id.mi_main);
            finish();
            addressEntry.setText("");
            priceEntry.setText("");
        } else {
            // instantiate database error
            DatabaseErrorDialog dbed = new DatabaseErrorDialog();
            dbed.show(getSupportFragmentManager(), "database error dialog");
            addressEntry.setText("");
            priceEntry.setText("");
        }
    }

    // --------------------------- Utility Methods --------------------------- //
    // called after a text entry is made
    public void hideMobileKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addressEntry.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        imm.hideSoftInputFromWindow(fuelEntry.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
