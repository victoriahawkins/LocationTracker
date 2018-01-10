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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


/*
Displays a map, requests permission for location updates, and records locations visited

for debugging, GPS traces can be downloaded here: http://www.openstreetmap.org/traces

 */
public class MapFragmentRecorder extends MapFragment {

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private static final int MY_PERMISSIONS_REQUEST_FINE_GPS = 1;
    private LatLng mLastPosition;

    private MapFragInteraction mCallback;


    List<Polyline> polylines = new ArrayList<>();

    Boolean recording = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View mapView = inflater.inflate(R.layout.activity_maps_recorder, container, false);

        final Button button = (Button) mapView.findViewById(R.id.recordingButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (button.getText().equals(getString(R.string.start))) {

                    startRecording(mapView);
                    button.setText(R.string.stop);

                } else {
                    stopRecording(button);
                }

            }
        });

        return mapView;
    }


    // set the fragment
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_recorder);
        mapFragment.getMapAsync(this);


    }

    private void startRecording(View view) {
        //        super.onViewCreated(view, savedInstanceState);

        // Check permission to receive location updates was granted to this app by user previously
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("MapFragmentRecorder", "no fine access location permission, requesting now and terminating flow");

            // calls back to onRequestPermissionsResult
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_GPS);

            return;
        }


        // start receiving updates
        initializeLocationUpdates();

        setRecording(true);


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
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void makeUseOfNewLocation(Location location) {


        if (location != null) {
            Log.d("MapFragmentRecorder", ".makeUseOfNewLocation location received: " + location.toString());

            LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());

            // first time, record position and return
            if (getRoute().isEmpty()) {
                setLastPosition(newPosition);
                getRoute().add(location);
                return;
            }


            // if recording, add line to map and save location
            if (isRecording()) {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(getLastPosition(), newPosition)
                        .width(5)
                        .color(Color.RED));

                polylines.add(line);

                // save the location for the route
                getRoute().add(location);

            }


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

            setLastPosition(newPosition);


            Log.d("MapFragmentRecorder", ".makeUseOfNewLocation route saved so far are " + getRoute().size());


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

//        super.onMapReady(googleMap);

        if (getLastPosition() != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(getLastPosition()));


        // if route active, redraw

        Log.d("MapFragmentRecorder", "onMapReady drawing previous location adding polyline for route with size " + getRoute().size());


        LatLng lastPosition = null;
        for (Location loc : getRoute()) {

            LatLng nextPosition = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (lastPosition == null) lastPosition = nextPosition;
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

    private void stopRecording(final Button button) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.save_route_dialog)
                // Add the buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        setRecording(false);

                        stopLocationUpdates();

                        mCallback.saveTrip(getRoute());

                        clearRoute();

                    }
                })
                .setNeutralButton(R.string.keep_recording, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked keep recording button, do nothing

                    }
                })

                .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                        setRecording(false);
                        stopLocationUpdates();
                        clearRoute();
                        button.setText(R.string.start);


                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


//        Snackbar.make(getView(), "Saving trip...", Snackbar.LENGTH_LONG).setAction("Action", null).show();


    }

    private void clearRoute() {
        getRoute().clear(); // reset route
        setLastPosition(null);
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
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

    public LatLng getLastPosition() {
        return mLastPosition;
    }

    public void setLastPosition(LatLng mLastPosition) {
        this.mLastPosition = mLastPosition;
    }

    //    interface callback for main activity to save Locations visited so far
    public interface MapFragInteraction {

        int saveTrip(ArrayList<Location> route);

    }

    public Boolean isRecording() {
        return recording;
    }

    public void setRecording(Boolean recording) {
        this.recording = recording;
    }


}
