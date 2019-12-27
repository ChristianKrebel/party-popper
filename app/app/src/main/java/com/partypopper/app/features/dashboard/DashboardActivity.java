package com.partypopper.app.features.dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.database.repository.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private List<Event> modelList = new ArrayList<>();

    private FirebaseFirestore db;

    DashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.edToolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // RecyclerView
        mRecyclerView = findViewById(R.id.eventRv);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        showData();

    }

    private void showData() {
        db.collection("events")
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
                        adapter = new DashboardAdapter(DashboardActivity.this, modelList, getApplicationContext());
                        // set adapter to recyclerview
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem); //TODO use not deprecated method
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

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<DashboardModel, DashboardViewHolder>(options) {
            @Override
            public DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.row_dashboard for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_dashboard, parent, false);

                return new DashboardViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(DashboardViewHolder holder, int position, DashboardModel model) {
                holder.setDetails(model.getTitle(), model.getDate(), model.getImage(),
                        model.getOrganizer(), model.getVisitor_count());
            }
        };
        adapter.startListening();
        mRecyclerView.setAdapter(adapter);*/
    }
}
