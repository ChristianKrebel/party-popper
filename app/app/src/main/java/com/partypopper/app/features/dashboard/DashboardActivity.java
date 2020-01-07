package com.partypopper.app.features.dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.util.LocationService;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.events.EventsAdapter;
import com.partypopper.app.features.organizer.BusinessActivity;
import com.partypopper.app.features.organizer.PublishEventActivity;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.database.repository.*;

import static com.partypopper.app.utils.Constants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private RecyclerView mRecyclerView;

    private EventsAdapter adapter;

    private HorizontalScrollView mSearchHsv;
    private ChipGroup mSearchCg;


    private LatLng currentLocation;


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


        // RecyclerView
        mRecyclerView = findViewById(R.id.eventRv);
        mRecyclerView.setHasFixedSize(true);

        initWithPermission();
    }

    protected void initWithPermission() {

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
                initRecyclerView();
            }
        } else {    // if not flexible permissions, all needed permissions are granted
            setCurrentLocation();
            initRecyclerView();
        }
    }

    private void setCurrentLocation() {
        LocationService locationService = LocationService.getLocationManager(this);
        currentLocation = new LatLng(locationService.latitude, locationService.longitude);
    }

    private void initRecyclerView() {
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

        eventsRepository.getNearbyEvents
                (currentLocation.latitude,
                currentLocation.longitude,
                STANDARD_DISTANCE,
                EVENTS_AMOUNT).addOnCompleteListener(new OnCompleteListener<List<Event>>() {
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

                                organizerRepository.getOrganizerById(event.getOrganizer()).addOnCompleteListener(new OnCompleteListener<Organizer>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Organizer> task) {
                                        if (task.isSuccessful()) {
                                            eventsAndOrganizerNames.put(event, task.getResult().getName());
                                            if (b == (events.size() - 1)) {
                                                dbCallback.onCallback(events, eventsAndOrganizerNames);
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


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.edOrganizersC:
                // TODO
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

                                organizerRepository.getOrganizerById(event.getOrganizer()).addOnCompleteListener(new OnCompleteListener<Organizer>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Organizer> task) {
                                        if (task.isSuccessful()) {
                                            eventsAndOrganizerNames.put(event, task.getResult().getName());
                                            if (b == (events.size() - 1)) {
                                                dbCallback.onCallback(events, eventsAndOrganizerNames);
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

                                organizerRepository.getOrganizerById(event.getOrganizer()).addOnCompleteListener(new OnCompleteListener<Organizer>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Organizer> task) {
                                        if (task.isSuccessful()) {
                                            eventsAndOrganizerNames.put(event, task.getResult().getName());
                                            if (b == (events.size() - 1)) {
                                                dbCallback.onCallback(events, eventsAndOrganizerNames);
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

        // Search instantly
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(mSearchCg.getCheckedChipId(), query, new eventsAndOrganizerNamesCallback() {
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchData(mSearchCg.getCheckedChipId(), newText, new eventsAndOrganizerNamesCallback() {
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
                return false;
            }
        });

        // Show and hide the chips when search view is opened or closed
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mSearchHsv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mSearchHsv.setVisibility(View.GONE);

                // Also reset recyclerview
                initWithPermission();
                mRecyclerView.setVisibility(View.VISIBLE);
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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, AuthenticationActivity.class));
                return true;
            case R.id.action_open_business_activity:
                startActivity(new Intent(this, BusinessActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LocationService.MY_PERMISSION_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    setCurrentLocation();
                    initRecyclerView();

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
