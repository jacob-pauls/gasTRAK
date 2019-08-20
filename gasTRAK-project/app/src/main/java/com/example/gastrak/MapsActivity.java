package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: MapsActivity.java

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.example.gastrak.GasStationFeatures.isSearch;
import static com.example.gastrak.MainActivity.isClicked;
import static com.example.gastrak.MainActivity.pDialog;


public class MapsActivity extends AppCompatActivity {

    // --------------------------- Variables and Constants --------------------------- //
    // hold name
    private static final String TAG = "MapsActivity";

    // global variables to hold device location and permission data
    private static final String F_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String C_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 909;
    private static final float NORMAL_ZOOM = 15f;
    private static final float NEARBY_PLACES_ZOOM = 12f;
    private static final int PROXIMITY_RADIUS = 5000;
    // my places API key, has to be configured for the IP it is being used for
    private static final String PLACES_API_KEY = "AIzaSyBBiipyoBIyMemiKh8o2d9cvLndKN9Xj6g";

    // booleans
    private boolean locationPermGranted = false;
    private static boolean isStartup = true;
    private boolean createdIntent = false;
    public static boolean clickGPButton = false;

    // objects
    private static GoogleMap mMap;
    private DrawerLayout drawer;

    // date format
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss z");

    //  objects used to find location
    private FusedLocationProviderClient mFLPC;
    private static LatLng curr;
    // assign gas station LatLng value globally
    private static LatLng station;

    // icons and features
    private EditText locationSearch;
    private ImageView hMenuIcon;
    private ImageView fLocationIcon;
    private ImageView nStationIcon;
    private ImageView zInIcon;
    private ImageView zOutIcon;
    private Button sTrackButton;
    public static NavigationView nv;
    private Intent navIntent;

    // variable to globally hold an address
    private static String globalAddress;

    // database instantiations
    public static GasDB gpDB;

    // arraylist to hold gas prices
    public static HashMap<MarkerOptions, Double> hashMapMarkers = new HashMap<MarkerOptions, Double>();

