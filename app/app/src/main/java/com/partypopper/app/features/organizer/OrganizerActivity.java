package com.partypopper.app.features.organizer;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import lombok.Getter;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.features.organizer.ui.main.OrganizerInfoFragment;
import com.partypopper.app.features.organizer.ui.main.OrganizerRateDialog;
import com.partypopper.app.features.organizer.ui.main.SectionsPagerAdapter;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import static com.partypopper.app.utils.Constants.HANDLER_DELAY;

public class OrganizerActivity extends BaseActivity implements OrganizerRateDialog.OrganizerRateDialogListener {

    private ImageView logoIv;
    private String name, organizerId;
    private AppBarLayout appBarLayout;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // get data from intent and set them to the views
        name = getIntent().getStringExtra("organizerName");
        organizerId = getIntent().getStringExtra("organizerId");


        logoIv = findViewById(R.id.oBannerIv);
        if (getIntent().hasExtra("organizerImage")) {
            byte[] bytes = getIntent().getByteArrayExtra("organizerImage");
            logoIv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }


        // Tabs and more
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get current fragment and position
                final int pos = viewPager.getCurrentItem();
                final Fragment activeFragment = sectionsPagerAdapter.getItem(pos);
                updateOrganizerRatingUIstate(pos, activeFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Get current fragment and position
                final int pos = viewPager.getCurrentItem();
                final Fragment activeFragment = sectionsPagerAdapter.getItem(pos);
                updateOrganizerRatingUIstate(pos, activeFragment);
            }
        });

        // Action bar
        Toolbar toolbar = findViewById(R.id.oToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.oToolbarLayout);
        appBarLayout = findViewById(R.id.oAppBarLayout);


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
                    collapsingToolbarLayout.setTitle(name);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        // Reset scrim's color
        final View gradientV = findViewById(R.id.oGradientV);
        Drawable unwrappedDrawable = gradientV.getBackground();
        final Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(getBaseContext(), R.color.scrim_topdown_reset));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void applyRating(float rating) {
        FollowRepository followRepository = FollowRepository.getInstance();
        followRepository.rateOrganizer(organizerId, rating);

        showText(getString(R.string.organizer_rated) + " " + (int) rating);

        // Update AVG rating in UI
        // Get current fragment and position
        final int pos = viewPager.getCurrentItem();
        final Fragment activeFragment = sectionsPagerAdapter.getItem(pos);

        // Delay needed because calculation of AVG rating needs time
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateOrganizerRatingUIstate(pos, activeFragment);
            }
        }, HANDLER_DELAY);
    }

    private void updateOrganizerRatingUIstate(final int pos, final Fragment activeFragment) {
        if(pos == 0) {
            ((OrganizerInfoFragment) activeFragment).onResumeSetOrganizerRating();
        }
    }

    public void onBannerImageViewClick(View view) {
        setAppBarOffset(0, appBarLayout);
    }


}