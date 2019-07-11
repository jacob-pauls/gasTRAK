package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: MainActivity.java

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    //  constants for activity name and error number if need be
    private static final String TAG = "MainActivity";
    private static final int ERROR_REQUEST = 9001;
    static boolean isClicked = false;
    // declare progress dialog for loading procedure
    static ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // check update
        if (isGSUpdated())
            openGT();
    }

    // open gasTRAK
    private void openGT() {
        final Button beginButton = (Button) findViewById(R.id.beginBtn);
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog();
                Intent map =  new Intent(MainActivity.this, MapsActivity.class);
                startActivity(map);
                isClicked = true;
            }
        });
    }

    // check to see if google play services are updated
    public boolean isGSUpdated() {
        Log.d(TAG,"isGSUpdated: Checking Google Services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available == ConnectionResult.SUCCESS) {
            // connected
            Log.d(TAG, "isGSUpdated: Google Play Services is updated and working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            // error, version issue
            Log.d(TAG, "isGSUpdated: Error occurred, try updating or other fixable solution");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_REQUEST);
            // google will provide a solution, based on the error
            dialog.show();
        } else {
            Toast.makeText(this, "Error occurred, cannot connect to gasTRAK", Toast.LENGTH_SHORT);
        } // end if
        return false;
    }

    public void showProgressDialog() {
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("gasTRAK is Loading");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.setTitle("Please wait...");
        pDialog.show();
    }
}
