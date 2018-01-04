package org.boxtree.vic.locationtracker;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by victoriahawkins on 6/8/17.
 * <p>
 * This part of the app, using Google Places API doesn't work through the proxy.
 */

public class MapFragmentPointsOfInterest extends MapFragment
//        implements GoogleApiClient.ConnectionCallbacks,
        implements GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    LatLng lastKnownCoordinate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)

//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
                .enableAutoManage(getActivity(), this)

                .build();

        mGoogleApiClient.connect();

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override

    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

//        PlaceDetectionAPI


        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getActivity(), "Please grant the LocationTracker app access to fine location updates.", Toast.LENGTH_LONG).show();

            return;
        }
        mMap.setMyLocationEnabled(true);


        // not too handy in the simulator
//        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
//        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//            @Override
//            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                    Log.i("MapFragmentPOI", String.format("Place '%s' has likelihood: %g",
//                            placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getLikelihood()));
//                }
//                likelyPlaces.release();
//            }
//        });


        // trying to get place for last known location
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastKnownLocation != null) {
            lastKnownCoordinate = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownCoordinate));
        } else {
            // emulator
            LatLng sydney = new LatLng(-34, 151); // default
            lastKnownCoordinate = sydney;
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownCoordinate));
            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(lastKnownCoordinate.latitude, lastKnownCoordinate.longitude))
                                    .zoom(10)
                                    .build()));


        }

//        google place id search useful for testing in emulator
//        https://developers.google.com/places/place-id
        String sydneyPlaceId = "ChIJP5iLHkCuEmsRwMwyFmh9AQU";


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                Log.d("MapFragmentPOI", "onMarkerDragStart..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                Log.d("MapFragmentPOI", "onMarkerDragEnd..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                Log.i("MapFragmentPOI", "onMarkerDrag...");
            }
        });


        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, sydneyPlaceId).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
            @Override
            public void onResult(PlacePhotoMetadataResult placePhotoMetadataResult) {
                if (placePhotoMetadataResult.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                    int photoCount = photoMetadata.getCount();
                    for (int i = 0; i < photoCount; i++) {
                        final PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(i);
                        final String photoDetail = placePhotoMetadata.toString();
                        final CharSequence photoAttribution = placePhotoMetadata.getAttributions();
                        placePhotoMetadata.getScaledPhoto(mGoogleApiClient, 500, 500).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                            @Override
                            public void onResult(PlacePhotoResult placePhotoResult) {
                                if (placePhotoResult.getStatus().isSuccess()) {
                                    Log.i("MapFragmentPOI", "Photo " + photoDetail + " loaded");


                                    addMarkerForPhoto(lastKnownCoordinate, placePhotoResult, photoAttribution);
                                } else {
                                    Log.e("MapFragmentPOI", "Photo " + photoDetail + " failed to load");
                                }
                            }
                        });
                    }
                    photoMetadata.release();
                } else {
                    Log.e("MapFragmentPOI", "No photos returned");
                }
            }
        });

    }

    private void addMarkerForPhoto(LatLng lastKnownCoordinate, PlacePhotoResult placePhotoResult, CharSequence photoAttribution) {

        mMap.addMarker(
                new MarkerOptions()
                        .position(lastKnownCoordinate)
                        .icon(BitmapDescriptorFactory.fromBitmap(placePhotoResult.getBitmap()))
                        .draggable(true)
                        .title(photoAttribution.toString()));
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MapFragmentPOI", "Google Places API connection failed with error code: " + connectionResult.getErrorCode() + ", msg: " + connectionResult.getErrorMessage());
    }

}
