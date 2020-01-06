package com.partypopper.app.features.organizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.authentication.AuthenticationActivity;
import com.partypopper.app.features.dashboard.DashboardActivity;
import com.partypopper.app.utils.BaseActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BusinessActivity extends BaseActivity implements OnMapReadyCallback {
    // File Upload attributes
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private EditText businessNameWdg;
    private EditText businessWebsiteWdg;
    private EditText businessPhoneWdg;
    private EditText businessAddressWdg;
    private EditText businessDescriptionWdg;
    private EditText businessEmailWdg;

    private String businessAddress = "";
    private ImageView imageView;
    private Button btnUpload;
    private MapFragment organizerLocationMf;
    private GoogleMap map;
    private LatLng addressPoint;

    private final FirebaseUser currentUser = mAuth.getCurrentUser();

    private String firestoreImagePath;
    private String firestoreImageRefPath;

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

        // map
        // get MapFragment
        organizerLocationMf = (MapFragment) getFragmentManager().findFragmentById(R.id.bsMapMv);
        organizerLocationMf.getMapAsync(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getInstance().getReference();

        btnUpload = findViewById(R.id.bsUpdateBtn);
        imageView = findViewById(R.id.bsLogoImg);

        businessNameWdg = findViewById(R.id.bsBusinessnamePt);
        businessWebsiteWdg = findViewById(R.id.bsBusinesswebsitePt);
        businessPhoneWdg = findViewById(R.id.bsBusinessphoneP);
        businessAddressWdg = findViewById(R.id.bsBusinessaddressPa);
        businessDescriptionWdg = findViewById(R.id.bsDescriptionMt);
        businessEmailWdg = findViewById(R.id.bsBusinessmailE);

        businessAddressWdg.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if(!s.equals("") ) {
                    //do your work here
                }
            }



            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {
                businessAddress = businessAddressWdg.getText().toString();
                reloadMap();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onRequestClick(View btn){
        if(filePath != null && !businessAddressWdg.getText().toString().isEmpty()
                && !businessDescriptionWdg.getText().toString().isEmpty()
                && !businessEmailWdg.getText().toString().isEmpty()
                && !businessNameWdg.getText().toString().isEmpty()
                && !businessPhoneWdg.getText().toString().isEmpty()
                && !businessWebsiteWdg.getText().toString().isEmpty()) {
            if (filePath != null) {
                final ProgressDialog progressDialog
                        = new ProgressDialog(this);
                progressDialog.setTitle("Uploading image...");
                progressDialog.show();

                final StorageReference ref = storageReference.child("events/"+ UUID.randomUUID().toString() + ".jpg");
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                showText("Image uploaded.");
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
                                showText("Image upload failed! Please try again later.");
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
        String businessWebsite = businessWebsiteWdg.getText().toString();
        String businessPhone = businessPhoneWdg.getText().toString();
        String businessName = businessNameWdg.getText().toString();
        String businessEmail = businessEmailWdg.getText().toString();
        String businessDescription = businessDescriptionWdg.getText().toString();
        String businessAddress = businessAddressWdg.getText().toString();

        OrganizerRepository repo = OrganizerRepository.getInstance();
        Organizer organizer = new Organizer();
        organizer.setWebsite(businessWebsite);
        organizer.setPhone(businessPhone);
        organizer.setName(businessName);
        organizer.setEmail(businessEmail);
        organizer.setDescription(businessDescription);
        organizer.setAddress(businessAddress);
        repo.singUpOrganizer(organizer).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                // Noch nee Nachricht und Loader undso
                Toast.makeText(getApplicationContext(), "Your account was successfully upgraded. Please log in again.", Toast.LENGTH_SHORT);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(BusinessActivity.this, AuthenticationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showText("Failure! Please try again later.");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * returns a Geopoint with latitude and longtitude coordinates, which matches the given address
     * @param strAddress passed Address which shall be represented as latitude and longtitude
     * @return GeoPoint
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            } else {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    /**
     * When the map is loaded set a marker and move and zoom its camera to the marker
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker
        LatLng coords;
        if(businessAddress.isEmpty() || getLocationFromAddress(this, businessAddress) == null) {
            coords = new LatLng(52.131, 8.666);
        } else {
            coords = getLocationFromAddress(this, businessAddress);
        }

        googleMap.addMarker(new MarkerOptions().position(coords)
                .title("Your location"));

        // move the map's camera to the same location and zoom
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(15.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        map = googleMap;
    }

    /**
     * reloads Google Map on call
     */
    public void reloadMap() {
        MapFragment organizerLocationMfReload = (MapFragment) getFragmentManager().findFragmentById(R.id.bsMapMv);
        organizerLocationMfReload.getMapAsync(this);
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
                imageView.setImageBitmap(bitmap);
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
