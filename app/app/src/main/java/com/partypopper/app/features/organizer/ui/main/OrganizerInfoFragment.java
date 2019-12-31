package com.partypopper.app.features.organizer.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import lombok.Setter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.GeoPoint;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.organizer.OrganizerActivity;
import com.partypopper.app.utils.BaseActivity;
import com.squareup.picasso.Picasso;


public class OrganizerInfoFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private boolean isOrganizerFavored;
    private MaterialButton organizerFavBt, organizerRateBt, organizerBlockBt;
    private RatingBar organizerRb;
    private String organizerId, organizerAddress, organizerDescription, organizerName, organizerPhone, organizerWebsite;
    private double organizerCoordsLat, organizerCoordsLng;
    private float organizerRating;
    private TextView addressTv, descriptionTv, nameTv, phoneTv, websiteTv;
    private SupportMapFragment organizerLocationMf;
    private LatLng coords;

    public OrganizerInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrganizerInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizerInfoFragment newInstance(Bundle organizerBundle) {
        OrganizerInfoFragment fragment = new OrganizerInfoFragment();
        fragment.setArguments(organizerBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organizerId = getArguments().getString("organizerId");
            organizerAddress = getArguments().getString("organizerAddress");
            organizerDescription = getArguments().getString("organizerDescription");
            organizerName = getArguments().getString("organizerName");
            organizerPhone = getArguments().getString("organizerPhone");
            organizerWebsite = getArguments().getString("organizerWebsite");
            organizerCoordsLat = getArguments().getDouble("organizerCoordsLat");
            organizerCoordsLng = getArguments().getDouble("organizerCoordsLng");
            organizerRating = getArguments().getFloat("organizerRating");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_organizer_info, container, false);

        organizerFavBt = v.findViewById(R.id.oOrganizerFavBt);
        organizerFavBt.setOnClickListener(this);
        organizerRateBt = v.findViewById(R.id.oOrganizerRateBt);
        organizerRateBt.setOnClickListener(this);
        organizerRb = v.findViewById(R.id.oOrganizerRb);
        organizerBlockBt = v.findViewById(R.id.coBlockOrganizerBt);
        organizerBlockBt.setOnClickListener(this);

        isOrganizerFavored = false;

        // get data from intent and set them to the views
        addressTv = v.findViewById(R.id.coOrganizerAddressTv);
        addressTv.setText(organizerAddress);
        addressTv.setOnClickListener(this);

        descriptionTv = v.findViewById(R.id.oOrganizerDescriptionTv);
        descriptionTv.setText(organizerDescription);

        nameTv = v.findViewById(R.id.oOrganizerNameTv);
        nameTv.setText(organizerName);

        phoneTv = v.findViewById(R.id.coOrganizerPhoneTv);
        phoneTv.setText(organizerPhone);

        websiteTv = v.findViewById(R.id.coOrganizerLinkTv);
        websiteTv.setText(organizerWebsite);

        coords = new LatLng(organizerCoordsLat, organizerCoordsLng);

        organizerRb = v.findViewById(R.id.oOrganizerRb);
        organizerRb.setRating(organizerRating);


        // map
        // get MapFragment
        organizerLocationMf = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.coOrganizerLocationMf);
        organizerLocationMf.getMapAsync(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oOrganizerFavBt:
                onOrganizerFavButtonClick(v);
                break;
            case R.id.oOrganizerRateBt:
                onOrganizerRateClick(v);
                break;
            case R.id.coOrganizerAddressTv:
                onOrganizerAddressTextViewClick(v);
                break;
            case R.id.coBlockOrganizerBt:
                onBlockOrganizerButtonClick(v);
                break;
        }
    }

    public void onOrganizerFavButtonClick(View view) {

        if (!isOrganizerFavored) {
            organizerFavBt.setIconResource(R.drawable.ic_favorite_white_trans30_24dp);
            organizerFavBt.setText(R.string.unfav_organizer);

            ((OrganizerActivity)getActivity()).showText("Fav");
        } else {
            organizerFavBt.setIconResource(R.drawable.ic_favorite_border_white_trans30_24dp);
            organizerFavBt.setText(R.string.fav_organizer);

            ((OrganizerActivity)getActivity()).showText("Unfav");
        }

        isOrganizerFavored = !isOrganizerFavored;
    }

    public void onOrganizerRateClick(View view) {
        OrganizerRateDialog organizerRateDialog = new OrganizerRateDialog();
        organizerRateDialog.show(getFragmentManager(), getString(R.string.organizer_rate_dialog_tag));
    }

    public void onOrganizerAddressTextViewClick(View view) {
        ((OrganizerActivity)getActivity()).showText(getString(R.string.copied_address));

        TextView textView = (TextView) view;
        BaseActivity.copyTextToClipboard("Address", textView.getText(), view.getContext());
    }

    public void onBlockOrganizerButtonClick(View view) {
        ((OrganizerActivity)getActivity()).showText("Block");
    }

    /**
     * When the map is loaded set a marker and move and zoom its camera to the marker
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker
        googleMap.addMarker(new MarkerOptions().position(coords)
                .title(organizerName));

        // move the map's camera to the same location and zoom
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(15.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
