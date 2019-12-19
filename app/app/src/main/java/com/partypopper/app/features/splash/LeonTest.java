package com.partypopper.app.features.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.utils.BaseActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeonTest extends BaseActivity {

    private final static String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //test();

        if(currentUser == null) {
            showLoginUI();
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "Go Parc Herford");
            data.put("push", true);

            //mFunctions.getHttpsCallable("signUpOrganizer").call(data);

            currentUser.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    if(result.getClaims().get("organizer") != null) {
                        //showOrganizerUI();
                        mAuth.signOut();
                    } else {
                        testFollow();
                        mFunctions.getHttpsCallable("signUpOrganizer").call(new HashMap<>());
                    }
                }
            });
        }
    }

    private void showOrganizerUI() {
        System.out.println("Is Organizer");
    }

    private void showUserUI() {
        Intent intent = new Intent(LeonTest.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLoginUI() {
        Intent intent = new Intent(LeonTest.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void testFollow() {
        final FollowRepository repo = FollowRepository.getInstance();

        repo.followOrganizer("Beispiel").addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                repo.getFollowing().addOnCompleteListener(new OnCompleteListener<List<String>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<String>> task) {
                        System.out.println("FOLLOWING " + task.getResult());
                    }
                });
            }
        });

        /*repo.followOrganizer("cde").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                repo.getFollowing().addOnCompleteListener(new OnCompleteListener<List<String>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<String>> task) {
                        System.out.println("FOLLOWING " + task.getResult());
                    }
                });
            }
        });*/
    }

    private void test() {
        final OrganizerRepository repo = OrganizerRepository.getInstance();

        Event event = new Event();
        event.setDescription("Test Desc");
        event.setEndDate(new Date(System.currentTimeMillis() + 100000000));
        event.setStartDate(new Date());
        event.setGoing(0);
        event.setName("Tast Name");
        event.setOrganizer(mAuth.getUid());

        //repo.createEvent(event);

        repo.getNextEvent().addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                Event event = task.getResult().get(0);
                event.setName("Lololol");
                repo.updateEvent(event);
            }
        });


        /*repo.getNextEvent().addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                System.out.println(task.getResult().get(0));
                repo.deleteEvent(task.getResult().get(0).getEntityKey());
            }
        });*/



        /*FirestoreRepository<Event> repo = new FirestoreRepository<>(Event.class, "events");

        System.out.println("TEST");

        repo.getAll().addOnCompleteListener(new OnCompleteListener<List<Event>>() {
            @Override
            public void onComplete(@NonNull Task<List<Event>> task) {
                Event e = task.getResult().get(0);


                System.out.println(e);
                e.getOrg().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        System.out.println(task.getResult());
                    }
                });


            }
        });*/
    }

    private void dbTest() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
