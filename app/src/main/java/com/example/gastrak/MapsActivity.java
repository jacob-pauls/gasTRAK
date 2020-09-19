package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: MapsActivity.java

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.gastrak.GasStationFeatures.isSearch;
import static com.example.gastrak.MainActivity.isClicked;
import static com.example.gastrak.MainActivity.pDialog;


public class MapsActivity extends AppCompatActivity {

    // --------------------------- Variables and Constants --------------------------- //
    // hold name
    private static final String TAG = "MapsActivity";

    // default user
    public static final String DEFAULT_USER = "jake_pauls";

    // global variables to hold device location and permission data
    private static final String F_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String C_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 909;
    private static final float NORMAL_ZOOM = 15f;
    private static final float NEARBY_PLACES_ZOOM = 12f;
    private static final int PROXIMITY_RADIUS = 5000;
    // put API key below (enabled for Google Places API)
    // can be the same one as entered in the google_maps_api.xml file
    private static final String PLACES_API_KEY = "";

    // booleans
    public static boolean locationPermGranted = false;
    private static boolean isStartup = true;
    private boolean createdIntent = false;
    public static boolean executingPlaces = false;
    public static boolean onLoadExecutePlaces = false;

    // objects {maps, GUI}
    private static GoogleMap mMap;
    private DrawerLayout drawer;
    public static PlacesClient placesClient;

    // connection to the node controller to perform refreshes on the MySQL
    private MySQLNodeConnectionController nodeController = null;

    // hash map to store place ids for nearby places searches
    private static HashMap<LatLng,String> nearbyPlacesPlaceIdHashMap = new HashMap<>();

    // date format
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss z");

    //  objects used to find location
    private FusedLocationProviderClient mFLPC;
    private static LatLng currentLocation;
    // assign gas stationLatLng LatLng value globally
    private static LatLng stationLatLng;

    // icons and features
    private ImageView hMenuIcon;
    private ImageView fLocationIcon;
    private ImageView nStationIcon;
    private ImageView zInIcon;
    private ImageView zOutIcon;
    private Button startTripButton;
    public static NavigationView nv;
    private Intent navIntent;

