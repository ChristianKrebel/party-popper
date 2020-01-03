package com.partypopper.app.database.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;

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

    public Task<List<Event>> getEvents(int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events").orderBy("startDate", Query.Direction.ASCENDING).limit(amount).get());
    }

    public Task<List<Event>> getEventsByOrganizerId(String organizerId, int amount) {
        return eventSimpleMapper.mapEntities(db.collection("events").whereEqualTo("organizer", organizerId).orderBy("startDate", Query.Direction.ASCENDING).limit(amount).get());
    }

    public Task<List<Event>> searchByName(String name) {
        return eventSimpleMapper.mapEntities(db.collection("events").whereGreaterThanOrEqualTo("lowercaseName", name.toLowerCase()).limit(5).get());
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

    public static EventsRepository getInstance() {
        if(instance == null) {
            instance = new EventsRepository();
        }
        return instance;
    }
}
