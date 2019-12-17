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

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import java.io.ByteArrayOutputStream;

public class DashboardActivity extends BaseActivity {

    private final int COMPRESSION_QUALITY = 98;

    private RecyclerView mRecyclerView;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerAdapter adapter;

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

        // send query to Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Data");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Data")
                .limitToLast(50);
        FirebaseRecyclerOptions<DashboardModel> options =
                new FirebaseRecyclerOptions.Builder<DashboardModel>()
                        .setQuery(query, DashboardModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<DashboardModel, DashboardViewHolder>(options) {
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
                holder.setOnClickListener(new EventClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // Views
                        ImageView mBannerIv = view.findViewById(R.id.rBannerIv);
                        TextView mTitleTv = view.findViewById(R.id.rTitleTv);
                        TextView mDateTv = view.findViewById(R.id.rDateTv);
                        TextView mOrganizerTv = view.findViewById(R.id.rOrganizerTv);
                        TextView mVisitorCountTv = view.findViewById(R.id.rVisitorCountTv);

                        // get data from views
                        Drawable mBannerDrawable = mBannerIv.getDrawable();
                        Bitmap mBanner = ((BitmapDrawable) mBannerDrawable).getBitmap();
                        String mTitle = mTitleTv.getText().toString();
                        String mDate = mDateTv.getText().toString();
                        String mOrganizer = mOrganizerTv.getText().toString();
                        String mVisitorCount = mVisitorCountTv.getText().toString();

                        // pass data to new activity
                        Intent intent = new Intent(view.getContext(), DashboardActivity.class);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mBanner.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
                        byte[] bytes = stream.toByteArray();
                        intent.putExtra("image", bytes);
                        intent.putExtra("title", mTitle);
                        intent.putExtra("date", mDate);
                        intent.putExtra("organizer", mOrganizer);
                        intent.putExtra("visitor_count", mVisitorCount);

                        // finally start the activity
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        // TODO
                    }
                });
            }
        };
        adapter.startListening();
        mRecyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
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
     * @param item
     * @return if succeeded or with item on default
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence text;
        switch (item.getItemId()) {
            case R.id.action_settings:
                text = "Settings";
                testToast(text);
                return true;
                // TODO more cases
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Query a String to the Firebase database and get results in the recyclerview
     * @param searchText
     */
    private void firebaseSearch(String searchText) {
        // Different query than the one in the onStart method
        // TODO shorten redundant code?
        // TODO exchange with Elastic Search for 'contains'-ability and no case sensitivity
        Query firebaseSearchQuery = mDatabaseReference.orderByChild("title")
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
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * Print a toast for testing purposes
     * @param text
     */
    protected void testToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}