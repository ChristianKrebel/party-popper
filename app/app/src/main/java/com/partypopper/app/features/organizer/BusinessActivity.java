package com.partypopper.app.features.organizer;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BusinessActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        Toolbar toolbar = findViewById(R.id.bsToolbar);
        setSupportActionBar(toolbar);


        // Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // set back button
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void sendRequest(View btn){
        EditText businessNameWdg = findViewById(R.id.bsBusinessnamePt);
        String businessName = businessNameWdg.getText().toString();
        EditText businessWebsiteWdg = findViewById(R.id.bsBusinesswebsitePt);
        String businessWebsite = businessWebsiteWdg.getText().toString();
        EditText businessPhoneWdg = findViewById(R.id.bsBusinessphoneP);
        String businessTelephone = businessPhoneWdg.getText().toString();
        EditText businessAddressWdg = findViewById(R.id.bsBusinessaddressPa);
        String businessAddress = businessAddressWdg.getText().toString();
        EditText businessDescriptionWdg = findViewById(R.id.bsDescriptionMt);
        String businessDescription = businessDescriptionWdg.getText().toString();
        EditText businessEmailWdg = findViewById(R.id.bsBusinessmailE);
        String businessEmail = businessEmailWdg.getText().toString();

        // TODO sending post request?
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
