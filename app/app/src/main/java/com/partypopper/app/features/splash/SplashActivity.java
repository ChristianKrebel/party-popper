package com.partypopper.app.features.splash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.features.organizer.BusinessActivity;
import com.partypopper.app.utils.BaseActivity;

import java.util.Date;


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
                        System.out.println("Is Organizer");

                        FollowRepository repo = FollowRepository.getInstance();

                        repo.rateOrganizer("De9bYKy9v2cfzlDht0LKCNHLzYE3", 5).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                            @Override
                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                System.out.println("COMPLETE");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println(e.getMessage());
                            }
                        });

                        /*Event event = new Event();
                        event.setName("Bla bla bla");
                        event.setOrganizer(currentUser.getUid());
                        event.setGoing(0);
                        event.setStartDate(new Date(2019, 12, 31));
                        event.setEndDate(new Date(2020, 1, 1));
                        event.setDescription("ASFBjhfsdaghwhzeugw fesdahfewe");
                        event.setLowercaseName(event.getName().toLowerCase());
                        event.setImage("https://dgfiugrtherwergt");

                        OrganizerRepository repo = OrganizerRepository.getInstance();

                        repo.createEvent(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                System.out.println("EVENT created");
                            }
                        });*/

                        /*EventsRepository repo = EventsRepository.getInstance();
                        repo.searchByName("bla").addOnCompleteListener(new OnCompleteListener<List<Event>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Event>> task) {
                                if(task.getResult() != null) {
                                    List<Event> list = task.getResult();

                                    System.out.println(list);
                                }
                            }
                        });*/

                        showOrganizerUI();
                    } else {
                        showUserUI();
                    }
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