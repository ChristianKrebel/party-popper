package com.partypopper.app.features.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.features.events.EventsAdapter;
import com.partypopper.app.features.organizer.BusinessActivity;
import com.partypopper.app.features.organizer.PublishEventActivity;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.database.repository.*;

import static com.partypopper.app.utils.Constants.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private EventsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.edToolbar);
        setSupportActionBar(toolbar);



        // FAB
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PublishEventActivity.class));
            }
        });

        if (true) {     // TODO just for testing, exchange with isOrganizer() later!
            fab.show();
        }

        // RecyclerView
        mRecyclerView = findViewById(R.id.eventRv);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        showData(new eventsAndOrganizerNamesCallback() {
            @Override
            public void onCallback(List<Event> events, Map<Event, String> eventsAndOrganizerNames) {
                adapter = new EventsAdapter(DashboardActivity.this,
                        events,
                        eventsAndOrganizerNames,
                        getApplicationContext(),
                        R.layout.row_events_dashboard);
                mRecyclerView.setAdapter(adapter);
            }
        });
    }

    private void showData(final eventsAndOrganizerNamesCallback dbCallback) {
        EventsRepository eventsRepository = EventsRepository.getInstance();
        final OrganizerRepository organizerRepository = OrganizerRepository.getInstance();

        eventsRepository.getEvents(EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                if(task.getResult() != null) {
                    final List<Event> events = task.getResult();
                    final Map<Event, String> eventsAndOrganizerNames = new LinkedHashMap<>();

                    for (int a = 0; a < events.size(); a++) {
                        final Event event = events.get(a);
                        final int b = a;

                        organizerRepository.getOrganizerById(event.getOrganizer()).addOnCompleteListener(new OnCompleteListener<Organizer>() {
                            @Override
                            public void onComplete(@NonNull Task<Organizer> task) {
                                if (task.isSuccessful()) {
                                    eventsAndOrganizerNames.put(event, task.getResult().getName());
                                    if (b == (events.size()-1)) {
                                        dbCallback.onCallback(events, eventsAndOrganizerNames);
                                    }
                                }
                            }
                        });
                    }



                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*db.collection("events")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot documentSnapshot: task.getResult()) {
                            Event event = new Event();

                            event.setDescription(documentSnapshot.getString("description"));
                            event.setEndDate(((Timestamp) documentSnapshot.getData().get("endDate")).toDate());
                            event.setGoing(documentSnapshot.getLong("going").intValue());
                            event.setName(documentSnapshot.getString("name"));
                            event.setOrganizer(documentSnapshot.getString("organizer"));
                            event.setStartDate(((Timestamp) documentSnapshot.getData().get("startDate")).toDate());
                            event.setImage(documentSnapshot.getString("image"));

                            modelList.add(event);
                        }

                        //adapter
                        adapter = new EventsAdapter(DashboardActivity.this, modelList, getApplicationContext());
                        // set adapter to recyclerview
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu
     * @return if succeeded
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem((R.id.action_search));
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle item selection of the menu
     *
     * @param item
     * @return if succeeded or with item on default
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            // TODO more cases
            case R.id.action_open_business_activity:
                startActivity(new Intent(this, BusinessActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Query a String to the Firebase database and get results in the recyclerview
     *
     * @param searchText
     */
    private void firebaseSearch(String searchText) {
        // Different query than the one in the onStart method
        // TODO shorten redundant code?
        // TODO exchange with Elastic Search for 'contains'-ability and no case sensitivity
        /*Query firebaseSearchQuery = mDatabaseReference.orderByChild("title")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff"); // High point unicode character, called Escape
        FirebaseRecyclerOptions<DashboardModel> options =
                new FirebaseRecyclerOptions.Builder<DashboardModel>()
                        .setQuery(firebaseSearchQuery, DashboardModel.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<DashboardModel, EventsViewHolder>(options) {
            @Override
            public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.row_events_dashboard for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_events_dashboard, parent, false);

                return new EventsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(EventsViewHolder holder, int position, DashboardModel model) {
                holder.setDetails(model.getTitle(), model.getDate(), model.getImage(),
                        model.getOrganizer(), model.getVisitor_count());
            }
        };
        adapter.startListening();
        mRecyclerView.setAdapter(adapter);*/
    }
}