    // --------------------------- Activity Life Cycle Methods --------------------------- //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // check permissions, if allowed, make map
        getLocationPermissions();
        // run gui features and components
        locationSearch = findViewById(R.id.gasStation);
        sTrackButton = findViewById(R.id.trackButton);
        // load db
        gpDB = new GasDB(this);
        // initialize
        init();
    }

    // everytime we return to the activity check if there is a profile
    @Override
    protected void onResume() {
        if (!checkForProfile()) {
            // cant make searches without a profile
            locationSearch.setEnabled(false);
            sTrackButton.setEnabled(false);
            nStationIcon.setEnabled(false);
            nStationIcon.setClickable(false);
            // show dialog
            ProfileErrorDialog ped = new ProfileErrorDialog();
            ped.show(getSupportFragmentManager(), "Profile Error Dialog");
        } else {
            locationSearch.setEnabled(true);
            sTrackButton.setEnabled(true);
            nStationIcon.setEnabled(true);
            nStationIcon.setClickable(true);
        }
        super.onResume();
    }

    // --------------------------- Initialization Methods --------------------------- //
    // method to initialize and perform tasks for GUI components
    private void init() {
        Log.d(TAG, "init: Initializing...");
        // main button listener
        sTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GasStationFeatures.findGasStation(MapsActivity.this, locationSearch);
                hideMobileKeyboard();
                startTrip();
            }
        });
        // listener for when the the user starts typing
        locationSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // if the action command is completed, searchable, or the user hits enter, etc.
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // perform search
                    GasStationFeatures.findGasStation(MapsActivity.this, locationSearch);
                    hideMobileKeyboard();
                    startTrip();
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
        // navigation menu and listeners
        nv = findViewById(R.id.ham_menu_view);
        nv.setCheckedItem(R.id.mi_main);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // check to see which navigation menu was selected and open respective activity
                switch (item.getItemId()) {
                    // 'Gas Map'
                    case R.id.mi_main:
                        // if we have created an intent, just go back to the main menu
                        if (createdIntent) {
                            finish();
                            createdIntent = false;
                        }
                        break;
                    // 'Vehicle Profiles'
                    case R.id.mi_vehicle_profiles:
                        Log.d(TAG, "onNavigationItemSelected: Selected VehicleProfileActivity");
                        navIntent = new Intent(MapsActivity.this, VehicleProfileActivity.class);
                        startActivity(navIntent);
                        createdIntent = true;
                        break;
                    // 'Fuel Efficiency Calculator'
                    case R.id.mi_fuel_efficiency_calculator:
                        Log.d(TAG, "onNavigationItemSelected: Selected FuelEfficiencyActivity");
                        navIntent = new Intent(MapsActivity.this, FuelEfficiencyActivity.class);
                        startActivity(navIntent);
                        createdIntent = true;
                        break;
                    // 'Previous Trips'
                    case R.id.mi_previous_trips:
                        Log.d(TAG, "onNavigationItemSelected: Selected PreviousTripsActivity");
                        navIntent = new Intent(MapsActivity.this, PreviousTripsActivity.class);
                        startActivity(navIntent);
                        createdIntent = true;
                        break;
                    // 'Upload Prices'
                    case R.id.mi_upload_prices:
                        Log.d(TAG, "onNavigationItemSelected: Selected UploadPricesActivity");
                        navIntent = new Intent(MapsActivity.this, UploadPricesActivity.class);
                        startActivity(navIntent);
                        createdIntent = true;
                        break;
                    // 'Instagram'
                    case R.id.mi_instagram:
                        Toast.makeText(MapsActivity.this, "Instagram", Toast.LENGTH_LONG).show();
                        break;
                }
                // retain the drawer to its starting position
                drawer.closeDrawer(GravityCompat.START);
                return true;
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
        // find closest gas stations listener
        nStationIcon = findViewById(R.id.nearbyStationIcon);
        nStationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked nearby gas station icon");
                clickGPButton = true;
                executeNearbyPlaces();
            }
        });
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
        // CHECK IF THERE IS A VEHICLE PROFILE ONCE GUI IS SET
        // if there is no vehicle profile
        Log.d(TAG, "init: Checking for vehicle profile...");
        if (!checkForProfile()) {
            // cant make searches without a profile
            locationSearch.setEnabled(false);
            sTrackButton.setEnabled(false);
            nStationIcon.setEnabled(false);
            nStationIcon.setClickable(false);
            // show dialog
            ProfileErrorDialog ped = new ProfileErrorDialog();
            ped.show(getSupportFragmentManager(), "Profile Error Dialog");
        }
    }

    // --------------------------- Map Features --------------------------- //
    // perform camera updates using the desired latitude and longitude
    // drops a marker if the location is not the user's current location
    public static void updateCamera(LatLng laln, float zoom, String title) {
        Log.d(TAG, "updateCameraL Clearing markers ");
        if (isStartup)
            mMap.clear();
        if (isSearch)
            mMap.clear();
        Log.d(TAG, "updateCamera: Moving camera to " + laln.latitude + ", " + laln.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
        // drop a marker
        dropMarker(laln, title);
        // print the db status after marker drop
        showDB(PricesEntry.TABLE_NAME);
    }

    // perform camera updates and drop a marker with a snippet
    public static void updateCamera(LatLng laln, float zoom, String title, String snippet) {
        Log.d(TAG, "updateCameraL Clearing markers ");
        if (isStartup)
            mMap.clear();
        if (isSearch)
            mMap.clear();
        Log.d(TAG, "updateCamera: Moving camera to " + laln.latitude + ", " + laln.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
        // drop a marker
        dropMarker(laln, title, snippet);
    }

    // drop marker without snippet
    public static void dropMarker(LatLng laln, String title) {
        // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            mMap.addMarker(mOptions);
        }
    }

    // overloaded marker method for markers with snippets
    public static void dropMarker(LatLng laln, String title, String snippet) {
    // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            // NOTE: Before ANY marker is dropped, an entry must be added to the SQLiteDB
            String tempSnippet;
            insertGasEntry(title, snippet);
            String price = retrieveGasPrice(snippet);
            // assign global values for other methods
            globalAddress = snippet;
            station = laln;
            // append price to snippet
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            tempSnippet = " \nPrice : $" + price + " /L" ;
            String newSnippet = snippet + tempSnippet;
            mOptions.snippet(newSnippet);
            if (!clickGPButton) {
                mMap.addMarker(mOptions);
            } else {
                // make comparison between self and gas station, store values in arraylist
                Double totalCost = GasStationComparison.calculateGasStationCostValue(curr, laln, snippet);
                hashMapMarkers.put(mOptions, totalCost);
                Log.d(TAG, "dropMarker: hashMapMarkers: " + hashMapMarkers.get(mOptions));
            }
            showDB(PricesEntry.TABLE_NAME);
        }
    }

    // overloaded drop marker for a particular hue
    public static void dropMarker(LatLng laln, String title, String snippet, float hue) {
    // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            mOptions.snippet(snippet);
            mOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
            mMap.addMarker(mOptions);
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
                Toast.makeText(MapsActivity.this, "Welcome to gasTRAK!", Toast.LENGTH_LONG).show();

                if(locationPermGranted) {
                    // adjust camera to current location
                    getCurrentLocation();
                    // mark current location for user
                    try {
                        // enable default google maps marker
                        mMap.setMyLocationEnabled(true);
                        Log.d(TAG, "setMyLocationEnabled: true");
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        // sets the map to be padded around the UI, makes google logo visible as per their terms and conditions
                        int leftPadding = paddingConversion(10);
                        int topPadding = paddingConversion(123);
                        int rightPadding = paddingConversion(10);
                        int bottomPadding = paddingConversion(123);
                        mMap.setPadding(leftPadding,topPadding,rightPadding,bottomPadding);
                        // set the information window for each marker
                        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                        // set a listener to watch for marker info window clicks
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Log.d(TAG, "onInfoWindowClick: Info Window Clicked: " + marker.getSnippet());
                                locationSearch.setText(marker.getTitle());
                            }
                        });
                    } catch (SecurityException e) {
                        Log.e(TAG, "onMapReady: SecurityException: Failed to enable current location on map");
                    }
                }
            }
        });
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
                            curr = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            updateCamera(curr, NORMAL_ZOOM, "Current Location");
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
    // method to calculate distance
    // used as part of the GasStationComparison
    public static float calcDistance(LatLng curr, LatLng station) {
        // configure Location objects
        Location currentLocation = new Location("Current Location");
        Location gasStation = new Location("Gas Station");
        // build distance
        currentLocation.setLatitude(curr.latitude);
        currentLocation.setLongitude(curr.longitude);
        gasStation.setLatitude(station.latitude);
        gasStation.setLongitude(station.longitude);
        float distance = currentLocation.distanceTo(gasStation)/1000;
        return distance;
    }
    // --------------------------- Google Places Methods --------------------------- //
    // construct the url to find nearby gas stations
    private String getUrl(double la, double ln, String nearbyPlace) {
        // url google provides in their 'Find Places' documentation to build from
        StringBuilder gpURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        // print current co-ordinates
        Log.d(TAG, "getUrl: La: " + la + " Ln: " + ln);
        // build string and return constructed url
        gpURL.append("location="+la+","+ln);
        gpURL.append("&radius="+PROXIMITY_RADIUS);
        gpURL.append("&type="+nearbyPlace);
        gpURL.append("&sensor=true");
        gpURL.append("&key="+PLACES_API_KEY);
        Log.d(TAG, "getUrl: Successfully constructed URL: " + gpURL.toString());
        return gpURL.toString();
    }

    // execute the GetNearbyPlaces class, using the url
    private void executeNearbyPlaces() {
        // clear the map
        mMap.clear();
        // assign the latitude/longitude based on current location
        double la = curr.latitude;
        double ln = curr.longitude;
        // type of place to look for
        String gasStation = "gas_station";
        // retrieve the constructed google places url
        String url = getUrl(la, ln, gasStation);
        // data transfer (?)
        Object dt[] = new Object[2];
        dt[0] = mMap;
        dt[1] = url;

        // exectue nearby places
        Log.d(TAG, "executeNearbyPlaces: Finding nearby places");
        GetNearbyPlaces gnp = new GetNearbyPlaces();
        gnp.execute(dt);
        // adjust camera
        Log.d(TAG, "executeNearbyPlaces: Moving camera to " + curr.latitude + ", " + curr.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curr, NEARBY_PLACES_ZOOM));
    }

    // --------------------------- Location Permissions --------------------------- //
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
        Log.d(TAG, "onRequest: " + grantResults[0] + "     " + grantResults[1]);
        switch(requestCode) {
            case LOCATION_REQUEST_CODE:
                // if both permissions are granted = true
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        locationPermGranted = false;
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                locationPermGranted = true;
                // since true, make the map
                makeMap();
        }
    }

    // --------------------------- Database/Dataflow Methods --------------------------- //
    // displays the entries within the db
    public static void showDB(String tableName) {
        String query = "SELECT * FROM " + tableName;
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        try {
            Cursor curr = rdb.rawQuery(query, null);
            if (curr != null) {
                curr.moveToFirst();
                do {
                    for (int i = 0; i < curr.getColumnCount(); i++) {
                        if (tableName == PricesEntry.TABLE_NAME) {
                            Log.d(TAG, "GasDB Data Display: PricesEntry: " + curr.getString(i));
                        }
                        if (tableName == TripsEntry.TABLE_NAME) {
                            Log.d(TAG, "GasDB Data Display: TripsEntry: " + curr.getString(i));
                        }
                        if (tableName == VehiclesEntry.TABLE_NAME) {
                            Log.d(TAG, "GasDB Data Display: VehiclesEntry: " + curr.getString(i));
                        }
                    }
                } while (curr.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "showDB: Exception thrown: " + e.getMessage());
        } finally {
            rdb.close();
        }
    }
    // check for duplicates in a particular table and column
    public static boolean checkDuplicate(String tableName, String columnName, String item) {
        int count = -1;
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {item});
        if(curr.moveToFirst()) {
            count = curr.getInt(0);
        }
        rdb.close();
        return count > 0;
    }
    // check if there is a vehicle profile
    public static boolean checkForProfile() {
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT * FROM " + VehiclesEntry.TABLE_NAME;
        Cursor curr = rdb.rawQuery(query, null);
        if (curr.moveToFirst()) {
            return true;
        }
        return false;
    }
    // insert gas entry to the db
    public static void insertGasEntry(String name, String address) {
        if (!checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_STATIONADDRESS, address)) {
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(PricesEntry.COLUMN_NAME_STATIONNAME, name);
            cv.put(PricesEntry.COLUMN_NAME_STATIONADDRESS, address);
            cv.put(PricesEntry.COLUMN_NAME_PRICE, "0");
            cv.put(PricesEntry.COLUMN_NAME_TIME, "00:00:00");
            wdb.insert(PricesEntry.TABLE_NAME, null, cv);
            wdb.close();
        } else {
            Log.d(TAG, "insertGasEntry: Record already exists");
        }
    }
    // insert trip entry to the db
    public static void insertTripEntry(String name, String address, String price, String amountFilled) {
        // retrieve current time
        Date currentTime = Calendar.getInstance().getTime();
        String time = format.format(currentTime);
        // assign totalCost
        float tempFilled = Float.parseFloat(amountFilled);
        float tempPrice = Float.parseFloat(price);
        float cost = tempPrice * tempFilled;
        String totalCost = String.format("%.01f", cost);
        // create writable database
        final SQLiteDatabase wdb = gpDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        // assign content values
        cv.put(TripsEntry.COLUMN_NAME_STATIONNAME, name);
        cv.put(TripsEntry.COLUMN_NAME_STATIONADDRESS, address);
        cv.put(TripsEntry.COLUMN_NAME_PRICE, price);
        cv.put(TripsEntry.COLUMN_NAME_TIME, time);
        cv.put(TripsEntry.COLUMN_NAME_AMOUNTFILLED, amountFilled);
        cv.put(TripsEntry.COLUMN_NAME_TOTALCOST, totalCost);
        wdb.insert(TripsEntry.TABLE_NAME, null, cv);
        showDB(TripsEntry.TABLE_NAME);
        wdb.close();
    }
    // insert vehicle entry to the db
    public static void insertVehicleEntry(String name, String make, String model, String year, String fe, String tank) {
        // create writable database
        final SQLiteDatabase wdb = gpDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        // assign content values
        cv.put(VehiclesEntry.COLUMN_NAME_VEHICLENAME, name);
        cv.put(VehiclesEntry.COLUMN_NAME_VEHICLEMAKE, make);
        cv.put(VehiclesEntry.COLUMN_NAME_VEHICLEMODEL, model);
        cv.put(VehiclesEntry.COLUMN_NAME_VEHICLEYEAR, year);
        cv.put(VehiclesEntry.COLUMN_NAME_FUELEFFICIENCY, fe);
        cv.put(VehiclesEntry.COLUMN_NAME_MAXTANK, tank);
        wdb.insert(VehiclesEntry.TABLE_NAME, null, cv);
        showDB(VehiclesEntry.TABLE_NAME);
        wdb.close();
    }
    // retrieve the price for a given gas station
    public static String retrieveGasPrice(String address) {
        String price = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_PRICE + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_STATIONADDRESS + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {address});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveGasPrice: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE)));
            price = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE));
        }
        rdb.close();
        return price;
    }
    // retrieve the name for a given gas station
    public static String retrieveGasName(String address) {
        String name = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_STATIONNAME + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_STATIONADDRESS + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {address});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveGasName: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_STATIONNAME)));
            name = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_STATIONNAME));
        }
        rdb.close();
        return name;
    }
    // retrieve vehicle data from specific column
    public static void retrieveVehicleData(String name) {
        // query db
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT * FROM " + VehiclesEntry.TABLE_NAME + " WHERE " + VehiclesEntry.COLUMN_NAME_VEHICLENAME + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {name});
        // set elements and pass them to view dialog in vehicle profiles
        if (curr.moveToFirst()) {
            String vehicleName = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLENAME));
            String vehicleMake = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEMAKE));
            String vehicleModel = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEMODEL));
            String vehicleYear = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEYEAR));
            String vehicleFE = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_FUELEFFICIENCY));
            String vehicleTankSize = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_MAXTANK));
            ViewVehicleProfileDialog.setVehicleName(vehicleName);
            ViewVehicleProfileDialog.setVehicleMake(vehicleMake);
            ViewVehicleProfileDialog.setVehicleModel(vehicleModel);
            ViewVehicleProfileDialog.setVehicleYear(vehicleYear);
            ViewVehicleProfileDialog.setVehicleFE(vehicleFE);
            ViewVehicleProfileDialog.setVehicleTankSize(vehicleTankSize);
        }
        curr.close();
    }
    // --------------------------- Trip Data and Methods --------------------------- //
    public void startTrip() {
        // pass dialog data
        String price = retrieveGasPrice(globalAddress);
        String name = retrieveGasName(globalAddress);
        float distance = calcDistance(curr, station);
        // round string to one decimal place
        String stringDistance = String.format("%.01f", distance);
        // instantiate dialog with data
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setPassedName(name);
        dialog.setAddress(globalAddress);
        dialog.setPrice(price);
        dialog.setDistance(stringDistance);
        dialog.show(getSupportFragmentManager(), "Confirm Dialog");
    }

    // --------------------------- Utility Methods --------------------------- //
    // called after a text entry is made
    public void hideMobileKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(locationSearch.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    // method which uses the formula of converting dp to px
    // used to scale the .setPadding method so the google logo can show in the same place relative to device size
    // ie: padding size for google logo is relative
    public int paddingConversion(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (dp * scale + 0.5f);
        return padding;
    }

    // if back is pressed, clear dialogs from main screen
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called.");
        super.onBackPressed();
        nv.setCheckedItem(R.id.mi_main);
        if (isClicked == true) {
            if(pDialog.isShowing())
                pDialog.dismiss();
        }
        isClicked = false;
    }
}
