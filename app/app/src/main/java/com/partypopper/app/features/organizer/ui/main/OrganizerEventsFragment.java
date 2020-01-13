package com.partypopper.app.features.organizer.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.partypopper.app.R;
import com.partypopper.app.database.model.BlockedOrganizer;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.events.eventsAndOrganizerNamesCallback;
import com.partypopper.app.features.events.EventsAdapter;
import com.partypopper.app.utils.EventHelper;

import static com.partypopper.app.utils.Constants.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The second Fragment of the OrganizerActivity.
 * It hosts the current events of the organizer in a RecyclerView.
 */
public class OrganizerEventsFragment extends Fragment {

    private String organizerId;
    private RecyclerView mRecyclerView;
    private EventsAdapter adapter;

    public OrganizerEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrganizerInfoFragment.
     */
    public static OrganizerEventsFragment newInstance(String organizerId) {
        OrganizerEventsFragment fragment = new OrganizerEventsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("organizerId", organizerId);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Sets all attributes from arguments.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organizerId = getArguments().getString("organizerId");
        }
    }

    /**
     * Creates the View of the fragment. Similar to onCreate of Activities.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_organizer_events, container, false);

        // RecyclerView
        mRecyclerView = v.findViewById(R.id.frEventRv);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        showData(new eventsAndOrganizerNamesCallback() {
            @Override
            public void onCallback(List<Event> events, Map<Event, String> eventsAndOrganizerNames, List<BlockedOrganizer> blockedOrganizers) {

                // Remove passed events
                events = EventHelper.getEventsWithoutPassedOnes(events);

                // Remove events of blocked organizers
                events = EventHelper.getEventsWithoutBlockedOnes(events, blockedOrganizers);

                adapter = new EventsAdapter(getActivity(),
                        events,
                        eventsAndOrganizerNames,
                        v.getContext(),
                        R.layout.row_events_organizer_events);
                mRecyclerView.setAdapter(adapter);
            }
        });

        return v;
    }

    private void showData(final eventsAndOrganizerNamesCallback dbCallback) {
        EventsRepository eventsRepository = EventsRepository.getInstance();
        final OrganizerRepository organizerRepository = OrganizerRepository.getInstance();
        // show all events, even if the organizer is blocked
        final List<BlockedOrganizer> blockedOrganizers = new ArrayList<>();

        // Get events of the specific organizer
        eventsRepository.getEventsByOrganizerId(organizerId, EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                if (task.getResult() != null) {
                    final List<Event> events = task.getResult();
                    final Map<Event, String> eventsAndOrganizerNames = new LinkedHashMap<>();

                    for (int a = 0; a < events.size(); a++) {
                        final Event event = events.get(a);
                        final int b = a;

                        // Get names of organizers
                        organizerRepository.getOrganizerById(event.getOrganizer())
                                .addOnCompleteListener(new OnCompleteListener<Organizer>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Organizer> task) {
                                        if (task.isSuccessful()) {
                                            eventsAndOrganizerNames.put(event, task.getResult().getName());
                                            if (b == (events.size()-1)) {
                                                dbCallback.onCallback(events, eventsAndOrganizerNames, blockedOrganizers);
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
