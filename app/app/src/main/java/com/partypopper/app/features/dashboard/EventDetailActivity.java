package com.partypopper.app.features.dashboard;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import androidx.appcompat.app.ActionBar;

public class EventDetailActivity extends BaseActivity {

    private TextView mTitleTv, mDateTv, mOrganizerTv, mVisitorCountTv;
    private ImageView mBannerIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Event Details");
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);

        // Views
        mBannerIv = findViewById(R.id.bannerIv);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
