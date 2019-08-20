package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: GasDB.java

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class GasDB extends SQLiteOpenHelper {

    // hold name
    private static final String TAG = "GasDB";

    // constant global variables
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "GasDB.db";

    // SQL query to create table to hold gas prices
    private static final String SQL_CREATE_TABLE_GASPRICES =
            "CREATE TABLE " + PricesEntry.TABLE_NAME + " ( "
                    + PricesEntry.COLUMN_NAME_GASID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + PricesEntry.COLUMN_NAME_STATIONNAME + " varchar(50) NOT NULL,"
                    + PricesEntry.COLUMN_NAME_STATIONADDRESS + " varchar(50),"
                    + PricesEntry.COLUMN_NAME_PRICE + " numeric(4,2) NOT NULL,"
                    + PricesEntry.COLUMN_NAME_TIME + " time)";

    // SQL query to create table to hold trip data
    private static final String SQL_CREATE_TABLE_TRIPS =
            "CREATE TABLE " + TripsEntry.TABLE_NAME + " ( "
                    + TripsEntry.COLUMN_NAME_TRIPID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TripsEntry.COLUMN_NAME_STATIONNAME + " varchar(25),"
                    + TripsEntry.COLUMN_NAME_STATIONADDRESS + " varchar(50),"
                    + TripsEntry.COLUMN_NAME_PRICE + " numeric(4,2),"
                    + TripsEntry.COLUMN_NAME_TIME + " time,"
                    + TripsEntry.COLUMN_NAME_AMOUNTFILLED + " numeric(4,2),"
                    + TripsEntry.COLUMN_NAME_TOTALCOST + " numeric(4,2))";

    // SQL query to create table to hold trip data
    private static final String SQL_CREATE_TABLE_VEHICLES =
            "CREATE TABLE " + VehiclesEntry.TABLE_NAME + " ( "
                    + VehiclesEntry.COLUMN_NAME_VEHICLEID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + VehiclesEntry.COLUMN_NAME_VEHICLENAME + " varchar(50) NOT NULL, "
                    + VehiclesEntry.COLUMN_NAME_VEHICLEMAKE + " varchar(25) NOT NULL, "
                    + VehiclesEntry.COLUMN_NAME_VEHICLEMODEL + " varchar(25) NOT NULL, "
                    + VehiclesEntry.COLUMN_NAME_VEHICLEYEAR + " int NOT NULL, "
                    + VehiclesEntry.COLUMN_NAME_FUELEFFICIENCY + " int NOT NULL, "
                    + VehiclesEntry.COLUMN_NAME_MAXTANK + " int NOT NULL)";

    // SQL query to drop the table
    private static final String SQL_DELETE_TABLE_GASPRICES =
            "DROP TABLE IF EXISTS " + PricesEntry.TABLE_NAME;

    // SQL query to drop the table
    private static final String SQL_DELETE_TABLE_TRIPS =
            "DROP TABLE IF EXISTS " + TripsEntry.TABLE_NAME;

    // SQL query to drop the table
    private static final String SQL_DELETE_TABLE_VEHICLES =
            "DROP TABLE IF EXISTS " + VehiclesEntry.TABLE_NAME;

    // constructor
    public GasDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "GasDB: instantiated");
    }

    // get all rows
    public Cursor getAllRows(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor curr = db.rawQuery("SELECT * FROM " + table, null);
        return curr;
    }

    // overridden SQLiteOpenHelper methods
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_GASPRICES);
        Log.d(TAG, "onCreate: Created '" + PricesEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_TRIPS);
        Log.d(TAG, "onCreate: Created '" + TripsEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_VEHICLES);
        Log.d(TAG, "onCreate: Created '" + VehiclesEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_GASPRICES);
        Log.d(TAG, "onUpgrade: Dropped '" + PricesEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_TRIPS);
        Log.d(TAG, "onUpgrade: Dropped '" + TripsEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE_VEHICLES);
        Log.d(TAG, "onUpgrade: Dropped '" + VehiclesEntry.TABLE_NAME + "' table in: " + DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade: Downgraded tables in: " + DATABASE_NAME);
        onUpgrade(db, oldVersion, newVersion);
    }
}

// constant column and table names for Prices Entry
class PricesEntry implements BaseColumns {
    public static final String TABLE_NAME = "Prices";
    public static final String COLUMN_NAME_GASID = "GasID";
    public static final String COLUMN_NAME_STATIONNAME = "StationName";
    public static final String COLUMN_NAME_STATIONADDRESS = "StationAddress";
    public static final String COLUMN_NAME_PRICE = "Price";
    public static final String COLUMN_NAME_TIME = "Time";
}

// constant column and table names for Trips Entry
class TripsEntry implements BaseColumns {
    public static final String TABLE_NAME = "Trips";
    public static final String COLUMN_NAME_TRIPID = "TripID";
    public static final String COLUMN_NAME_STATIONNAME = "StationName";
    public static final String COLUMN_NAME_STATIONADDRESS = "StationAddress";
    public static final String COLUMN_NAME_PRICE = "Price";
    public static final String COLUMN_NAME_TIME = "Time";
    public static final String COLUMN_NAME_AMOUNTFILLED = "AmountFilled";
    public static final String COLUMN_NAME_TOTALCOST = "TotalCost";
}

// constant column and table names for VehiclesEntry
class VehiclesEntry implements BaseColumns {
    public static final String TABLE_NAME = "Vehicles";
    public static final String COLUMN_NAME_VEHICLEID = "VehicleID";
    public static final String COLUMN_NAME_VEHICLENAME = "VehicleName";
    public static final String COLUMN_NAME_VEHICLEMAKE = "VehicleMake";
    public static final String COLUMN_NAME_VEHICLEMODEL = "VehicleModel";
    public static final String COLUMN_NAME_VEHICLEYEAR = "VehicleYear";
    public static final String COLUMN_NAME_FUELEFFICIENCY = "FuelEfficiency";
    public static final String COLUMN_NAME_MAXTANK = "MaxTankSize";
}
