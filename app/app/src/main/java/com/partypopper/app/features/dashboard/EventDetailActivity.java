package com.partypopper.app.features.dashboard;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView mTitleTv, mDateTv, mTimeTv, mOrganizerTv, mVisitorCountTv, mDescriptionTv;
    private ImageView mBannerIv;
    private boolean isOrganizerInfoExpanded, isOrganizerFavored;
    private MaterialButton expandBt, favBt, blockOrganizerBt;
    private LinearLayout organizerInfoLl;
    private AppBarLayout appBarLayout;
    private MapFragment organizerLocationMf;

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
        mTitleTv = findViewById(R.id.edEventTitleTv);
        mTitleTv.setText(getIntent().getStringExtra("name"));

        mVisitorCountTv = findViewById(R.id.edEventAttendersTv);
        mVisitorCountTv.setText(getIntent().getIntExtra("going", 0) + " " + getString(R.string.are_attending));

        mOrganizerTv = findViewById(R.id.edOrganizerNameTv);
        mOrganizerTv.setText(getIntent().getStringExtra("organizer"));

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


        mBannerIv = findViewById(R.id.edBannerIv);
        byte[] bytes = getIntent().getByteArrayExtra("image");
        mBannerIv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

        mDescriptionTv = findViewById(R.id.edEventDescriptionTv);
        mDescriptionTv.setText(getIntent().getStringExtra("description"));




        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.edToolbarLayout);
        appBarLayout = findViewById(R.id.edAppBarLayout);

        // Set start height of image
        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                ImageView bannerIv = findViewById(R.id.edBannerIv);
                int heightPx = bannerIv.getHeight();
                int widthPx = bannerIv.getWidth();
                float ratio = widthPx / heightPx;
                // only collapse if image has bigger height than width
                if (ratio < 1.0f) {
                    setAppBarOffset(heightPx / 2);
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


            }
        });

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
        // get MapFragment
        organizerLocationMf = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.coOrganizerLocationMf);
        organizerLocationMf.getMapAsync(this);


        /*
        // Views
        mBannerIv = findViewById(R.id.edBannerIv);
        mTitleTv = findViewById(R.id.titleTv);
        mDateTv = findViewById(R.id.dateTv);
        mOrganizerTv = findViewById(R.id.organizerTv);
        mVisitorCountTv = findViewById(R.id.visitorCountTv);

        // get data from intent and set them to the views
        byte[] bytes = getIntent().getByteArrayExtra("image");
        mBannerIv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        mTitleTv.setText(getIntent().getStringExtra("title"));
        mDateTv.setText(getIntent().getStringExtra("date"));
        mOrganizerTv.setText(getIntent().getStringExtra("organizer"));
        mVisitorCountTv.setText(getIntent().getStringExtra("visitor_count"));
        */
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
                showText("action_event_open_event_link");
                return true;
            case R.id.action_event_share:
                showText("action_event_share");
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
        showText("onBannerImageViewClick");
    }

    public void onAttendEventButtonClick(View view) {
        showText("onAttendEventButtonClick");
    }

    public void onOrganizerClick(View view) {
        showText("onOrganizerClick");
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
        showText("onOrganizerAddressTextViewClick");

        TextView textView = (TextView) view;
        copyTextToClipboard("Address", textView.getText());
    }

    public void onBlockOrganizerButtonClick(View view) {
        showText("onBlockOrganizerButtonClick");
    }

    /**
     * Sets an offset for the appbar (how much of the imageView is shown) and animates it
     * @param offset
     */
    private void setAppBarOffset(int offset) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt();
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                    appBarLayout.requestLayout();
                }
            });
            valueAnimator.setIntValues(0, -offset);
            valueAnimator.setDuration(400);
            valueAnimator.start();
        }
    }

    /**
     * When the map is loaded set a marker and move and zoom its camera to the marker
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker
        TextView titleTv = findViewById(R.id.edOrganizerNameTv);
        LatLng coords = new LatLng(52.131, 8.666); // TODO get real coords
        googleMap.addMarker(new MarkerOptions().position(coords)
                .title(titleTv.getText().toString()));

        // move the map's camera to the same location and zoom
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(15.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
