package com.partypopper.app.features.splash;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.features.dashboard.EventDetailActivity;
import com.partypopper.app.features.organizer.OrganizerActivity;
import com.partypopper.app.utils.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {
            showLoginUI();
        } else {
            currentUser.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    if(result.getClaims().get("organizer") != null) {
                        showOrganizerUI();
                    } else {
                        showUserUI();
                    }
                }
            });
        }
    }

    private void showOrganizerUI() {
        System.out.println("Is Organizer");
    }

    private void showUserUI() {
        Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLoginUI() {
        Intent intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}