package com.partypopper.app.features.organizer;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.List;

public class BusinessActivity extends BaseActivity implements OnMapReadyCallback {
    private String businessAddress = "";
    private EditText businessAddressWdg;
    private MapFragment organizerLocationMf;
    private GoogleMap map;
    private LatLng addressPoint;

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

    public LatLng getAddressPoint() {
        return addressPoint;
    }

    public void reloadMap() {
        MapFragment organizerLocationMfReload = (MapFragment) getFragmentManager().findFragmentById(R.id.bsMapMv);
        organizerLocationMfReload.getMapAsync(this);
    }

}
