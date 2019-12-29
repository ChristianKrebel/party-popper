package com.partypopper.app.database.repository;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class OrganizerRepository {

    private static OrganizerRepository instance;

    private final FirebaseFirestore db;
    private final FirestoreRepository<Event> repo;
    private final SimpleMapper<Event> eventSimpleMapper;
    private final FirestoreRepository<Organizer> repoOrganizer;

    private OrganizerRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.repo = new FirestoreRepository<>(Event.class, "events");
        this.eventSimpleMapper = new SimpleMapper<>(Event.class);
        this.repoOrganizer = new FirestoreRepository<>(Organizer.class, "organizer");
    }

    public Task<Void> createEvent(Event event) {
        return repo.create(event, false);
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

    public Task<Organizer> getOrganizerById(String id) {
        return repoOrganizer.get(id);
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