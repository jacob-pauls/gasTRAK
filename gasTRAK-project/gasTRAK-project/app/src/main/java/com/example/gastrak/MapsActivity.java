package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: MapsActivity.java

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.icu.text.LocaleDisplayNames;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.gastrak.MainActivity.isClicked;
import static com.example.gastrak.MainActivity.pDialog;


public class MapsActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "MapsActivity";
    // global variables to hold device location and permission data
    private static final String F_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String C_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 909;
    private static final float NORMAL_ZOOM = 15f;
    // variables
    private Boolean locationPermGranted = false;
    private boolean isStartup = true;
    private GoogleMap mMap;
    private DrawerLayout drawer;
    //  object used to find location
    private FusedLocationProviderClient mFLPC;
    // icons and features
    private EditText locationSearch;
    private ImageView hMenuIcon;
    private ImageView fLocationIcon;
    //private ImageView nStationIcon = (ImageView) findViewById(R.id.nearbyStationIcon);
    private ImageView zInIcon;
    private ImageView zOutIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // check permissions, if allowed, make map
        getLocationPermissions();
        // run gui features and components
        locationSearch = (EditText) findViewById(R.id.gasStation);
        init();
    }

    // method to initialize and perform tasks for GUI components
    private void init() {
        Log.d(TAG, "init: Initializing...");
        // listener for when the the user starts typing
        locationSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // if the action command is completed, searchable, or the user hits enter, etc.
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // perform search
                    findGasStation();

                }
                return false;
            }
        });
        // hamburger menu listener
        hMenuIcon = findViewById(R.id.hamMenuIcon);
        // initialize drawer object and lock drawer
        drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        hMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked hamburger menu icon");
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        // find location icon listener
        fLocationIcon = findViewById(R.id.findLocationIcon);
        fLocationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked current location icon");
                getCurrentLocation();
            }
        });
        // find closest gas station listener
        //
        // zoom in listener
        zInIcon = findViewById(R.id.zoomInIcon);
        zInIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Zoom increased");
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        // zoom out listener
        zOutIcon = findViewById(R.id.zoomOutIcon);
        zOutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Zoom decreased");
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    /* find gas stations using geoLocation
       - currently only outputs data to LogCat
       - find a way to mark gas stations, and restrict searches to nearby gas stations only */
    private void findGasStation() {
        Log.d(TAG, "findGasStation: geolocating...");
        // hold the user's desired location
        String searchString = locationSearch.getText().toString();
        Geocoder gc = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = gc.getFromLocationName(searchString, 1);
        } catch (IOException e){
            Log.e(TAG, "findGasStation: IOException: " + e.getMessage());
        }
        // if we have any search results
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "findGasStation: Found address location: " + address.toString());
        }
    }

    // find current location using the google API
    private void getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation: Getting the current location");
        mFLPC = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(locationPermGranted) {
                Log.d(TAG, "getCurrentLocation: Location permissions passed");
                Task location = mFLPC.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"onComplete: Successfully found location");
                            Location currentLocation = (Location) task.getResult();
                            // update camera to match current location of the user
                            updateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), NORMAL_ZOOM);
                            isStartup = false;
                        } else {
                            Log.d(TAG, "onComplete: Found location failed");
                            Toast.makeText(MapsActivity.this, "Unable to find current location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getCurrentLocation: Security Exception:  " + e.getMessage());
        }
    }

    // perform camera updates using the desired latitude and longitude
    private void updateCamera(LatLng laln, float zoom) {
        Log.d(TAG, "updateCamera: Moving camera to " + laln.latitude + ", " + laln.longitude);
        if (isStartup) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
        }
    }

    // create map
    private void makeMap() {
        // refer to map
        Log.d(TAG, "makeMap: Making map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Log.d(TAG, "onMapReady: Map is ready!");
                Toast.makeText(MapsActivity.this, "Welcome to gasTRAK!", Toast.LENGTH_SHORT).show();

                if(locationPermGranted) {
                    // adjust camera to current location
                    getCurrentLocation();
                    // mark current location for user
                    try {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    } catch (SecurityException e) {
                        Log.e(TAG, "onMapReady: SecurityException: Failed to enable current location on map");
                    }
                }
            }
        });
    }

    // get permissions
    private void getLocationPermissions() {
        // store permissions in an array
        String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), F_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermissions: Fine location granted");
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), C_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLocationPermissions: Coarse location granted");
                Log.d(TAG, "getLocationPermissions: Location permissions granted");
                // permissions of both types granted
                locationPermGranted = true;
                // initialize map
                makeMap();
            } else {
                // ask for permission
                ActivityCompat.requestPermissions(this, perm, LOCATION_REQUEST_CODE);
            }
        } else {
            // ask for permission
            ActivityCompat.requestPermissions(this, perm, LOCATION_REQUEST_CODE);
        }
    }

    // if we need to request permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: Requested permissions");
        // reset boolean
        locationPermGranted = false;
        switch(requestCode) {
            case LOCATION_REQUEST_CODE:
                // if both permissions are granted = true
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    locationPermGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                    // since true, make the map
                    makeMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Permission failed");
                }
        }
    }

    // if back is pressed, clear dialogs from main screen
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called.");
        super.onBackPressed();
        if (isClicked == true) {
            if(pDialog.isShowing())
                pDialog.dismiss();
        }
        isClicked = false;
    }
}
