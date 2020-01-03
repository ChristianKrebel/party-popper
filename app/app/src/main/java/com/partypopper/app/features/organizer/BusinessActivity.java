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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BusinessActivity extends BaseActivity implements OnMapReadyCallback {
    private String businessAddress = "";
    private ImageView imageView;
    private Button btnUpload;
    private EditText businessAddressWdg;
    private MapFragment organizerLocationMf;
    private GoogleMap map;
    private LatLng addressPoint;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    private Uri filePath;

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

        btnUpload = findViewById(R.id.bsUpdateBtn);
        imageView = findViewById(R.id.bsLogoImg);

        EditText businessNameWdg = findViewById(R.id.bsBusinessnamePt);
        String businessName = businessNameWdg.getText().toString();
        EditText businessWebsiteWdg = findViewById(R.id.bsBusinesswebsitePt);
        String businessWebsite = businessWebsiteWdg.getText().toString();
        EditText businessPhoneWdg = findViewById(R.id.bsBusinessphoneP);
        String businessTelephone = businessPhoneWdg.getText().toString();
        businessAddressWdg = findViewById(R.id.bsBusinessaddressPa);
        EditText businessDescriptionWdg = findViewById(R.id.bsDescriptionMt);
        String businessDescription = businessDescriptionWdg.getText().toString();
        EditText businessEmailWdg = findViewById(R.id.bsBusinessmailE);
        String businessEmail = businessEmailWdg.getText().toString();

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
        addressPoint = getLocationFromAddress(this, businessAddress);

        showText("Latitude: " + addressPoint.latitude + ", Longtitude: " + addressPoint.longitude);
        // TODO sending post request?
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
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    /**
     * Override onActivityResult method to check request code and result code and to set image into the image view
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to upload the chosen image to Firestore
     */
    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            //TODO implement Firestore Connection
        }
    }

    public LatLng getAddressPoint() {
        return addressPoint;
    }
}