    // variable to globally hold an address
    private static String globalAddress;
    // variable to globally hold plusCode
    public static String globalPlaceId;
    // variably to globally hold a name
    private static String stationName;

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
        // perform refresh
        if (mMap != null) {
            ArrayList<String> gasStationPlaceIds = returnListOfGasStations();
            refreshAll(gasStationPlaceIds);
        }
        // initialize
        init();
    }

    // everytime we return to the activity check if there is a profile
    // register or unregister the broadcast receiver for MySQL requests
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: onResume Called.");
        // if there isn't a profile, or there isn't a default set. throw dialog
        if (!checkForProfile()) {
            // cant make searches without a profile
            nStationIcon.setEnabled(false);
            nStationIcon.setClickable(false);
            // show dialog
            ProfileErrorDialog ped = new ProfileErrorDialog();
            ped.show(getSupportFragmentManager(), "Profile Error Dialog");
        } else {
            nStationIcon.setEnabled(true);
            nStationIcon.setClickable(true);
        }
        // listen for node broadcasts
        nodeController = new MySQLNodeConnectionController(this);
        // refresh all gas station prices
        if (mMap != null) {
            ArrayList<String> gasStationPlaceIds = returnListOfGasStations();
            refreshAll(gasStationPlaceIds);
        }
    }

    @Override
    protected void onPause() {
        if (nodeController != null) {
            nodeController.unregisterReceiver(this);
            nodeController = null;
        }
        super.onPause();
    }

    // --------------------------- Initialization Methods --------------------------- //
    // method to initialize and perform tasks for GUI components
    private void init() {
        Log.d(TAG, "init: Initializing...");
        // initialize places client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), PLACES_API_KEY);
        }
        placesClient = Places.createClient(getApplicationContext());
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
                startTripButton.setVisibility(View.INVISIBLE);
            }
        });
        // find closest gas stations listener
        nStationIcon = findViewById(R.id.nearbyStationIcon);
        nStationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked nearby gas stationLatLng icon");
                executingPlaces = true;
                executeNearbyPlaces();
                startTripButton.setVisibility(View.INVISIBLE);
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
        // start trip button (if a marker is clicked)
        startTripButton = findViewById(R.id.startTripButton);
        startTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Trip Started.");
                GasStationFeatures.findGasStation(MapsActivity.this, stationLatLng, stationName);
                startTrip();
            }
        });
        // CHECK IF THERE IS A VEHICLE PROFILE ONCE GUI IS SET
        // if there is no vehicle profile
        Log.d(TAG, "init: Checking for vehicle profile...");
        if (!checkForProfile()) {
            // cant make searches without a profile
            nStationIcon.setEnabled(false);
            nStationIcon.setClickable(false);
            // show dialog
            ProfileErrorDialog ped = new ProfileErrorDialog();
            ped.show(getSupportFragmentManager(), "Profile Error Dialog");
        }
    }

    // --------------------------- Map Features --------------------------- //
    // perform camera updates on a particular map
    // perform camera updates using the desired latitude and longitude
    // drops a marker if the location is not the user's current location
    public static void updateCamera(GoogleMap map, LatLng laln, float zoom) {
        Log.d(TAG, "updateCameraL Clearing markers ");
        if (isStartup)
            map.clear();
        if (isSearch)
            map.clear();
        Log.d(TAG, "updateCamera: Moving camera to " + laln.latitude + ", " + laln.longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
    }
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
        Log.d(TAG, "updateCamera: Clearing markers ");
        if (isStartup)
            mMap.clear();
        if (isSearch)
            mMap.clear();
        Log.d(TAG, "updateCamera: Moving camera to " + laln.latitude + ", " + laln.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(laln, zoom));
        // drop a marker
        dropMarker(laln, title, snippet);
    }

    // ------ dropMarker() for Individual Place requests ------ //

    // overloaded marker method for markers with snippets
    public static void dropMarker(LatLng laln, String title, String snippet) {
    // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            mMap.clear();
            String tempSnippet;
            // assign global values for other methods
            globalAddress = snippet;
            stationLatLng = laln;

            /* For Individual Station Requests
                - Insert to MySQL (server won't commit if the record exists)
                - Insert to SQLite
                - Refresh gas station from MySQL (if applicable)
                - Use the SQLite data to populate markers (avoid async issues)
                - Update SQLite after so data is up to data
                - Insert record to Firebase for storage
             */
            // submit request to insert gas station to mySQL (server-side handles duplicates)
            MySQLController mySQLController = new MySQLController(ContextWorkaround.getContext());
            mySQLController.insertGasStationToMySQLDatabase(globalPlaceId, "0", DEFAULT_USER);
            // insert to SQLite (avoid asynchronous issues)
            basicInsertGasStationToSQLite(globalPlaceId, title, snippet);
            // retrieve current price (if applicable)
            mySQLController.refreshGasStationFromMySQLDatabase(globalPlaceId);
            // update SQLite via places object
            FirebaseOperations.retrievePlaceAndThenUpdateSQLite(globalPlaceId);
            // then update via places object
            FirebaseOperations.retrievePlaceAndInsertToFirebase(globalPlaceId);

            // append price to snippet
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);

            /*
             retrieve the gas price,
             - at the moment this fails if a new price is uploaded on the first try due to async issues
             - on second load the gas price is present
             - async performs the SQLite query before the database finishes the get request
             */
            String price = retrieveGasPriceFromPlaceId(globalPlaceId);
            String user = retrieveUserUploadInfoWithPlaceId(globalPlaceId);
            String date = retrieveDateModifiedWithPlaceId(globalPlaceId);
            tempSnippet = " \nPrice : $" + price + " /L" ;
            tempSnippet += "\nUploaded By : " + user;
            tempSnippet += "\nLast Updated : " + date;
            String newSnippet = snippet + tempSnippet;
            mOptions.snippet(newSnippet);
            if (!executingPlaces) {
                Marker marker = mMap.addMarker(mOptions);
                marker.setTag(globalPlaceId);
            } else {
                // make comparison between self and gas stationLatLng, store values in arraylist
                Double totalCost = GasStationComparison.calculateGasStationCostValue(currentLocation, laln, globalPlaceId);
                hashMapMarkers.put(mOptions, totalCost);
                Log.d(TAG, "dropMarker: hashMapMarkers: " + hashMapMarkers.get(mOptions));
            }
            showDB(PricesEntry.TABLE_NAME);
        }
    }

    // ------ dropMarker() for widespread Place requests ------ //

    // overloaded marker method for markers with snippets and to insert to firebase using placeId
    public static void dropMarker(LatLng laln, String title, String snippet, String placeId) {
        // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            // add placeId to the hash map
            nearbyPlacesPlaceIdHashMap.put(laln, placeId);

            /* For Places Requests
                - Insert to MySQL (server won't commit if the record exists)
                - Insert to SQLite
                - Refresh gas station from MySQL (if applicable)
                - Use the SQLite data to populate markers (avoid async issues)
                - Update SQLite after so data is up to date
                - Insert record to Firebase for storage
             */
            // submit request to insert gas station to mySQL (server-side handles duplicates)
            MySQLController mySQLController = new MySQLController(ContextWorkaround.getContext());
            mySQLController.insertGasStationToMySQLDatabase(placeId, "0", DEFAULT_USER);
            // insert to SQLite (avoid asynchronous issues)
            basicInsertGasStationToSQLite(placeId, title, snippet);
            // retrieve current price (if applicable)
            mySQLController.refreshGasStationFromMySQLDatabase(placeId);
            // update SQLite via places object
            FirebaseOperations.retrievePlaceAndThenUpdateSQLite(placeId);
            // insert to firebase
            FirebaseOperations.retrievePlaceAndInsertToFirebase(placeId);

            String tempSnippet;

            // assign global values for other methods
            globalPlaceId = placeId;
            globalAddress = snippet;
            stationLatLng = laln;
            Log.d(TAG, "dropMarker: retrievePlaceAndThenUpdateSQLite: placeId: " + globalPlaceId);
            // append price to snippet
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            /*
             retrieve the gas price,
             - at the moment this fails if a new price is uploaded on the first try due to async issues
             - on second load the gas price is present
             - async performs the SQLite query before the database finishes the get request
             */
            String price = retrieveGasPriceFromPlaceId(placeId);
            String user = retrieveUserUploadInfoWithPlaceId(placeId);
            String date = retrieveDateModifiedWithPlaceId(placeId);
            tempSnippet = " \nPrice : $" + price + " /L" ;
            tempSnippet += "\nUploaded By : " + user;
            tempSnippet += "\nLast Updated : " + date;
            String newSnippet = snippet + tempSnippet;
            mOptions.snippet(newSnippet);
            if (!executingPlaces) {
                Marker marker = mMap.addMarker(mOptions);
                marker.setTag(globalPlaceId);
            } else {
                // make comparison between self and gas stationLatLng, store values in arraylist
                Double totalCost = GasStationComparison.calculateGasStationCostValue(currentLocation, laln, globalPlaceId);
                hashMapMarkers.put(mOptions, totalCost);
                Log.d(TAG, "dropMarker: hashMapMarkers: " + hashMapMarkers.get(mOptions));
            }
            showDB(PricesEntry.TABLE_NAME);
        }
    }

    // ------ Misc. dropMarker() methods ------ //

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

    // overloaded drop marker for a particular hue
    public static void dropMarker(LatLng laln, String title, String snippet, float hue) {
    // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            mOptions.snippet(snippet);
            mOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
            String placeId = nearbyPlacesPlaceIdHashMap.get(laln);
            Log.d(TAG, "dropMarker: placeId: " + placeId);
            mMap.addMarker(mOptions).setTag(placeId);
        }
    }

    // overloaded drop marker for a particular map
    public static void dropMarker(GoogleMap map, LatLng laln, String title, String snippet, float hue) {
        // if we do a non-startup update, drop a marker
        if (!title.equals("Current Location")) {
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title(title);
            mOptions.position(laln);
            mOptions.snippet(snippet);
            mOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
            String placeId = nearbyPlacesPlaceIdHashMap.get(laln);
            Log.d(TAG, "dropMarker: placeId: " + placeId);
            map.addMarker(mOptions).setTag(placeId);
        }
    }

    // ------ Basic Map Operations ------ //
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
                        int topPadding = paddingConversion(10);
                        int rightPadding = paddingConversion(10);
                        int bottomPadding = paddingConversion(10);
                        mMap.setPadding(leftPadding,topPadding,rightPadding,bottomPadding);
                        // set the information window for each marker
                        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                        // set a listener to watch for marker info window clicks
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Log.d(TAG, "onInfoWindowClick: Info Window Clicked: " + marker.getSnippet());
                                GasStationInfoWindowDialog giwd = new GasStationInfoWindowDialog();
                                // split the snippet
                                String[] snippetEdit = marker.getSnippet().split("\n");
                                giwd.setGasStation(marker.getTitle() + ", " + snippetEdit[0]);
                                giwd.setPlaceId(marker.getTag().toString());
                                giwd.show(getSupportFragmentManager(), "Gas Station Info Window Dialog");
                            }
                        });
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                Log.d(TAG, "onMarkerClick: Marker Clicked: " + marker.getPosition() + " Marker ID: " + marker.getTag());
                                startTripButton.setVisibility(View.VISIBLE);
                                stationLatLng = marker.getPosition();
                                stationName = marker.getTitle();
                                return false;
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
                            MapsActivity.currentLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            updateCamera(MapsActivity.currentLocation, NORMAL_ZOOM, "Current Location");
                            // configure the autocomplete handler if profile is created
                            if (checkForProfile())
                                locationAutocompleteHandler();
                            isStartup = false;
                            mMap.clear();
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

    // execute the GetNearbyPlaces class, using the url
    private void executeNearbyPlaces() {
        // clear the map
        mMap.clear();
        // assign the latitude/longitude based on current location
        double la = currentLocation.latitude;
        double ln = currentLocation.longitude;
        // type of place to look for
        String gasStation = "gas_station";
        // retrieve the constructed google places url
        GooglePlacesURL gpURL = new GooglePlacesURL(la, ln, PROXIMITY_RADIUS, gasStation, true, PLACES_API_KEY);
        String url = gpURL.returnGooglePlacesURLForNearbySearch();
        // data transfer (?)
        Object dt[] = new Object[2];
        dt[0] = mMap;
        dt[1] = url;

        // exectue nearby places
        Log.d(TAG, "executeNearbyPlaces: Finding nearby places");
        GetNearbyPlaces gnp = new GetNearbyPlaces();
        gnp.execute(dt);
        // adjust camera
        Log.d(TAG, "executeNearbyPlaces: Moving camera to " + currentLocation.latitude + ", " + currentLocation.longitude);
        // if the places execution is onload perform a normal zoom
        if (onLoadExecutePlaces) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, NORMAL_ZOOM));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, NEARBY_PLACES_ZOOM));
        }
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

    // --------------------------- SQLite --------------------------- //

    // --------------------------- Utility Operations --------------------------- //

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
            Log.d(TAG, "checkDuplicate: item: " + item);
            count = 1;
            Log.d(TAG, "checkDuplicate: count: " + count);
        }
        rdb.close();
        return count > 0;
    }
    // check if there is a vehicle profile
    public static boolean checkForProfile() {
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT * FROM " + VehiclesEntry.TABLE_NAME + " WHERE " + VehiclesEntry.COLUMN_NAME_SETASDEFAULT + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {"1"});
        if (curr.moveToFirst()) {
            Log.d(TAG, "checkForProfile: Returned true");
            return true;
        }
        Log.d(TAG, "checkForProfile: Returned false");
        return false;
    }

    // --------------------------- Inserts/Updates to Gas Stations --------------------------- //

    public static void basicInsertGasStationToSQLite(String placeId, String name, String address) {
        if (!checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_GASID, placeId)) {
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(PricesEntry.COLUMN_NAME_GASID, placeId);
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

    public static void updateGasStationInSQLite(GasStationObject newGasStation) {
        if (checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_GASID, newGasStation.getId())) {
            String whereClause = PricesEntry.COLUMN_NAME_GASID + "= ?";
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(PricesEntry.COLUMN_NAME_GASID, newGasStation.getId());
            cv.put(PricesEntry.COLUMN_NAME_STATIONNAME, newGasStation.getName());
            cv.put(PricesEntry.COLUMN_NAME_STATIONADDRESS, newGasStation.getAddress());
            cv.put(PricesEntry.COLUMN_NAME_LATITUDE, newGasStation.getLatitude());
            cv.put(PricesEntry.COLUMN_NAME_LONGITUDE, newGasStation.getLongitude());
            cv.put(PricesEntry.COLUMN_NAME_PHONENUMBER, newGasStation.getPhoneNumber());
            cv.put(PricesEntry.COLUMN_NAME_PLUSCODE, newGasStation.getPlusCode());
            cv.put(PricesEntry.COLUMN_NAME_RATING, newGasStation.getRating());
            cv.put(PricesEntry.COLUMN_NAME_URI, newGasStation.getUri());
            cv.put(PricesEntry.COLUMN_NAME_USERRATINGSTOTAL, newGasStation.getUserRatingsTotal());
            cv.put(PricesEntry.COLUMN_NAME_PRICE, newGasStation.getPrice());
            cv.put(PricesEntry.COLUMN_NAME_TIME, newGasStation.getTime());
            Log.d(TAG, "updateGasStationInSQLite: Gas Station updated in SQLite: " + newGasStation.toString());
            wdb.update(PricesEntry.TABLE_NAME, cv,whereClause,new String[]{newGasStation.getId()});
            wdb.close();

        }
    }

    // update called from the broadcast receiever to handle MySQL requests
    public void updateGasStationPriceInSQLite(String placeId, String price, String user, String time) {
        if (checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_GASID, placeId)) {
            String whereClause = PricesEntry.COLUMN_NAME_GASID + "= ?";
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            double intPrice = Double.parseDouble(price);
            cv.put(PricesEntry.COLUMN_NAME_PRICE, intPrice);
            cv.put(PricesEntry.COLUMN_NAME_UPLOADEDBY, user);
            cv.put(PricesEntry.COLUMN_NAME_TIME, time);
            Log.d(TAG, "updateGasStationPriceInSQLite: Gas price updated SQLite for placeId: " + placeId);
            wdb.update(PricesEntry.TABLE_NAME, cv, whereClause, new String[] {placeId});
            wdb.close();
        }
    }

    // --------------------------- Refresh All Methods From MySQL --------------------------- //

    public static ArrayList<String> returnListOfGasStations() {
        // hold all current gas stations in array list
        ArrayList<String> gasStationIds = new ArrayList<>();
        String query = "SELECT * FROM " + PricesEntry.TABLE_NAME;
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        Cursor curr = rdb.rawQuery(query, null);
        if (curr.moveToFirst()) {
            do {
                gasStationIds.add(curr.getString(0));
            } while (curr.moveToNext());
        } else {
            Log.d(TAG, "returnListOfGasStations: There are no gas stations in the SQLite Database");
        }
        // check results
        for (int i = 0; i < gasStationIds.size(); i++) {
            Log.d(TAG, "returnListOfGasStations: Loop check: " + gasStationIds.get(i));
        }
        curr.close();
        rdb.close();
        return gasStationIds;
    }

    public void refreshAll(ArrayList<String> gasStationPlaceIds) {
        // if empty execute a nearby places search to insert data
        if (gasStationPlaceIds.isEmpty()) {
            onLoadExecutePlaces = true;
            Toast.makeText(this, "Syncing gasTRAK data. Please wait...", Toast.LENGTH_LONG).show();
            executeNearbyPlaces();
        } else {
            MySQLController mySQLController = new MySQLController(ContextWorkaround.getContext());
            for (int i = 0; i < gasStationPlaceIds.size(); i++) {
                mySQLController.refreshGasStationFromMySQLDatabase(gasStationPlaceIds.get(i));
            }
        }
    }

    // --------------------------- Inserts/Updates to Trips/Vehicle Profiles --------------------------- //

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

        // send to MySQL for update as well
        MySQLController mySQLController = new MySQLController(ContextWorkaround.getContext());
        Log.d(TAG, "insertTripEntry: globalPlaceId: " + globalPlaceId);
        mySQLController.updateGasStationInMySQLDatabase(globalPlaceId, price, DEFAULT_USER);
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
        cv.put(VehiclesEntry.COLUMN_NAME_SETASDEFAULT, 0);
        wdb.insert(VehiclesEntry.TABLE_NAME, null, cv);
        showDB(VehiclesEntry.TABLE_NAME);
        wdb.close();
    }

    public static void updateVehicleEntry(String vehicleId) {
        if (checkDuplicate(VehiclesEntry.TABLE_NAME, VehiclesEntry.COLUMN_NAME_VEHICLEID, vehicleId)) {
            // set all vehicle profiles to 0
            String whereClause2 = VehiclesEntry.COLUMN_NAME_SETASDEFAULT + "= ?";
            final SQLiteDatabase wdb2 = gpDB.getWritableDatabase();
            ContentValues cv2 = new ContentValues();
            cv2.put(VehiclesEntry.COLUMN_NAME_SETASDEFAULT, 0);
            wdb2.update(VehiclesEntry.TABLE_NAME, cv2, whereClause2, new String[] {"1"});

            // set the default to 1
            String whereClause = VehiclesEntry.COLUMN_NAME_VEHICLEID + "= ?";
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(VehiclesEntry.COLUMN_NAME_SETASDEFAULT, 1);
            Log.d(TAG, "updateVehicleEntry: Vehicle default updated for vehicle: " + vehicleId);
            wdb.update(VehiclesEntry.TABLE_NAME, cv, whereClause, new String[] {vehicleId});
            wdb.close();

            showDB(VehiclesEntry.TABLE_NAME);
        }
    }

    // --------------------------- Retrievals to Trips/Vehicle Profiles --------------------------- //

    // retrieve the price for a given gas stationLatLng
    public static String retrieveGasPriceFromStationAddress(String address) {
        String price = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_PRICE + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_STATIONADDRESS + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {address});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveGasPriceFromStationAddress: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE)));
            price = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE));
        }
        rdb.close();
        return price;
    }
    public static String retrieveGasPriceFromPlaceId(String placeId) {
        String price = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_PRICE + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_GASID + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {placeId});
        Log.d(TAG, "retrieveGasPriceFromPlaceId: cursor: " + curr.toString());
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveGasPriceFromPlaceId: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE)));
            price = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_PRICE));
        }
        Log.d(TAG, "retrieveGasPriceFromPlaceId: price returned: " + price);
        rdb.close();
        return price;
    }
    // retrieve the name for a given gas stationLatLng
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
    // retrieve the name for a given gas station with a place id
    public static String retrieveGasNameWithPlaceId(String placeId) {
        String name = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_STATIONNAME + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_GASID + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {placeId});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveGasNameWithPlaceId: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_STATIONNAME)));
            name = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_STATIONNAME));
        }
        rdb.close();
        return name;
    }
    // retrieve the user uploaded from placeId
    public static String retrieveUserUploadInfoWithPlaceId(String placeId) {
        String name = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_UPLOADEDBY + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_GASID + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {placeId});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveUserUploadInfoWithPlaceId: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_UPLOADEDBY)));
            name = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_UPLOADEDBY));
        }
        rdb.close();
        return name;
    }
    // retrieve the user uploaded from placeId
    public static String retrieveDateModifiedWithPlaceId(String placeId) {
        String name = "";
        final SQLiteDatabase rdb = gpDB.getReadableDatabase();
        String query = "SELECT " + PricesEntry.COLUMN_NAME_TIME + " FROM " + PricesEntry.TABLE_NAME + " WHERE " + PricesEntry.COLUMN_NAME_GASID + " = ?";
        Cursor curr = rdb.rawQuery(query, new String[] {placeId});
        if (curr.moveToFirst()) {
            Log.d(TAG, "retrieveDateModifiedWithPlaceId: " + curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_TIME)));
            name = curr.getString(curr.getColumnIndex(PricesEntry.COLUMN_NAME_TIME));
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
            String vehicleId = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEID));
            String vehicleName = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLENAME));
            String vehicleMake = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEMAKE));
            String vehicleModel = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEMODEL));
            String vehicleYear = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_VEHICLEYEAR));
            String vehicleFE = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_FUELEFFICIENCY));
            String vehicleTankSize = curr.getString(curr.getColumnIndex(VehiclesEntry.COLUMN_NAME_MAXTANK));
            ViewVehicleProfileDialog.setVehicleId(vehicleId);
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
    // configures autocompletion results when searching for a gas station
    public void locationAutocompleteHandler() {
        // initialize fragment and values returned by the API
        final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocompleteFragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                Place.Field.PLUS_CODE, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.WEBSITE_URI));
        autocompleteSupportFragment.setHint("Enter a gas stationLatLng");
        // set the bounds for the autocompletion of results
        RectangularBounds rectangularBounds = generateLatLngBounds(currentLocation, 10000);
        autocompleteSupportFragment.setLocationBias(rectangularBounds);
        autocompleteSupportFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        autocompleteSupportFragment.setMenuVisibility(false);
        // listen for selection
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // find the place selected
                // set visibility on trip button to invisible
                startTripButton.setVisibility(View.INVISIBLE);
                // assign place id
                globalPlaceId = place.getId();
                final LatLng latLng = place.getLatLng();
                GasStationFeatures.retrieveAndSendPlaceObjectData(place);
                Log.d(TAG, "AutocompleteSupportFragment: onPlaceSelected: Lat: " + latLng.latitude + ", Lng: " + latLng.longitude + ", Address: " + place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                // error handling
                Toast.makeText(MapsActivity.this, "Gas Station Search Failed. Status Code: " + status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startTrip() {
        Log.d(TAG, "startTrip: check globalPlaceId: " + globalPlaceId);
        Log.d(TAG, "startTrip: check globalAddress: " + globalAddress);
        // pass dialog data
        String price = retrieveGasPriceFromStationAddress(globalAddress);
        String name = retrieveGasName(globalAddress);
        float distance = calcDistance(currentLocation, stationLatLng);
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

    // --------------------------- General Utility Methods --------------------------- //
    // method which uses the formula of converting dp to px
    // used to scale the .setPadding method so the google logo can show in the same place relative to device size
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

    // auto generate latlng bounds to limit AutoSuggestions based on current location
    public RectangularBounds generateLatLngBounds(LatLng currentLocation, double radiusInMeters) {
        double distanceFromCenterToCornerOfMap = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner = SphericalUtil.computeOffset(currentLocation, distanceFromCenterToCornerOfMap, 225.0);
        LatLng northeastCorner = SphericalUtil.computeOffset(currentLocation, distanceFromCenterToCornerOfMap, 45.0);
        LatLngBounds latLngBounds = new LatLngBounds(southwestCorner, northeastCorner);
        RectangularBounds rectangularBounds = RectangularBounds.newInstance(latLngBounds.southwest, latLngBounds.northeast);
        return rectangularBounds;
    }
}
