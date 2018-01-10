package org.boxtree.vic.locationtracker.vo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by vic on 1/10/18.
 * Store 10 locations returned from places API
 */

public class NearbyPlace {

    private String placeId;
    private LatLng latitudeLongitude;
    private String name;

    public NearbyPlace(String pId, LatLng pLatLng, String pName) {
        placeId = pId;
        latitudeLongitude = pLatLng;
        name = pName;
    }

    public LatLng getLatitudeLongitude() {
        return latitudeLongitude;
    }

    public void setLatitudeLongitude(LatLng latitudeLongitude) {
        this.latitudeLongitude = latitudeLongitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NearbyPlace{" +
                "placeId='" + placeId + '\'' +
                ", latitudeLongitude=" + latitudeLongitude +
                ", name='" + name + '\'' +
                '}';
    }
}
