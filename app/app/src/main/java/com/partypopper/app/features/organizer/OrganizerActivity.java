package com.partypopper.app.features.organizer;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.organizer.ui.main.OrganizerInfoFragment;
import com.partypopper.app.features.organizer.ui.dialog.OrganizerRateDialog;
import com.partypopper.app.features.organizer.ui.main.OrganizerSectionsPagerAdapter;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;
import com.squareup.picasso.Picasso;

import static com.partypopper.app.utils.Constants.HANDLER_DELAY;

/**
 * The Activity for the profile and overview of an organizer.
 * It is tabbed and has Fragments.
 * Tab 1: Info
 * Tab 2: Events of organizer
 */
public class OrganizerActivity extends BaseActivity implements OrganizerRateDialog.OrganizerRateDialogListener {

    private ImageView logoIv;
    private String name, organizerId;
    private AppBarLayout appBarLayout;

    private OrganizerSectionsPagerAdapter organizerSectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Action bar
        Toolbar toolbar = findViewById(R.id.oToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.oToolbarLayout);
        appBarLayout = findViewById(R.id.oAppBarLayout);

        // get data from intent and set them to the views
        name = getIntent().getStringExtra("organizerName");
        organizerId = getIntent().getStringExtra("organizerId");

        logoIv = findViewById(R.id.oBannerIv);

        OrganizerRepository organizerRepository = OrganizerRepository.getInstance();
        organizerRepository.getOrganizerById(organizerId).addOnCompleteListener(new OnCompleteListener<Organizer>() {
            @Override
            public void onComplete(@NonNull Task<Organizer> task) {
                if (task.isSuccessful()) {
                    Organizer organizer = task.getResult();
                    Picasso.get()
                            .load(organizer.getImage())
                            .into(logoIv, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
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


        // Tabs and more
        organizerSectionsPagerAdapter = new OrganizerSectionsPagerAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(organizerSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get current fragment and position
                final int pos = viewPager.getCurrentItem();
                final Fragment activeFragment = organizerSectionsPagerAdapter.getItem(pos);
                updateOrganizerRatingUIstate(pos, activeFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Get current fragment and position
                final int pos = viewPager.getCurrentItem();
                final Fragment activeFragment = organizerSectionsPagerAdapter.getItem(pos);
                updateOrganizerRatingUIstate(pos, activeFragment);
            }
        });

        // Reset scrim's color
        final View gradientV = findViewById(R.id.oGradientV);
        Drawable unwrappedDrawable = gradientV.getBackground();
        final Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(getBaseContext(), R.color.scrim_topdown_reset));

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
                    collapsingToolbarLayout.setTitle(name);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
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
     * Rates an organizer and then tries to update the view.
     * This method is called when a RateDialog got input.
     *
     * @param rating
     */
    @Override
    public void applyRating(float rating) {
        FollowRepository followRepository = FollowRepository.getInstance();
        followRepository.rateOrganizer(organizerId, rating);

        showText(getString(R.string.organizer_rated) + " " + (int) rating);

        // Update AVG rating in UI
        // Get current fragment and position
        final int pos = viewPager.getCurrentItem();
        final Fragment activeFragment = organizerSectionsPagerAdapter.getItem(pos);

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

    /**
     * OnClickListener-method.
     *
     * @param view
     */
    public void onBannerImageViewClick(View view) {
        setAppBarOffset(0, appBarLayout);
    }


}