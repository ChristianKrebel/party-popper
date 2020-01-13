package com.partypopper.app.features.eventDetail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.BlockedRepository;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.organizer.OrganizerActivity;
import com.partypopper.app.utils.BaseActivity;

import static com.partypopper.app.utils.Constants.*;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * The Activity for a specific event.
 * It has information about the event and also some about
 * its organizer.
 * A user can attend the event.
 */
public class EventDetailActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView mTitleTv, mDateTv, mTimeTv, mOrganizerTv, mVisitorCountTv, mDescriptionTv,
            organizerWebsiteTv, organizerPhoneTv, organizerAddressTv;
    private ImageView mBannerIv, organizerIv;
    private boolean isOrganizerInfoExpanded, isOrganizerFavored, isOrganizerBlocked, isAttending;
    private MaterialButton expandBt, favBt, blockOrganizerBt, attendEventBt;
    private LinearLayout organizerInfoLl;
    private AppBarLayout appBarLayout;
    private SupportMapFragment organizerLocationMf;
    private RatingBar mOrganizerRatingRb;
    private LatLng coords;
    private String organizerId, eventId, eventUrl;
    private Organizer organizer;
    private int eventAttending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set dark mode to always be active
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);



        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edToolbar);
        setSupportActionBar(toolbar);

        // Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);

        // get data from intent and set them to the views
        organizerId = getIntent().getStringExtra("organizer");
        eventId = getIntent().getStringExtra("eventId");

        mTitleTv = findViewById(R.id.edEventTitleTv);
        mTitleTv.setText(getIntent().getStringExtra("name"));

        eventAttending = getIntent().getIntExtra("going", 0);
        mVisitorCountTv = findViewById(R.id.edEventAttendersTv);

        mDateTv = findViewById(R.id.edEventDateTv);
        mTimeTv = findViewById(R.id.edEventTimeTv);

        Date startDate = (Date) getIntent().getSerializableExtra("startDate");
        Date endDate = (Date) getIntent().getSerializableExtra("endDate");


        String endDayStr = android.text.format.DateFormat.format("EE", endDate).toString();
        String startDateStr = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate);
        String endDateStr = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate);
        if(startDateStr.equals(endDateStr)) {
            String startDayStr = android.text.format.DateFormat.format("EEEE", startDate).toString();
            mDateTv.setText(startDayStr + ", " + startDateStr);
        } else {
            String startDayStr = android.text.format.DateFormat.format("EE", startDate).toString();
            mDateTv.setText(startDayStr + ", " + startDateStr + " - " + endDayStr + ", " + endDateStr);
        }

        String startTimeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(startDate);
        String endTimeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(endDate);
        mTimeTv.setText(startTimeStr + " - " + endTimeStr);

        eventUrl = getIntent().getStringExtra("eventUrl");

        mBannerIv = findViewById(R.id.edBannerIv);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.edToolbarLayout);
        appBarLayout = findViewById(R.id.edAppBarLayout);

        attendEventBt = findViewById(R.id.edAttendEventBt);
        final TextView organizerLinkTv = findViewById(R.id.coOrganizerLinkTv);
        final TextView organizerPhoneTv = findViewById(R.id.coOrganizerPhoneTv);
        final TextView organizerAddressTv = findViewById(R.id.coOrganizerAddressTv);
        final MaterialButton blockOrganizerBt = findViewById(R.id.coBlockOrganizerBt);
        final View gradientV = findViewById(R.id.edGradientV);

        EventsRepository eventsRepository = EventsRepository.getInstance();
        eventsRepository.getEventByEventId(eventId).addOnCompleteListener(new OnCompleteListener<Event>() {
            @Override
            public void onComplete(@NonNull Task<Event> task) {
                if (task.isSuccessful()) {
                    Event event = task.getResult();
                    Picasso.get()
                        .load(event.getImage())
                        .into(mBannerIv, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                                // Set start height of image
                                setStartAppBarOffset();

                                // Set the color of the tool bar and button to one of the image
                                setColorsFromImage(collapsingToolbarLayout, attendEventBt, organizerLinkTv, organizerPhoneTv, gradientV);


                                // Set the title to only be visible when the tool bar is collapsed
                                setTitleVisibilityListener(collapsingToolbarLayout);
                            }

                            @Override
                            public void onError(Exception ex) {
                                Log.e(ex.toString(), ex.getLocalizedMessage());
                            }
                        });
                }
            }
        });

        mDescriptionTv = findViewById(R.id.edEventDescriptionTv);
        mDescriptionTv.setText(getIntent().getStringExtra("description"));



        // Organizer info is not expanded by default
        isOrganizerInfoExpanded = false;


        // no need for listener because of onClick in XML
        expandBt = findViewById(R.id.edOrganizerExpandBt);
        organizerInfoLl = findViewById(R.id.edOrganizerInfoLl);
        organizerInfoLl.setVisibility(View.GONE);

        favBt = findViewById(R.id.edOrganizerFavBt);


        // need for listener for fragment clicks
        organizerAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrganizerAddressTextViewClick(v);
            }
        });

        blockOrganizerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBlockOrganizerButtonClick(v);
            }
        });


        // map
        coords = getIntent().getParcelableExtra("location");
        // get MapFragment
        organizerLocationMf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.coOrganizerLocationMf);
    }

    /**
     * When this Activity gets resumed (in the view) update some Views.
     */
    @Override
    public void onResume() {
        super.onResume();
        // get and set organizer data
        showData(organizerId, this);
        onResumeSetOrganizerFavoredState();
        onResumeSetAttendingState();
        onResumeSetBlockedOrganizerState();
    }

    private void setColorsFromImage(final CollapsingToolbarLayout collapsingToolbarLayout, final Button attendEventBt, final TextView organizerLinkTv, final TextView organizerPhoneTv, final View gradientV) {
        Drawable mBannerDrawable = mBannerIv.getDrawable();
        Bitmap mBannerBm = ((BitmapDrawable) mBannerDrawable).getBitmap();
        Palette.from(mBannerBm).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int imageColor = palette.getMutedColor(R.attr.colorPrimary);

                collapsingToolbarLayout.setContentScrimColor(imageColor);
                attendEventBt.getBackground().setColorFilter(imageColor, PorterDuff.Mode.SRC);
                organizerLinkTv.setLinkTextColor(changeValueOfColor(imageColor, 1.4f));
                organizerPhoneTv.setLinkTextColor(changeValueOfColor(imageColor, 1.4f));

                // Set status bar color
                getWindow().setStatusBarColor(changeValueOfColor(imageColor, 0.8f));

                // Set scrim color
                Drawable unwrappedDrawable = gradientV.getBackground();
                final Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, changeValueOfColor(imageColor, 0.6f));
            }
        });
    }

    private void setStartAppBarOffset() {
        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                int heightPx = mBannerIv.getHeight();
                int widthPx = mBannerIv.getWidth();
                float ratio = widthPx / heightPx;
                // only collapse if image has bigger height than width
                if (ratio < 1.0f) {
                    setAppBarOffset(heightPx / 2, appBarLayout);
                }
            }
        });
    }

    private void setTitleVisibilityListener(final CollapsingToolbarLayout collapsingToolbarLayout) {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mTitleTv.getText());
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }

    private void showData(final String organizerId, final OnMapReadyCallback onMapReadyCallback) {
        OrganizerRepository organizerRepository = OrganizerRepository.getInstance();
        organizerRepository.getOrganizerById(organizerId).addOnCompleteListener(new OnCompleteListener<Organizer>() {
            @Override
            public void onComplete(@NonNull Task<Organizer> task) {
                if (task.isSuccessful()) {
                    organizer = task.getResult();

                    mOrganizerTv = findViewById(R.id.edOrganizerNameTv);
                    mOrganizerTv.setText(organizer.getName());

                    mOrganizerRatingRb = findViewById(R.id.edOrganizerRb);
                    mOrganizerRatingRb.setRating(organizer.getAvgRating());

                    organizerIv = findViewById(R.id.edOrganizerIv);
                    Picasso.get().load(organizer.getImage()).into(organizerIv);

                    organizerWebsiteTv = findViewById(R.id.coOrganizerLinkTv);
                    organizerWebsiteTv.setText(organizer.getWebsite());

                    organizerPhoneTv = findViewById(R.id.coOrganizerPhoneTv);
                    organizerPhoneTv.setText(organizer.getPhone());

                    organizerAddressTv = findViewById(R.id.coOrganizerAddressTv);
                    organizerAddressTv.setText(organizer.getAddress());

                    organizerLocationMf.getMapAsync(onMapReadyCallback);
                }
            }
        });
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu
     * @return if succeeded
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle item selection of the menu.
     * @param item
     * @return if succeeded or with item on default
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_event_open_event_link:
                openUrl(eventUrl);
                return true;
            case R.id.action_event_share:
                share("text/html", mTitleTv.getText().toString(), eventUrl);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Adds the back button.
     *
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onExpandButtonClick(View view) {
        if (isOrganizerInfoExpanded) {
            organizerInfoLl.setVisibility(View.GONE);
            expandBt.setIconResource(R.drawable.ic_keyboard_arrow_down_white_trans30_24dp);
        } else {
            organizerInfoLl.setVisibility(View.VISIBLE);
            expandBt.setIconResource(R.drawable.ic_keyboard_arrow_up_white_trans30_24dp);
        }
        isOrganizerInfoExpanded = !isOrganizerInfoExpanded;
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onBannerImageViewClick(View view) {
        setAppBarOffset(0, appBarLayout);
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onAttendEventButtonClick(View view) {
        final EventsRepository eventsRepository = EventsRepository.getInstance();
        eventsRepository.hasJoined(eventId).addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    isAttending = task.getResult();
                    Log.d("Attend", "ATTENDING????????????????????" + isAttending);
                    if (isAttending) {
                        eventsRepository.leaveEvent(eventId).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                            @Override
                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                if (task.isSuccessful()) {
                                    showText(getString(R.string.event_left));
                                    eventAttending--;
                                    isAttending = !isAttending;
                                    changeAttendingUIstate(isAttending);
                                    Log.d("Attend", "LEFT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                }
                            }
                        });
                    } else {
                        eventsRepository.joinEvent(eventId).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                            @Override
                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                if (task.isSuccessful()) {
                                    showText(getString(R.string.event_attended));
                                    eventAttending++;
                                    isAttending = !isAttending;
                                    changeAttendingUIstate(isAttending);
                                    Log.d("Attend", "JOINED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onOrganizerClick(View view) {
        Intent intent = new Intent(view.getContext(), OrganizerActivity.class);
        intent.putExtra("organizerId", organizerId);
        intent.putExtra("organizerAddress", organizer.getAddress());
        intent.putExtra("organizerDescription", organizer.getDescription());
        intent.putExtra("organizerName", organizer.getName());
        intent.putExtra("organizerPhone", organizer.getPhone());
        intent.putExtra("organizerRating", organizer.getAvgRating());
        intent.putExtra("organizerWebsite", organizer.getWebsite());
        intent.putExtra("organizerCoordsLat", coords.latitude);
        intent.putExtra("organizerCoordsLng", coords.longitude);

        startActivity(intent);
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onOrganizerFavButtonClick(View view) {
        FollowRepository followRepository = FollowRepository.getInstance();
        if (isOrganizerFavored) {
            followRepository.unfollowOrganizer(organizerId);
        } else {
            followRepository.followOrganizer(organizerId);
        }
        isOrganizerFavored = !isOrganizerFavored;
        changeOrganizerFavButtonUIstate(isOrganizerFavored);
    }

    /**
     * Changes the UI of the Favorate-Button (Follow-Button).
     *
     * @param favor
     */
    private void changeOrganizerFavButtonUIstate(boolean favor) {
        if(favor) {
            favBt.setIconResource(R.drawable.ic_favorite_white_trans30_24dp);
        } else {
            favBt.setIconResource(R.drawable.ic_favorite_border_white_trans30_24dp);
        }
    }

    /**
     * Checks if user is following and updates the View.
     */
    private void onResumeSetOrganizerFavoredState() {
        // Follows organizer?
        isOrganizerFavored = false;
        changeOrganizerFavButtonUIstate(isOrganizerFavored);
        FollowRepository.getInstance().getFollowing().addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().contains(organizerId)) {
                        isOrganizerFavored = true;
                        changeOrganizerFavButtonUIstate(isOrganizerFavored);
                    }
                }
            }
        });
    }

    /**
     * Changes the UI of the Attend-Button.
     *
     * @param isAttending
     */
    private void changeAttendingUIstate(boolean isAttending) {
        mVisitorCountTv.setText(eventAttending + " " + getString(R.string.are_attending));
        if (isAttending) {
            attendEventBt.setText(getString(R.string.attending_to_event));
            Log.d("Attend", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SET TEXT ATTENDING");
        } else {
            attendEventBt.setText(getString(R.string.attend_to_event));
            Log.d("Attend", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SET TEXT ATTEND");
        }
    }

    /**
     * Checks if user is attending and updates the View.
     */
    private void onResumeSetAttendingState() {
        final EventsRepository eventsRepository = EventsRepository.getInstance();
        eventsRepository.getEventByEventId(eventId).addOnCompleteListener(new OnCompleteListener<Event>() {
            @Override
            public void onComplete(@NonNull Task<Event> task) {
                if (task.isSuccessful()) {
                    eventAttending = task.getResult().getGoing();

                    // is attending?
                    eventsRepository.hasJoined(eventId).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
                                isAttending = task.getResult();
                                Log.d("Attend", "ATTENDING????????????????????" + isAttending);
                                changeAttendingUIstate(isAttending);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onOrganizerAddressTextViewClick(View view) {
        showText(getString(R.string.copied_address));

        TextView textView = (TextView) view;
        copyTextToClipboard("Address", textView.getText(), view.getContext());
    }

    /**
     * Checks if user has blocked the organizer and updates the View.
     */
    public void onResumeSetBlockedOrganizerState() {
        final BlockedRepository blockedRepository = BlockedRepository.getInstance();
        blockedRepository.hasBlocked(organizerId).addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    Log.d("BLOCKED", "Organizer is blocked: " + task.getResult() + "!!!!!!!!!!!!!!!");
                }
            }
        });
    }

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onBlockOrganizerButtonClick(View view) {
        showText(getString(R.string.organizer_blocked));
        FollowRepository followRepository = FollowRepository.getInstance();
        followRepository.blockOrganizer(organizerId);
    }

    /**
     * When the map is loaded set a marker and move and zoom its camera to the marker
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker
        googleMap.addMarker(new MarkerOptions().position(coords)
                .title(mOrganizerTv.getText().toString()));

        // move the map's camera to the same location and zoom
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(MAP_ZOOM).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
