package com.partypopper.app.database.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.partypopper.app.database.model.Event;

import java.util.List;

public class EventsRepository {

    private static EventsRepository instance;

    private final FirebaseFirestore db;
    private final FirestoreRepository<Event> repo;
    private final SimpleMapper<Event> eventSimpleMapper;

    private EventsRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.repo = new FirestoreRepository<>(Event.class, "events");
        this.eventSimpleMapper = new SimpleMapper<>(Event.class);
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

    public static EventsRepository getInstance() {
        if(instance == null) {
            instance = new EventsRepository();
        }
        return instance;
    }
}
