package com.partypopper.app.features.dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.partypopper.app.R;
import com.partypopper.app.database.model.BlockedOrganizer;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.features.events.eventsAndOrganizerNamesCallback;
import com.partypopper.app.features.splash.SplashActivity;
import com.partypopper.app.service.LocationService;
import com.partypopper.app.features.events.EventsAdapter;
import com.partypopper.app.features.business.BusinessActivity;
import com.partypopper.app.features.publishEvent.PublishEventActivity;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.database.repository.*;
import com.partypopper.app.utils.EventHelper;

import static com.partypopper.app.utils.Constants.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Main Activity (SplashActivity will lead you here).
 * It is a dashboard of nearby events and has functions like
 * search, add event, become organizer etc.
 */
public class DashboardActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private RecyclerView mRecyclerView;

    private EventsAdapter adapter;

    private HorizontalScrollView mSearchHsv;
    private ChipGroup mSearchCg;

    private LatLng currentLocation;

    private SwipeRefreshLayout swipeRefreshLayout;

    private enum Sort {
        startdate,
        attendees,
        distance
    }

    private Sort sortMethod = Sort.startdate;


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

        if (isOrganizer()) {
            fab.show();
        }


        // Chips
        mSearchHsv = findViewById(R.id.edSearchHsv);
        mSearchCg = findViewById(R.id.edSearchCg);
        // act on chip selection
        mSearchCg.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                searchData(i, "", new eventsAndOrganizerNamesCallback() {
                    @Override
                    public void onCallback(List<Event> events, Map<Event, String> eventsAndOrganizerNames, List<BlockedOrganizer> blockedOrganizers) {
                        adapter = new EventsAdapter(DashboardActivity.this,
                                events,
                                eventsAndOrganizerNames,
                                getApplicationContext(),
                                R.layout.row_events_dashboard);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
            }
        });


        // Pull down to refresh
        swipeRefreshLayout = findViewById(R.id.edSwipeRefreshL);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initWithPermission();
                adapter.notifyDataSetChanged();
            }
        });


        // RecyclerView
        mRecyclerView = findViewById(R.id.eventRv);
        mRecyclerView.setHasFixedSize(true);

        initWithPermission();
    }

    private void initWithPermission() {
        // If Version with flexible permissions ask for them
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // If permission not granted, request them
                ActivityCompat.requestPermissions
                        ( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                                LocationService.MY_PERMISSION_ACCESS_COARSE_LOCATION );
                return;
            } else {    // Permission already granted
                setCurrentLocation();
            }
        } else {    // if not flexible permissions, all needed permissions are granted
            setCurrentLocation();
        }
    }

    private void setCurrentLocation() {
        // Use callback to initialize the recyclerView AFTER receiving the current location
        LocationService.requestSingleUpdate(this,
                new LocationService.LocationCallback() {
                    @Override public void onNewLocationAvailable(LocationService.GPSCoordinates location) {
                        currentLocation = new LatLng(location.latitude, location.longitude);
                        initRecyclerView();
                    }
                });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        showData(new eventsAndOrganizerNamesCallback() {
            @Override
            public void onCallback(List<Event> events,
                                   Map<Event, String> eventsAndOrganizerNames,
                                   List<BlockedOrganizer> blockedOrganizers) {

                // Remove passed events
                events = EventHelper.getEventsWithoutPassedOnes(events);

                // Remove events of blocked organizers
                events = EventHelper.getEventsWithoutBlockedOnes(events, blockedOrganizers);

                // Sort
                sortEvents(events);

                adapter = new EventsAdapter(DashboardActivity.this,
                        events,
                        eventsAndOrganizerNames,
                        getApplicationContext(),
                        R.layout.row_events_dashboard);
                mRecyclerView.setAdapter(adapter);

                // Stop showing if it is refreshing
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showData(final eventsAndOrganizerNamesCallback dbCallback) {
        EventsRepository eventsRepository = EventsRepository.getInstance();
        final OrganizerRepository organizerRepository = OrganizerRepository.getInstance();

        // Get nearby Events
        eventsRepository.getNearbyEvents
                (currentLocation.latitude,
                currentLocation.longitude,
                STANDARD_DISTANCE,
                EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                if(task.getResult() != null) {
                    final List<Event> events = task.getResult();
                    final Map<Event, String> eventsAndOrganizerNames = new HashMap<>();

                    for (int a = 0; a < events.size(); a++) {
                        final Event event = events.get(a);
                        final int b = a;

                        // Get blocked organizers
                        BlockedRepository.getInstance().getBlockedOrganizers(EVENTS_AMOUNT)
                                .addOnSuccessListener(new OnSuccessListener<List<BlockedOrganizer>>() {
                            @Override
                            public void onSuccess(final List<BlockedOrganizer> blockedOrganizers) {

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
                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    private void searchData(int type, String query, final eventsAndOrganizerNamesCallback dbCallback) {
        EventsRepository eventsRepository = EventsRepository.getInstance();
        final OrganizerRepository organizerRepository = OrganizerRepository.getInstance();

        switch (type) {
            case R.id.edEventsC:
                eventsRepository.searchByName(query, EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Event>> task) {
                        if (task.getResult() != null) {
                            final List<Event> events = task.getResult();
                            if (events.size() == 0) {
                                mRecyclerView.setVisibility(View.GONE);
                            } else {
                                mRecyclerView.setVisibility(View.VISIBLE);
                            }
                            final Map<Event, String> eventsAndOrganizerNames = new LinkedHashMap<>();

                            for (int a = 0; a < events.size(); a++) {
                                final Event event = events.get(a);
                                final int b = a;

                                // Get blocked organizers
                                BlockedRepository.getInstance().getBlockedOrganizers(EVENTS_AMOUNT)
                                        .addOnSuccessListener(new OnSuccessListener<List<BlockedOrganizer>>() {
                                            @Override
                                            public void onSuccess(final List<BlockedOrganizer> blockedOrganizers) {

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
                                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.edDateC:
                Date startDate = Calendar.getInstance().getTime();
                try {
                    startDate = DateFormat.getDateInstance(DateFormat.SHORT).parse(query);
                } catch (Exception e) {
                    Log.e(e.toString(), e.getMessage());
                }
                eventsRepository.searchByDate(startDate, EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Event>> task) {
                        if (task.getResult() != null) {
                            final List<Event> events = task.getResult();
                            if (events.size() == 0) {
                                mRecyclerView.setVisibility(View.GONE);
                            } else {
                                mRecyclerView.setVisibility(View.VISIBLE);
                            }
                            final Map<Event, String> eventsAndOrganizerNames = new LinkedHashMap<>();

                            for (int a = 0; a < events.size(); a++) {
                                final Event event = events.get(a);
                                final int b = a;

                                // Get blocked organizers
                                BlockedRepository.getInstance().getBlockedOrganizers(EVENTS_AMOUNT)
                                        .addOnSuccessListener(new OnSuccessListener<List<BlockedOrganizer>>() {
                                            @Override
                                            public void onSuccess(final List<BlockedOrganizer> blockedOrganizers) {

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
                                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.edAttendeesC:
                int att = 0;
                try {
                    att = Integer.parseInt(query);
                } catch (Exception e) {
                    Log.e(e.toString(), e.getMessage());
                }
                eventsRepository.searchByAttendees(att, EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Event>> task) {
                        if (task.getResult() != null) {
                            final List<Event> events = task.getResult();
                            if (events.size() == 0) {
                                mRecyclerView.setVisibility(View.GONE);
                            } else {
                                mRecyclerView.setVisibility(View.VISIBLE);
                            }
                            final Map<Event, String> eventsAndOrganizerNames = new LinkedHashMap<>();

                            for (int a = 0; a < events.size(); a++) {
                                final Event event = events.get(a);
                                final int b = a;

                                // Get blocked organizers
                                BlockedRepository.getInstance().getBlockedOrganizers(EVENTS_AMOUNT)
                                        .addOnSuccessListener(new OnSuccessListener<List<BlockedOrganizer>>() {
                                            @Override
                                            public void onSuccess(final List<BlockedOrganizer> blockedOrganizers) {

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
                                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                break;
        }

    }

    private void sortEvents(List<Event> events) {
        switch (sortMethod) {
            case startdate:
                EventHelper.sortByStartdate(events);
                break;
            case attendees:
                EventHelper.sortByAttendees(events);
                break;
            case distance:
                EventHelper.sortByDistance
                        (currentLocation.latitude, currentLocation.longitude, events);
                break;
        }
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu
     * @return if succeeded
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);

        if(isOrganizer()) {
            menu.removeItem(R.id.action_open_business_activity);
        }

        MenuItem searchItem = menu.findItem((R.id.action_search));
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Search instantly
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(mSearchCg.getCheckedChipId(), query, new eventsAndOrganizerNamesCallback() {
                    @Override
                    public void onCallback(List<Event> events,
                                           Map<Event, String> eventsAndOrganizerNames,
                                           List<BlockedOrganizer> blockedOrganizers) {

                        adapter = new EventsAdapter(DashboardActivity.this,
                                events,
                                eventsAndOrganizerNames,
                                getApplicationContext(),
                                R.layout.row_events_dashboard);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchData(mSearchCg.getCheckedChipId(), newText, new eventsAndOrganizerNamesCallback() {
                    @Override
                    public void onCallback(List<Event> events,
                                           Map<Event, String> eventsAndOrganizerNames,
                                           List<BlockedOrganizer> blockedOrganizers) {


                        adapter = new EventsAdapter(DashboardActivity.this,
                                events,
                                eventsAndOrganizerNames,
                                getApplicationContext(),
                                R.layout.row_events_dashboard);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
                return false;
            }
        });

        // Show and hide the chips when search view is opened or closed
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // Show Chips
                mSearchHsv.setVisibility(View.VISIBLE);

                // Instantly search
                searchData(mSearchCg.getCheckedChipId(), "", new eventsAndOrganizerNamesCallback() {
                    @Override
                    public void onCallback(List<Event> events, Map<Event, String> eventsAndOrganizerNames, List<BlockedOrganizer> blockedOrganizers) {
                        adapter = new EventsAdapter(DashboardActivity.this,
                                events,
                                eventsAndOrganizerNames,
                                getApplicationContext(),
                                R.layout.row_events_dashboard);
                        mRecyclerView.setAdapter(adapter);
                    }
                });

                // Hide sort options
                menu.setGroupVisible(R.id.sortMethodGroup, false);

                // Deactivate pull down to refresh
                swipeRefreshLayout.setEnabled(false);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //Hide chips
                mSearchHsv.setVisibility(View.GONE);

                // Also reset recyclerview
                initWithPermission();
                mRecyclerView.setVisibility(View.VISIBLE);

                // Show sort options
                menu.setGroupVisible(R.id.sortMethodGroup, true);

                // Activate pull down to refresh
                swipeRefreshLayout.setEnabled(true);
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
            case R.id.action_sign_out:
                isOrganizer = false;
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, SplashActivity.class));
                return true;
            case R.id.action_open_business_activity:
                startActivity(new Intent(this, BusinessActivity.class));
                return true;
            case R.id.action_sort_startdate:
                sortMethod = Sort.startdate;
                initWithPermission();
                return true;
            case R.id.action_sort_attendees:
                sortMethod = Sort.attendees;
                initWithPermission();
                return true;
            case R.id.action_sort_distance:
                sortMethod = Sort.distance;
                initWithPermission();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks if a permission is granted and acts for both situations.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LocationService.MY_PERMISSION_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    setCurrentLocation();

                } else {
                    // permission denied, boo!
                    Toast.makeText(this, getString(R.string.error_location_permission_needed), Toast.LENGTH_LONG).show();

                    // Do not spam the request or message!
                    final Activity thisActivity = this;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Request the permission again
                            ActivityCompat.requestPermissions
                                    (thisActivity, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                                            LocationService.MY_PERMISSION_ACCESS_COARSE_LOCATION );
                        }
                    }, HANDLER_DELAY);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }

    }
}
