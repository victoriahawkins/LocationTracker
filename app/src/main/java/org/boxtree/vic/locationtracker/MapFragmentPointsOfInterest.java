package org.boxtree.vic.locationtracker;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.boxtree.vic.locationtracker.vo.NearbyPlace;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
//import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by victoriahawkins on 6/8/17.
 * <p>
 *     debugged with
 * http://www.openstreetmap.org/user/aintgd/traces/2573220
 */

public class MapFragmentPointsOfInterest extends MapFragment implements GoogleMap.OnMarkerClickListener {


    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private List<NearbyPlace> mNearbyPlaces = new ArrayList<>();

    public MapFragmentPointsOfInterest() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        Log.d("MapFragmentPOI", "Loading map for points of interest");


        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getActivity(), "Please grant the LocationTracker app access to fine location updates.", Toast.LENGTH_LONG).show();

            return;
        }
        mMap.setMyLocationEnabled(true);

        setupMarkerDragListener();


        // get current places and place ids to display something interesting on the map
        Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i("MapFragmentPOI", String.format("Place '%s' has likelihood: %g and ID: %s",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood(), placeLikelihood.getPlace().getId()));


                    NearbyPlace p = new NearbyPlace(placeLikelihood.getPlace().getId(), placeLikelihood.getPlace().getLatLng(), placeLikelihood.getPlace().getName().toString());
                    mNearbyPlaces.add(p);
                }


                likelyPlaces.release();


                positionCameraForLastLocation();

                displayPhotos();


            }
        });

    }

    private void setupMarkerDragListener() {
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
    }

    private void positionCameraForLastLocation() {
        LatLng mLastKnownCoordinate = mNearbyPlaces.get(0).getLatitudeLongitude();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownCoordinate, 17));
    }

    private void addMarkerForPhoto(NearbyPlace pPlace, Bitmap image, CharSequence photoAttribution) {

        mMap.addMarker(
                new MarkerOptions()
                        .position(pPlace.getLatitudeLongitude())
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .draggable(true)
//                       .title(photoAttribution.toString()
                        .snippet(photoAttribution.toString())
                        .title(pPlace.getName()
                        ));

        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {


        marker.showInfoWindow();
        return false;
    }

    private void displayPhotos() {

        for (NearbyPlace place : mNearbyPlaces) {

            getPhotos(place);
        }

        getPhotos(mNearbyPlaces.get(0));
    }

    // Request photos and metadata for the specified place.
    private void getPhotos(final NearbyPlace pPlace) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(pPlace.getPlaceId());
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {

                // Get the place photo buffers (photos).
                PlacePhotoMetadataResponse photos = task.getResult();


                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                PlacePhotoMetadata frozen;
                if (photoMetadataBuffer.getCount() > 0) {

                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0); // get first one for this photo
                    frozen = photoMetadata.freeze();
                    photoMetadataBuffer.release();
                } else {
                    Log.i(TAG, String.format("getPhotos none returned for %s...", pPlace));
                    photoMetadataBuffer.release();
                    return;
                }


                // Get the attribution text.
                final CharSequence attribution = frozen.getAttributions();


                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(frozen);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        addMarkerForPhoto(pPlace, bitmap, attribution);

                    }
                });
            }
        });
    }


}
