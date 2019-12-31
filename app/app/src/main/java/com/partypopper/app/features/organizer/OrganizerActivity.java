package com.partypopper.app.features.organizer;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import lombok.Getter;

import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.features.organizer.ui.main.OrganizerRateDialog;
import com.partypopper.app.features.organizer.ui.main.SectionsPagerAdapter;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

public class OrganizerActivity extends BaseActivity implements OrganizerRateDialog.OrganizerRateDialogListener {

    private ImageView logoIv;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // get data from intent and set them to the views
        name = getIntent().getStringExtra("organizerName");

        logoIv = findViewById(R.id.oBannerIv);
        if (getIntent().hasExtra("organizerImage")) {
            byte[] bytes = getIntent().getByteArrayExtra("organizerImage");
            logoIv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }


        // Tabs and more
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // Action bar
        Toolbar toolbar = findViewById(R.id.oToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.oToolbarLayout);
        AppBarLayout appBarLayout = findViewById(R.id.oAppBarLayout);


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

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void applyRating(float rating) {
        showText(Float.toString(rating));
    }


}