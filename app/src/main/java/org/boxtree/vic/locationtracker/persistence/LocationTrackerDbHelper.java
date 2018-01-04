package org.boxtree.vic.locationtracker.persistence;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import org.boxtree.vic.locationtracker.vo.Trip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by victoriahawkins on 6/5/17.
 */

public class LocationTrackerDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_TRIPS =
            "CREATE TABLE " + TripContract.TripEntry.TABLE_NAME + " (" +
                    TripContract.TripEntry._ID + " INTEGER PRIMARY KEY, " +
                    TripContract.TripEntry.COLUMN_NAME_NAME + " TEXT," +
                    TripContract.TripEntry.COLUMN_NAME_DESCRIPTON + " TEXT," +
                    TripContract.TripEntry.COLUMN_NAME_DATE + " INTEGER)";

    private static final String SQL_CREATE_LOCATIONS =
            "CREATE TABLE " + TripContract.Location.TABLE_NAME + " (" +
                    TripContract.Location._ID + " INTEGER PRIMARY KEY, " +
                    TripContract.Location.COLUMN_NAME_TRIP_ID + " INTEGER," +
                    TripContract.Location.COLUMN_NAME_LAT + " DOUBLE," +
                    TripContract.Location.COLUMN_NAME_LONG + " DOUBLE)";


    private static final String SQL_DELETE_TRIPS =
            "DROP TABLE IF EXISTS " + TripContract.TripEntry.TABLE_NAME;

    private static final String SQL_DELETE_LOCATIONS =
            "DROP TABLE IF EXISTS " + TripContract.Location.TABLE_NAME;



    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LocationTracker.db";


    public LocationTrackerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIPS);
        db.execSQL(SQL_CREATE_LOCATIONS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_LOCATIONS);
        db.execSQL(SQL_DELETE_TRIPS);
        onCreate(db);

    }

//    public void insertNewTrip(LocationTrackerDbHelper dbh, Trip trip) {
    public void insertNewTrip(Trip trip) {

//        SQLiteDatabase db = dbh.getWritableDatabase();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues tripValues = new ContentValues();
        tripValues.put(TripContract.TripEntry.COLUMN_NAME_NAME, trip.getName());
        tripValues.put(TripContract.TripEntry.COLUMN_NAME_DESCRIPTON, trip.getDescription());
        tripValues.put(TripContract.TripEntry.COLUMN_NAME_DATE, trip.getDate().getTime());

        long newTripRowId = db.insert(TripContract.TripEntry.TABLE_NAME, null, tripValues);

        Log.d("LocationTrackerDbHelper", "Inserted new Trip " + newTripRowId);


        for (Location gpsCoordinate : trip.getRoute()) {

            ContentValues locationValue = new ContentValues();
            locationValue.put(TripContract.Location.COLUMN_NAME_TRIP_ID, newTripRowId);
            locationValue.put(TripContract.Location.COLUMN_NAME_LAT, gpsCoordinate.getLatitude());

            long newLocationRowId = db.insert(TripContract.Location.TABLE_NAME, null, locationValue);

            Log.d("LocationTrackerDbHelper", "Inserted new Location" + newLocationRowId);

        }

        db.close();
    }

    public void deleteTrip(Trip trip) {

        Log.d("LocationTrackerDbHelper", "Removing Trip:" + trip);


        SQLiteDatabase db = this.getWritableDatabase();

        // Define 'where' part of query.
        String selection = TripContract.Location.COLUMN_NAME_TRIP_ID + " = ?";
    // Specify arguments in placeholder order.
        String[] selectionArgs = { Long.toString(trip.getItemId()) };
        int location_rows_affected = db.delete(TripContract.Location.TABLE_NAME, selection, selectionArgs);


        Log.d("LocationTrackerDbHelper", "Deleted rows from Locations table: " + location_rows_affected);



        int trip_rows_affected = db.delete(TripContract.TripEntry.TABLE_NAME, TripContract.TripEntry._ID + " = ?", new String[]{ Long.toString(trip.getItemId())});

        Log.d("LocationTrackerDbHelper", "Deleted rows from Trip table: " + trip_rows_affected);



        db.close();


    }

    public List<Trip> getAllTrips() {

        List<Trip> trips = new ArrayList<>();



        SQLiteDatabase db = this.getReadableDatabase();

        String[] tripProjection = {
                TripContract.TripEntry._ID,
                TripContract.TripEntry.COLUMN_NAME_NAME,
                TripContract.TripEntry.COLUMN_NAME_DESCRIPTON,
                TripContract.TripEntry.COLUMN_NAME_DATE
        };


        String sortOrder = TripContract.TripEntry._ID + " DESC";

        Cursor tripCursor = db.query(
                TripContract.TripEntry.TABLE_NAME,          // table to query
                tripProjection,                             // columns to select
                null,                                       // where clause bind null, select all rows
                null,                                       // where clause arg null
                null,                                       // don't group rows
                null,                                       // don't filter by row groups
                sortOrder                                   // sort by row id created order, last first
        );

        while (tripCursor.moveToNext()) {
            long itemId = tripCursor.getLong(tripCursor.getColumnIndexOrThrow(TripContract.TripEntry._ID));
            Date createDate = new Date(tripCursor.getLong(tripCursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_DATE)));

            String name = tripCursor.getString(tripCursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_NAME));
            String desc = tripCursor.getString(tripCursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_DESCRIPTON));

            Trip trip = new Trip(itemId, name, desc, createDate);

            Log.d("TripItemFragment", "loading trip info for " + trip);

            trips.add(trip);
        }

        tripCursor.close();

        db.close();

        return trips;
    }



}
