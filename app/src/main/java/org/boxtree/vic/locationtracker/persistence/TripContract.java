package org.boxtree.vic.locationtracker.persistence;

import android.provider.BaseColumns;

/**
 * Created by victoriahawkins on 6/5/17.
 */

public class TripContract {

    // do not instantiate this class
    private TripContract() {}

    /* Inner classes define the table's contents */
    public static class TripEntry implements BaseColumns {

        public static final String TABLE_NAME = "trips";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTON = "description";
        public static final String COLUMN_NAME_DATE = "date";

    }

//    Location[gps 51.681737,-2.239192 acc=20 et=+51m55s809ms alt=0.0
//    Location[gps 51.681738,-2.239295 acc=20 et=+51m56s68ms alt=0.0
    public static class Location implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_TRIP_ID = "trip_id"; // fk
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LONG = "longitude";

    }


}
