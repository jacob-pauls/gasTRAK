package com.example.gastrak;

// Jacob Pauls
// 300273666
// MySQLNodeControllerConnectionController.java

// class to control the BroadcastReceiver for MySQL requests

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

public class MySQLNodeConnectionController {

    private BroadcastReceiver myReceiver = null;

    public MySQLNodeConnectionController(Context context) {
        myReceiver = new MySQLNodeConnection(context);
        context.registerReceiver(myReceiver, new IntentFilter(MySQLNodeConnection.STATUS_DONE));
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(myReceiver);
    }

}
