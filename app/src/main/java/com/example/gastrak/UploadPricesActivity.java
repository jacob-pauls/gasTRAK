package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: UploadPricesActivity.java

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.gastrak.MapsActivity.executingPlaces;
import static com.example.gastrak.MapsActivity.gpDB;
import static com.example.gastrak.MapsActivity.nv;

public class UploadPricesActivity extends AppCompatActivity {

    // hold name
    private static final String TAG = "UploadPricesActivity";
    // put API key below (enabled for Google Places API)
    // can be the same one as entered in the google_maps_api.xml file
    private static final String PLACES_API_KEY = "";
    // places radius
    private static final int PROXIMITY_RADIUS = 5000;
    private static final float NEARBY_PLACES_ZOOM = 12f;

    // boolean to control which fragment markers are added to
    static boolean addMarkersToMapFragment = false;

    // icons and features
    private ImageView backButton;
    private Button addDBEntry;
    private EditText addressEntry;
    private EditText fuelEntry;
    private ImageView mapFragmentZoomInIcon;
    private ImageView mapFragmentZoomOutIcon;

    // connection to the node controller to perform refreshes on the MySQL
    private MySQLNodeConnectionController nodeController = null;

    // value to hold current marker id
    private String selectedMarkerId;

    // zoom
    private static final float NORMAL_ZOOM = 15f;

    //  objects used to find location
    public static GoogleMap theMapFragment;
    private FusedLocationProviderClient mFLPC;
    private static LatLng curr;


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
        mapFragmentZoomInIcon = findViewById(R.id.mapFragmentZoomInIcon);
        mapFragmentZoomInIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Zoom increased");
                theMapFragment.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        mapFragmentZoomOutIcon = findViewById(R.id.mapFragmentZoomOutIcon);
        mapFragmentZoomOutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Zoom decreased");
                theMapFragment.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        makeMapFragment();
    }

    // --------------------------- Map Fragment Methods and Nearby Places Methods --------------------------- //
    private void makeMapFragment() {
        // refer to map
        Log.d(TAG, "makeMapFragment: Making map");
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                theMapFragment = googleMap;
                Log.d(TAG, "onMapReady: Map is ready!");
                Toast.makeText(UploadPricesActivity.this, "MapFragment Loaded!", Toast.LENGTH_LONG).show();

                if(MapsActivity.locationPermGranted) {
                    // adjust camera to current location
                    getCurrentLocationForFragment();
                    // mark current location for user
                    try {
                        // enable default google maps marker
                        theMapFragment.setMyLocationEnabled(true);
                        Log.d(TAG, "setMyLocationEnabled: true");
                        theMapFragment.getUiSettings().setMyLocationButtonEnabled(false);
                        // set the information window for each marker
                        theMapFragment.setInfoWindowAdapter(new CustomInfoWindowAdapter(UploadPricesActivity.this));
                        // set a listener to watch for marker clicks
                        theMapFragment.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                addressEntry.setText(marker.getSnippet());
                                Log.d(TAG, "onMarkerClick: Marker Clicked: " + marker.getPosition() + " Marker ID: " + marker.getTag());
                                selectedMarkerId = marker.getTag().toString();
                                return false;
                            }
                        });
                        // set map padding
                        theMapFragment.setPadding(0,0,0,70);
                    } catch (SecurityException e) {
                        Log.e(TAG, "onMapReady: SecurityException: Failed to enable current location on map");
                    }
                }
            }
        });
    }

    private void getCurrentLocationForFragment() {
        Log.d(TAG, "getCurrentLocationForFragment: Getting the current location");
        mFLPC = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(MapsActivity.locationPermGranted) {
                Log.d(TAG, "getCurrentLocationForFragment: Location permissions passed");
                Task location = mFLPC.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"onComplete: Successfully found location");
                            Location currentLocation = (Location) task.getResult();
                            // update camera to match current location of the user
                            curr = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MapsActivity.updateCamera(theMapFragment, curr, NORMAL_ZOOM);
                            // call nearby places once map and location are configured
                            executingPlaces = true;
                            addMarkersToMapFragment = true;
                            callNearbyPlacesForMapFragment();
                        } else {
                            Log.d(TAG, "onComplete: Found location failed");
                            Toast.makeText(UploadPricesActivity.this, "Unable to find current location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getCurrentLocationForFragment: Security Exception:  " + e.getMessage());
        }
    }

    private void callNearbyPlacesForMapFragment() {
        // clear the map
        theMapFragment.clear();
        // assign the latitude/longitude based on current location
        double la = curr.latitude;
        double ln = curr.longitude;
        // type of place to look for
        String gasStation = "gas_station";
        // retrieve the constructed google places url
        GooglePlacesURL gpURL = new GooglePlacesURL(la, ln, PROXIMITY_RADIUS, gasStation,true, PLACES_API_KEY);
        String url = gpURL.returnGooglePlacesURLForNearbySearch();
        // data transfer (?)
        Object dt[] = new Object[2];
        dt[0] = theMapFragment;
        dt[1] = url;

        // exectue nearby places
        Log.d(TAG, "callNearbyPlacesForMapFragment: Finding nearby places");
        GetNearbyPlaces gnp = new GetNearbyPlaces();
        gnp.execute(dt);
        // adjust camera
        Log.d(TAG, "callNearbyPlacesForMapFragment: Moving camera to " + curr.latitude + ", " + curr.longitude);
        theMapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(curr, NEARBY_PLACES_ZOOM));
    }

    // --------------------------- Database/Dataflow Methods --------------------------- //
    // set the price for a given gas station
    public void setGasPrice(EditText addressEntry, EditText priceEntry) {
        Log.d(TAG, "setGasPrice: Setting gas price...");
        Log.d(TAG, "setGasPrice: current marker ID: " + selectedMarkerId);
        // retrieve address, price, and current time
        String price = priceEntry.getText().toString();
        Date currentTime = Calendar.getInstance().getTime();
        String time = format.format(currentTime);

        // if a result is shown, commit to update
        if (MapsActivity.checkDuplicate(PricesEntry.TABLE_NAME, PricesEntry.COLUMN_NAME_GASID, selectedMarkerId)) {
            // update global place id in MapsActivity
            MapsActivity.globalPlaceId = selectedMarkerId;
            // since data is here, write to SQLite database
            final SQLiteDatabase wdb = gpDB.getWritableDatabase();
            ContentValues cv = new ContentValues();
            // value set for price and time updates
            cv.put(PricesEntry.COLUMN_NAME_PRICE, price);
            cv.put(PricesEntry.COLUMN_NAME_TIME, time);
            Log.d(TAG, "setGasPrice: Performing price and time updates...");
            wdb.update(PricesEntry.TABLE_NAME, cv, PricesEntry.COLUMN_NAME_GASID + " = ?", new String[] {selectedMarkerId});
            wdb.close();
            // commit to mySQL
            MySQLController mySQLController = new MySQLController(getApplicationContext());
            mySQLController.updateGasStationInMySQLDatabase(selectedMarkerId, price, MapsActivity.DEFAULT_USER);
            // drop marker at the location
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

    // methods to handle the node controller for MySQL
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: onResume Called.");
        nodeController = new MySQLNodeConnectionController(this);
    }

    @Override
    protected void onPause() {
        if (nodeController != null) {
            nodeController.unregisterReceiver(this);
            nodeController = null;
        }
        super.onPause();
    }

}
