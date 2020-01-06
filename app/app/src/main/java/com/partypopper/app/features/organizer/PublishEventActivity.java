package com.partypopper.app.features.organizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.EventsRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.utils.BaseActivity;
import com.partypopper.app.utils.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class PublishEventActivity extends BaseActivity {
    // File Upload attributes
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private EditText publisheventTitleWdg;
    private EditText publisheventReflinkWdg;
    private EditText publisheventStartdateWdg;
    private EditText publisheventEnddateWdg;
    private TimePicker publisheventStarttimeWdg;
    private TimePicker publisheventEndtimeWdg;
    private EditText publisheventDescriptionWdg;
    private Button updateButtonWdg;
    private Button publisheventButtonWdg;
    private ImageView publisheventImageWdg;

    private final FirebaseUser currentUser = mAuth.getCurrentUser();

    private String firestoreImagePath;
    private String firestoreImageRefPath;

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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getInstance().getReference();

        DatePicker dp1 = new DatePicker(this, R.id.pubStartdateD);
        DatePicker dp2 = new DatePicker(this, R.id.pubEnddateD);

        publisheventTitleWdg = findViewById(R.id.pubTitlePt);
        publisheventReflinkWdg = findViewById(R.id.pubReflinkPt);
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
        if(filePath != null && !publisheventTitleWdg.getText().toString().isEmpty()
                && !publisheventStartdateWdg.getText().toString().isEmpty()
                && !publisheventEnddateWdg.getText().toString().isEmpty()
                && !publisheventDescriptionWdg.getText().toString().isEmpty()) {
            if (filePath != null) {
                final ProgressDialog progressDialog
                        = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                final StorageReference ref = storageReference.child("events/"+ UUID.randomUUID().toString() + ".jpg");
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                showText("Uploaded");
                                setFirestoreImageRefPath(ref.getPath().substring(1));

                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String rawFirestoreImagePath = "https://firebasestorage.googleapis.com" + uri.getPath() + "?" + uri.getEncodedQuery();
                                        firestoreImagePath = rawFirestoreImagePath.replace("events/", "events%2F");
                                        setFirebaseData();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        showText("Couldn't get Image URL with " + getFirestoreImageRefPath());
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                showText("Upload failed");
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded "+(int)progress+"%");
                            }
                        });
            }
        } else {
            showText("Please fill every empty fields");
        }
    }

    /**
     * sets up the needed data for events for the firebase
     */
    public void setFirebaseData() {
        String eventTitle = publisheventTitleWdg.getText().toString();
        String eventReflink = publisheventReflinkWdg.getText().toString();
        String eventStartdate = publisheventStartdateWdg.getText().toString();
        String eventEnddate = publisheventEnddateWdg.getText().toString();
        // Angeblich zu niedriges API Level, obwohl offizielle Android Doku API Level 1 ansagt
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

        OrganizerRepository repo = OrganizerRepository.getInstance();
        Event event = new Event();
        event.setName(eventTitle);
        event.setEventUrl(eventReflink);
        event.setOrganizer(currentUser.getUid());
        event.setGoing(0);
        event.setStartDate(new Date(startDateYear, startDateMonth, startDateDay, eventStarttimeHour, eventStarttimeMinute));
        event.setEndDate(new Date(endDateYear, endDateMonth, endDateDay, eventEndtimeHour, eventEndtimeMinute));
        event.setDescription(eventDescription);
        event.setLowercaseName(event.getName().toLowerCase());
        event.setImage(firestoreImagePath);
        event.setLocation(repo.getOrganizerById(currentUser.getUid()).getResult().getLocation());
        repo.createEvent(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showText("Event created");
            }
        });
    }

    /**
     * Defining Implicit Intent to mobile gallery
     */
    public void chooseImage(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Override onActivityResult method to check request code and result code and to set image into the image view
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                publisheventImageWdg.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFirestoreImageRefPath() {
        return firestoreImageRefPath;
    }

    public void setFirestoreImageRefPath(String firestoreImageRefPath) {
        this.firestoreImageRefPath = firestoreImageRefPath;
    }
}
