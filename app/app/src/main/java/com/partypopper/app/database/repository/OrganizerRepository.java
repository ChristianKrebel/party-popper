package com.partypopper.app.database.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.partypopper.app.database.model.Event;

import java.util.List;

public class OrganizerRepository {

    private static OrganizerRepository instance;

    private final FirebaseFirestore db;
    private final FirestoreRepository<Event> repo;
    private final SimpleMapper<Event> eventSimpleMapper;

    private OrganizerRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.repo = new FirestoreRepository<>(Event.class, "events");
        this.eventSimpleMapper = new SimpleMapper<>(Event.class);
    }

    public Task<Void> createEvent(Event event) {
        repo.create(event, false);
        return null;
    }

    public Task<Void> deleteEvent(String id) {
        return repo.delete(id);
    }

    public Task<Void> updateEvent(Event event) {
        return repo.update(event);
    }

    public Task<List<Event>> getNextEvent() {
        return eventSimpleMapper.mapEntities(db.collection("events").orderBy("startDate", Query.Direction.ASCENDING).limit(1).get());
    }

    public static OrganizerRepository getInstance() {
        if(instance == null) {
            instance = new OrganizerRepository();
        }
        return instance;
    }




    /*public Task<List<Event>> getUpcomingEvent(int limit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("events")
                .whereLessThan("startDate", System.currentTimeMillis())
                .orderBy("startDate")
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, List<Event>>() {
                    @Override
                    public List<Event> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<Event> events = new ArrayList<>();
                            for (DocumentSnapshot snap : querySnapshot.getDocuments()) {
                                Event event = snap.toObject(Event.class);
                                if (event != null) {
                                    event.setEntityKey(snap.getId());
                                    events.add(event);
                                }
                            }
                            return events;
                        } else {
                            return new ArrayList<>();
                        }
                    }
                });
    }*/
}