package org.boxtree.vic.locationtracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


/*
Displays a map, requests permission for location updates, and records locations visited

for debugging, GPS traces can be downloaded here: http://www.openstreetmap.org/traces

 */
public class MapFragmentRecorder extends MapFragment  {

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private static final int MY_PERMISSIONS_REQUEST_FINE_GPS = 1;
    private LatLng mLastPosition;

    private MapFragInteraction mCallback;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check permission to receive location updates was granted to this app by user previously
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("MapFragmentRecorder", "no fine access location permission, requesting now and terminating flow");

            // calls back to onRequestPermissionsResult
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_GPS);

            return;
        }

        // start receiving updates
        initializeLocationUpdates();

//        Snackbar.make(findViewById(R.id.flContent), "Tracking new location updates...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        Snackbar.make(view, "Tracking new location updates...", Snackbar.LENGTH_LONG).setAction("Action", null).show();



    }

    // receive updates for GPS location
    private void initializeLocationUpdates() {
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        // GPS_PROVIDER rather than NETWORK_PROVIDER uses mock location data
        try {
            Log.d("MapFragmentRecorder", "Request location updates");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), "Please grant the LocationTracker app access to fine location updates.", Toast.LENGTH_LONG).show();

        }
    }


    /* Callback for permissions result */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("MapFragmentRecorder", "Permission granted for GPS location receipt, initializing locaiton manager and updates");


                    initializeLocationUpdates();

                } else {

                    Log.d("MapFragmentRecorder", "no fine access location permission, requesting now and terminating app");

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void makeUseOfNewLocation(Location location) {


        if (location != null) {
            Log.d("MapFragmentRecorder", ".makeUseOfNewLocation location received: " + location.toString());

            LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());

            if (mLastPosition == null) mLastPosition = newPosition;


            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(mLastPosition, newPosition)
                    .width(5)
                    .color(Color.RED));


            // i like this one for collecting the trip realtime
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 17));


//            mMap.addMarker(new MarkerOptions().position(newPosition).title("Lat/Long: " + newPosition.latitude + "/" + newPosition.longitude));




            // I like this one for replay
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                    .build();                   // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mLastPosition = newPosition;

            // save the location for the route
            getRoute().add(location);


            Log.d("MapFragmentRecorder", ".makeUseOfNewLocation route saved so far are "+ getRoute().size());


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

//        super.onMapReady(googleMap);

        if (mLastPosition != null)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mLastPosition));



        // if route active, redraw

        Log.d("MapFragmentRecorder", "onMapReady drawing previous location adding polyline for route with size " + getRoute().size());


        LatLng lastPosition = null;
        for (Location loc : getRoute()) {

            Log.d("MapFragmentRecorder", "onMapReady drawing previous location adding polyline");
            LatLng nextPosition = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (lastPosition== null) lastPosition = nextPosition;
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(lastPosition, nextPosition)
                    .width(5)
                    .color(Color.RED));


            lastPosition = nextPosition;

        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallback = (MapFragInteraction) context;

    }


    // commit the trip details here
//    @Override
//    public void onPause() {
//        super.onPause();

    @Override
    public void onPause() {
        super.onPause();

    // confirm save trip

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.save_route_dialog)
                // Add the buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                        stopLocationUpdates();

                        mCallback.saveTrip(getRoute());

                        setRoute(new ArrayList<Location>()); // reset route


                    }
                })
                .setNeutralButton(R.string.keep_recording, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked keep recording button, do nothing


                        // TODO for keep recording need to set preference in main activity to preserve the location updates

                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                        stopLocationUpdates();
                        setRoute(new ArrayList<Location>()); // reset route


                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();



//        Snackbar.make(getView(), "Saving trip...", Snackbar.LENGTH_LONG).setAction("Action", null).show();



    }

    @Override
    public void onResume() {
        super.onResume();

        initializeLocationUpdates();
    }

    private void stopLocationUpdates() {
//        Locations

        // this doesn't really have any effect in the simulator
        Log.d("MapFragmentRecorder", "Stop receiving location updates");
        mLocationManager.removeUpdates(mLocationListener);
    }

    //    interface callback for main activity to save Locations visited so far
    public interface MapFragInteraction {

        public int saveTrip(ArrayList<Location> route);

    }




}
