package com.example.gastrak;

// Jacob Pauls
// 300273666
// FirebaseOperations.java

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.example.gastrak.MapsActivity.placesClient;

public class FirebaseOperations {

    // hold name
    private static final String TAG = "FirebaseOperations";

    // firebase connections and global references
    private static FirebaseDBConnection firebaseConnection;
    private static CollectionReference collection;

    // object to hold new gas station for inserts
    private static GasStationObject newGasStation;

    public static void insertGasStationToFirebase(final GasStationObject gasStation) {
        firebaseConnection = new FirebaseDBConnection();
        collection = firebaseConnection.getCollectionReference("GasStations");
        Map<String,Object> dataInsert = new HashMap<>();
        // extract data for commit to firebase
        // Plus Code
        String placePlusCode = gasStation.getPlusCode();
        dataInsert.put("Plus Code", placePlusCode);
        // ID
        String placeId = gasStation.getId();
        dataInsert.put("ID",placeId);
        // Name
        String placeName = gasStation.getName();
        dataInsert.put("Name",placeName);
        // Address
        String placeAddress = gasStation.getAddress();
        dataInsert.put("Address",placeAddress);
        // LatLng
        double placeLat = gasStation.getLatitude();
        dataInsert.put("Latitude",placeLat);
        double placeLng = gasStation.getLongitude();
        dataInsert.put("Longitude", placeLng);
        // Phone Number
        String placePhoneNumber = gasStation.getPhoneNumber();
        dataInsert.put("Phone Number",placePhoneNumber);
        // Ratings
        Double placeRating = gasStation.getRating();
        dataInsert.put("Rating",placeRating);
        // User Ratings Total
        double placeUserRatingsTotal = gasStation.getUserRatingsTotal();
        dataInsert.put("User Ratings Total",placeUserRatingsTotal);
        // URI (non-unique)
        String placeWebsiteURI = gasStation.getUri();
        dataInsert.put("URI",placeWebsiteURI);
        collection.document(placeId).set(dataInsert)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "insertGasStationToFirebase: Gas station committed to Firebase:" + gasStation);
                        Log.d(TAG, "insertGasStationToFirebase onSucceessListener: Document Inserted to Firebase Successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "insertGasStationToFirebase: Commit to Firebase instance failed.");
                    }
                });
    }

    public static void retrievePlaceAndInsertToFirebase(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                Place.Field.PLUS_CODE, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.WEBSITE_URI);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                Place place = fetchPlaceResponse.getPlace();
                Log.d(TAG, "retrievePlaceAndInsertToFirebase: Place Object Returned: " + place);
                newGasStation = new GasStationObject();
                newGasStation.setId(place.getId());
                newGasStation.setName(place.getName());
                newGasStation.setAddress(place.getAddress());
                newGasStation.setLatitude(place.getLatLng().latitude);
                newGasStation.setLongitude(place.getLatLng().longitude);
                newGasStation.setPhoneNumber(place.getPhoneNumber());
                newGasStation.setPlusCode(place.getPlusCode().getGlobalCode());
                newGasStation.setRating(place.getRating());
                String stationUri;
                if (place.getWebsiteUri() == null) {
                    stationUri = "na";
                } else {
                    stationUri = place.getWebsiteUri().toString();
                }
                newGasStation.setUri(stationUri);
                newGasStation.setUserRatingsTotal(place.getUserRatingsTotal());
                newGasStation.setPrice(0);
                newGasStation.setTime("00:00:00");
                insertGasStationToFirebase(newGasStation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int status = apiException.getStatusCode();
                    Log.e(TAG, "retrievePlaceAndInsertToFirebase: Place Not Found. Error: " + status + " Message: " + apiException.getMessage());
                }
            }
        });
    }

    public static void retrievePlaceAndThenUpdateSQLite(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                Place.Field.PLUS_CODE, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.WEBSITE_URI);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                Place place = fetchPlaceResponse.getPlace();
                Log.d(TAG, "retrievePlaceAndThenUpdateSQLite: Place Object Returned: " + place);
                newGasStation = new GasStationObject();
                newGasStation.setId(place.getId());
                newGasStation.setName(place.getName());
                newGasStation.setAddress(place.getAddress());
                newGasStation.setLatitude(place.getLatLng().latitude);
                newGasStation.setLongitude(place.getLatLng().longitude);
                newGasStation.setPhoneNumber(place.getPhoneNumber());
                newGasStation.setPlusCode(place.getPlusCode().getGlobalCode());
                newGasStation.setRating(place.getRating());
                String stationUri;
                if (place.getWebsiteUri() == null) {
                    stationUri = "na";
                } else {
                    stationUri = place.getWebsiteUri().toString();
                }
                newGasStation.setUri(stationUri);
                newGasStation.setUserRatingsTotal(place.getUserRatingsTotal());
                newGasStation.setPrice(0);
                newGasStation.setTime("00:00:00");
                Log.d(TAG, "retrievePlaceAndThenUpdateSQLite: GasStationObject Returned: " + newGasStation.toString());
                MapsActivity.updateGasStationInSQLite(newGasStation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int status = apiException.getStatusCode();
                    Log.e(TAG, "retrievePlaceAndThenUpdateSQLite: Place Not Found. Error: " + status + " Message: " + apiException.getMessage());
                }
            }
        });
    }

    //
//    public static GasStationObject returnGasStationObjectFromFirebase(String placeId) {
//        firebaseConnection = new FirebaseDBConnection();
//        collection = firebaseConnection.getCollectionReference("GasStations");
//        // Retrieve fields from Firebase
//        Query query = collection.whereEqualTo("ID",placeId);
//        query.get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                newGasStation = new GasStationObject();
//                                Log.d(TAG, "returnGasStationObjectFromFirebase: document: " + document.getId() + " => " + document.getData());
//                                newGasStation.setId(document.getData().get("ID").toString());
//                                newGasStation.setName(document.getData().get("Name").toString());
//                                newGasStation.setAddress(document.getData().get("Address").toString());
//                                newGasStation.setLatitude(document.getDouble("Latitude"));
//                                newGasStation.setLongitude(document.getDouble("Longitude"));
//                                newGasStation.setPhoneNumber(document.getData().get("Phone Number").toString());
//                                newGasStation.setPlusCode(document.getData().get("Plus Code").toString());
//                                newGasStation.setRating(document.getDouble("Rating"));
//                                newGasStation.setUri(document.getData().get("URI").toString());
//                                newGasStation.setUserRatingsTotal(document.getDouble("User Ratings Total"));
//                                newGasStation.setPrice(0);
//                                newGasStation.setTime("00:00:00");
//                                Log.d(TAG, "returnGasStationObjectFromFirebase: newGasStation Uploaded From Firebase: " + newGasStation.toString());
//                            }
//                        }
//                    }
//                });
//        return newGasStation;
//    }
}
