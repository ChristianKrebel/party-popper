package com.partypopper.app.database.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.model.Event;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsRepository {

    private static EventsRepository instance;

    private final FirebaseFirestore db;
    private final SimpleMapper<Event> eventSimpleMapper;

    private final FirebaseFunctions mFunctions;

    private EventsRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventSimpleMapper = new SimpleMapper<>(Event.class);
        this.mFunctions = FirebaseFunctions.getInstance();
    }

    public Task<Boolean> hasJoined(String eventId) {
        return db.collection("events").document(eventId).collection("joined").document(FirebaseAuth.getInstance().getUid()).get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                DocumentSnapshot snapshot = task.getResult();
                return snapshot != null && snapshot.exists();
            }
        });
    }

    public Task<List<Event>> getEvents(int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events").orderBy("startDate", Query.Direction.ASCENDING).limit(amount).get());
    }

    public Task<List<Event>> getEventsByOrganizerId(String organizerId, int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events").whereEqualTo("organizer", organizerId).orderBy("startDate", Query.Direction.ASCENDING).limit(amount).get());
    }

    public Task<Event> getEventByEventId(String id) {
        return eventSimpleMapper.mapEntity(db.collection("events").document(id).get());
    }

    public Task<List<Event>> searchByName(String name, int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events")
                .orderBy("lowercaseName")
                .startAt(name.toLowerCase())
                .endAt(name.toLowerCase() + "\uf8ff") // High point unicode character, called Escape.
                .limit(amount)
                .get());
    }

    public Task<List<Event>> searchByDate(Date date, int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .startAt(date)
                .limit(amount)
                .get());
    }

    public Task<List<Event>> searchByAttendees(int att, int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events")
                .orderBy("going")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo("going",att)
                .limit(amount)
                .get());
    }

    public Task<HttpsCallableResult> joinEvent(String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        return mFunctions.getHttpsCallable("joinEvent").call(data);
    }

    public Task<HttpsCallableResult> leaveEvent(String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        return mFunctions.getHttpsCallable("leaveEvent").call(data);
    }

    public Task<List<Event>> getNearbyEvents(double latitude, double longitude, int distance, int amount) {

        // ~1 mile of lat and lon in degrees
        double lat = 0.0144927536231884;
        double lng = 0.0181818181818182;

        double lowerLat = latitude - (lat * distance);
        double lowerLng = longitude - (lng * distance);

        double greaterLat = latitude + (lat * distance);
        double greaterLng = longitude + (lng * distance);

        final GeoPoint lesserGeopoint = new GeoPoint(lowerLat, lowerLng);
        final GeoPoint greaterGeoPoint = new GeoPoint(greaterLat, greaterLng);



        Task<List<Event>> eventTask = eventSimpleMapper.mapEntities(db.collection("events").whereGreaterThanOrEqualTo("location", lesserGeopoint).whereLessThanOrEqualTo("location", greaterGeoPoint).limit(amount).get());
        return eventTask.continueWith(new Continuation<List<Event>, List<Event>>() {
            @Override
            public List<Event> then(@NonNull Task<List<Event>> task) throws Exception {
                List<Event> events = task.getResult();
                if(events != null) {
                    for(Event e : events) {
                        if(lesserGeopoint.compareTo(e.getLocation()) > 0 || e.getLocation().compareTo(greaterGeoPoint) > 0) {
                            events.remove(e);
                        }
                    }
                }
                return events;
            }
        });
    }

    public static EventsRepository getInstance() {
        if(instance == null) {
            instance = new EventsRepository();
        }
        return instance;
    }
}
