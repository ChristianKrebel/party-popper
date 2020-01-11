package com.partypopper.app.utils;

import com.google.android.gms.maps.model.LatLng;
import com.partypopper.app.database.model.Event;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventHelper {

    public static List<Event> sortByDistance(final double latitude, final double longitude, List<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return Double.compare(getDistanceToEvent(latitude, longitude, o1), getDistanceToEvent(latitude, longitude, o2));
            }
        });
        return events;
    }

    private static double getDistanceToEvent(double latitude, double longitude, Event event) {
        return distance(latitude, longitude, event.getLocation().getLatitude(), event.getLocation().getLongitude());
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return dist;
        }
    }
}
