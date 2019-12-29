package com.partypopper.app.database.repository;

import com.partypopper.app.database.model.Event;

import java.util.List;
import java.util.Map;

public interface eventsAndOrganizerNamesCallback {
    void onCallback(List<Event> events, Map<Event, String> eventsAndOrganizerNames);
}
