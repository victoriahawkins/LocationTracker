package org.boxtree.vic.locationtracker.vo;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by victoriahawkins on 6/2/17.
 */

public class Trip {


    private long itemId; // sqllite row id

    private String name;

    private String description;

    private Date date;

    private ArrayList<Location> route;

    // TODO photos



    // used when capturing new route before saved
    public Trip (ArrayList<Location> route) {
        this.route = route;
    }

    // used for holding trip retrieved from db
    public Trip(long itemId, String name, String description, Date date) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.date = date;
    }


    // getter and setter

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<Location> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Location> route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "itemId=" + itemId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", route=" + route +
                '}';
    }
}
