package com.partypopper.app.features.organizer;

import android.os.Bundle;
import android.widget.EditText;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.utils.DatePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;


public class PublishEventActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publishevent);

        Toolbar toolbar = findViewById(R.id.pubToolbar);
        setSupportActionBar(toolbar);

        // Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);

        DatePicker dp = new DatePicker(this, R.id.pubDateD);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onPublishClick() {
        EditText publisheventTitleWdg = findViewById(R.id.pubTitlePt);
        String eventTitle = publisheventTitleWdg.getText().toString();
        EditText publisheventDateWdg = findViewById(R.id.pubDateD);
        String eventDate = publisheventDateWdg.getText().toString();
        EditText publisheventTimeWdg = findViewById(R.id.pubTimeT);
        String eventTime = publisheventTimeWdg.getText().toString();
        EditText publisheventDescriptionWdg = findViewById(R.id.pubDescriptionMt);
        String eventDescription = publisheventDescriptionWdg.getText().toString();
    }
}
