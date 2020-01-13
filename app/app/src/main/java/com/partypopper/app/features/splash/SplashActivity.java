package com.partypopper.app.features.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.partypopper.app.database.model.BlockedOrganizer;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.repository.BlockedRepository;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.service.MessagingService;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.utils.EventHelper;

import java.util.List;


public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {
            showLoginUI();
        } else {
            currentUser.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    if(result.getClaims().get("organizer") != null) {
                        showOrganizerUI();
                    }
                    Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mAuth.signOut();
                    showLoginUI();
                }
            });
        }
    }

    private void showOrganizerUI() {
        System.out.println("Is Organizer");
        DashboardActivity.setOrganizer(true);
    }

    private void showLoginUI() {
        Intent intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}