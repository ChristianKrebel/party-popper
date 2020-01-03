package com.partypopper.app.features.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.utils.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.util.Date;
import java.util.List;


public class PublishEventActivity extends BaseActivity {
    private EditText publisheventTitleWdg;
    private EditText publisheventStartdateWdg;
    private EditText publisheventEnddateWdg;
    private TimePicker publisheventStarttimeWdg;
    private TimePicker publisheventEndtimeWdg;
    private EditText publisheventDescriptionWdg;
    private Button updateButtonWdg;
    private Button publisheventButtonWdg;
    private ImageView publisheventImageWdg;

    private final FirebaseUser currentUser = mAuth.getCurrentUser();

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

        DatePicker dp1 = new DatePicker(this, R.id.pubStartdateD);
        DatePicker dp2 = new DatePicker(this, R.id.pubEnddateD);

        publisheventTitleWdg = findViewById(R.id.pubTitlePt);
        publisheventStartdateWdg = findViewById(R.id.pubStartdateD);
        publisheventEnddateWdg = findViewById(R.id.pubEnddateD);
        publisheventStarttimeWdg = findViewById(R.id.pubStarttimeT);
        publisheventEndtimeWdg = findViewById(R.id.pubEndtimeT);
        publisheventDescriptionWdg = findViewById(R.id.pubDescriptionMt);
        updateButtonWdg = findViewById(R.id.pubUpdateBtn);
        publisheventButtonWdg = findViewById(R.id.pubPublishBtn);
        publisheventImageWdg = findViewById(R.id.publisheventImageWdg);
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

    public void onPublishClick(View view) {
        String eventTitle = publisheventTitleWdg.getText().toString();
        String eventStartdate = publisheventStartdateWdg.getText().toString();
        String eventEnddate = publisheventEnddateWdg.getText().toString();
        int eventStarttimeHour = publisheventStarttimeWdg.getHour();
        int eventStarttimeMinute = publisheventStarttimeWdg.getMinute();
        int eventEndtimeHour = publisheventEndtimeWdg.getHour();
        int eventEndtimeMinute = publisheventEndtimeWdg.getMinute();
        String eventDescription = publisheventDescriptionWdg.getText().toString();

        String[] startDateArr = eventStartdate.split("/");
        int startDateDay = Integer.parseInt(startDateArr[0].trim());
        int startDateMonth = Integer.parseInt(startDateArr[1].trim());
        int startDateYear = Integer.parseInt(startDateArr[2].trim());
        //showText(startDateDay + " " + startDateMonth + " " + startDateYear);

        String[] endDateArr = eventEnddate.split("/");
        int endDateDay = Integer.parseInt(endDateArr[0].trim());
        int endDateMonth = Integer.parseInt(endDateArr[1].trim());
        int endDateYear = Integer.parseInt(endDateArr[2].trim());

        Event event = new Event();
        event.setName(eventTitle);
        event.setOrganizer(currentUser.getUid());
        event.setGoing(0);
        event.setStartDate(new Date(startDateYear, startDateMonth, startDateDay, eventStarttimeHour, eventStarttimeMinute));
        event.setEndDate(new Date(endDateYear, endDateMonth, endDateDay, eventEndtimeHour, eventEndtimeMinute));
        event.setDescription(eventDescription);
        event.setLowercaseName(event.getName().toLowerCase());
        event.setImage("https://dgfiugrtherwergt");


        EventsRepository repo = EventsRepository.getInstance();
        repo.searchByName("bla").addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                if(task.getResult() != null) {
                    List<Event> list = task.getResult();

                    System.out.println(list);
                }
            }
        });
    }
}
