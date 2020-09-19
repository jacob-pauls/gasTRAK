package com.example.gastrak;

// Jacob Pauls
// 300273666
// FirebaseDBConnection.java

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseDBConnection {

    // hold name
    private static final String TAG = "FirebaseDBConnection";

    private FirebaseFirestore gasTrakFirebase;

    public FirebaseDBConnection() {
        gasTrakFirebase = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        gasTrakFirebase.setFirestoreSettings(settings);
    }

    public CollectionReference getCollectionReference(String collectionName) {
        return gasTrakFirebase.collection(collectionName);
    }

}
