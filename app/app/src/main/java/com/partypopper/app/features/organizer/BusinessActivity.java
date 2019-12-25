package com.partypopper.app.features.organizer;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.partypopper.app.R;

import androidx.appcompat.app.AppCompatActivity;

public class BusinessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Print a toast for testing purposes
     * @param text
     */
    protected void testToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void sendRequest(View btn){
        EditText businessNameWdg = findViewById(R.id.bsBusinessnamePt);
        String businessName = businessNameWdg.getText().toString();
        testToast(businessName);
    }
}
