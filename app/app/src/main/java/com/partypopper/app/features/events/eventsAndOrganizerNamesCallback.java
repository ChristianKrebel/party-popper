package com.partypopper.app.features.events;

import com.partypopper.app.database.model.BlockedOrganizer;
import com.partypopper.app.database.model.Event;

import java.util.List;
import java.util.Map;

/**
 * A Callback to use when data is retrieved asyncly.
 */
public interface eventsAndOrganizerNamesCallback {
    void onCallback(List<Event> events,
                    Map<Event, String> eventsAndOrganizerNames,
                    List<BlockedOrganizer> blockedOrganizers);
}
