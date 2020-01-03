package com.partypopper.app.features.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.GeoPoint;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.organizer.OrganizerActivity;
import com.partypopper.app.utils.BaseActivity;

import static com.partypopper.app.utils.Constants.*;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

public class EventDetailActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView mTitleTv, mDateTv, mTimeTv, mOrganizerTv, mVisitorCountTv, mDescriptionTv,
            organizerWebsiteTv, organizerPhoneTv, organizerAddressTv;
    private ImageView mBannerIv, organizerIv;
    private boolean isOrganizerInfoExpanded, isOrganizerFavored;
    private MaterialButton expandBt, favBt, blockOrganizerBt;
    private LinearLayout organizerInfoLl;
    private AppBarLayout appBarLayout;
    private SupportMapFragment organizerLocationMf;
    private RatingBar mOrganizerRatingRb;
    private LatLng coords;
    private String organizerId, eventUrl;
    private Organizer organizer;

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

        mTitleTv = findViewById(R.id.edEventTitleTv);
        mTitleTv.setText(getIntent().getStringExtra("name"));

        mVisitorCountTv = findViewById(R.id.edEventAttendersTv);
        mVisitorCountTv.setText(getIntent().getIntExtra("going", 0) + " " + getString(R.string.are_attending));

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

        if (getIntent().hasExtra("image")) {
            byte[] bytes = getIntent().getByteArrayExtra("image");
            mBannerIv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        mDescriptionTv = findViewById(R.id.edEventDescriptionTv);
        mDescriptionTv.setText(getIntent().getStringExtra("description"));





        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.edToolbarLayout);
        appBarLayout = findViewById(R.id.edAppBarLayout);

        // Set start height of image
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

        // Set the title to only be visible when the tool bar is collapsed
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



        // Set the color of the tool bar and button to one of the image
        final Button attendEventBt = findViewById(R.id.edAttendEventBt);
        final TextView organizerLinkTv = findViewById(R.id.coOrganizerLinkTv);
        final TextView organizerPhoneTv = findViewById(R.id.coOrganizerPhoneTv);
        final TextView organizerAddressTv = findViewById(R.id.coOrganizerAddressTv);
        final MaterialButton blockOrganizerBt = findViewById(R.id.coBlockOrganizerBt);
        final View gradientV = findViewById(R.id.edGradientV);
        Drawable mBannerDrawable = mBannerIv.getDrawable();
        if (getIntent().hasExtra("image")) {
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

        // Organizer info is not expanded by default
        isOrganizerInfoExpanded = false;

        isOrganizerFavored = false;


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
        // default coords
        coords = new LatLng(0,0);
        // get MapFragment
        organizerLocationMf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.coOrganizerLocationMf);


        // get and set organizer data
        showData(organizerId, this);
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

                    //GeoPoint geoPoint = organizer.getCoordinates();
                    //coords = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
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
     * Handle item selection of the menu
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

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

    public void onBannerImageViewClick(View view) {
        setAppBarOffset(0, appBarLayout);
    }

    public void onAttendEventButtonClick(View view) {
        showText("onAttendEventButtonClick");
    }

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

        Drawable organizerIvDrawable = organizerIv.getDrawable();
        if (organizerIvDrawable != null) {
            Bitmap mBanner = ((BitmapDrawable) organizerIvDrawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBanner.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
            byte[] bytes = stream.toByteArray();
            intent.putExtra("organizerImage", bytes);
        }

        startActivity(intent);
    }

    public void onOrganizerFavButtonClick(View view) {
        showText("onOrganizerFavButtonClick");

        if(isOrganizerFavored) {
            favBt.setIconResource(R.drawable.ic_favorite_border_white_trans30_24dp);
        } else {
            favBt.setIconResource(R.drawable.ic_favorite_white_trans30_24dp);
        }
        isOrganizerFavored = !isOrganizerFavored;
    }

    public void onOrganizerAddressTextViewClick(View view) {
        showText(getString(R.string.copied_address));

        TextView textView = (TextView) view;
        copyTextToClipboard("Address", textView.getText(), view.getContext());
    }

    public void onBlockOrganizerButtonClick(View view) {
        showText("onBlockOrganizerButtonClick");
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
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(15.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
