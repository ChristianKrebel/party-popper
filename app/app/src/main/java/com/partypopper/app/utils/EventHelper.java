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

    public static List<Event> sortByAttendees(List<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getGoing() > o2.getGoing() ? -1 :(o1.getGoing() < o2.getGoing() ? 1 : 0);
            }
        });
        return events;
    }

    public static List<Event> sortByStartdate(List<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        });
        return events;
    }



    /* If to sort with chaining */

    public class GroupBySorter implements Comparator<Event> {

        private List<Comparator<Event>> listComparators;

        public GroupBySorter(Comparator<Event>... comparators) {
            this.listComparators = Arrays.asList(comparators);
        }

        public int compare(Event empOne, Event empTwo) {
            for (Comparator<Event> comparator : listComparators) {
                int result = comparator.compare(empOne, empTwo);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }

    class SortByDistance implements Comparator<Event>
    {
        private double latitude, longitude;

        SortByDistance(final double latitude, final double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        @Override
        public int compare(Event o1, Event o2) {
            return Double.compare(getDistanceToEvent(latitude, longitude, o1), getDistanceToEvent(latitude, longitude, o2));
        }
    }

    class SortByAttendees implements Comparator<Event>
    {
        @Override
        public int compare(Event a, Event b)
        {
            return a.getGoing() > b.getGoing() ? -1 :(a.getGoing() < b.getGoing() ? 1 : 0);
        }
    }

    class SortByStartdate implements Comparator<Event>
    {
        @Override
        public int compare(Event a, Event b)
        {
            return a.getStartDate().compareTo(b.getStartDate());
        }
    }

    public List<Event> sortByAll(final double latitude, final double longitude, List<Event> events) {
        Collections.sort(events, new GroupBySorter(new SortByStartdate(), new SortByAttendees()));
        return events;
    }
}
