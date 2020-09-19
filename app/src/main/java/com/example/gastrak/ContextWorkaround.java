package com.example.gastrak;

import android.app.Application;
import android.content.Context;

// Jacob Pauls
// 300273666
// ContextWorkaround.java

/*
    NOTE: Tried to limit the usage of this as much as possible in the event this is volatile.
          Predominant cases were where I was required to perform operations to MySQL from static methods.
          Essentially just retrieves the application context through on create, assigns it, and returns that Context.
*/

public class ContextWorkaround extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        ContextWorkaround.context = getApplicationContext();
    }

    public static Context getContext() {
        return ContextWorkaround.context;
    }

}
