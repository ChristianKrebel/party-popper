package com.partypopper.app.features.organizer.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.FollowRepository;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.features.organizer.OrganizerActivity;
import com.partypopper.app.features.organizer.ui.dialog.OrganizerRateDialog;
import com.partypopper.app.utils.BaseActivity;

import java.util.List;

import static com.partypopper.app.utils.Constants.MAP_ZOOM;

/**
 * The first Fragment ind the OrganizerActivity.
 * It hosts information about the organizer.
 */
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
    public static OrganizerInfoFragment newInstance(Bundle organizerBundle) {
        OrganizerInfoFragment fragment = new OrganizerInfoFragment();
        fragment.setArguments(organizerBundle);
        return fragment;
    }

    /**
     * Sets all attributes from arguments.
     *
     * @param savedInstanceState
     */
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

    /**
     * Creates the View of the fragment. Similar to onCreate of Activities.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
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

        // Follows organizer?
        onResumeSetOrganizerFavoredState();

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


        // map
        // get MapFragment
        organizerLocationMf = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.coOrganizerLocationMf);
        organizerLocationMf.getMapAsync(this);

        return v;
    }

    /**
     * When this Activity gets resumed (in the view) update some Views.
     */
    @Override
    public void onResume() {
        super.onResume();
        onResumeSetOrganizerFavoredState();
        onResumeSetOrganizerRating();
    }

    /**
     * OnClickListener.
     *
     * @param v
     */
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

    private void onOrganizerFavButtonClick(View view) {
        FollowRepository followRepository = FollowRepository.getInstance();
        if (isOrganizerFavored) {
            followRepository.unfollowOrganizer(organizerId);
        } else {
            followRepository.followOrganizer(organizerId);
        }
        isOrganizerFavored = !isOrganizerFavored;
        changeOrganizerFavButtonUIstate(isOrganizerFavored);
    }

    private void changeOrganizerFavButtonUIstate(boolean favor) {
        if (favor) {
            organizerFavBt.setIconResource(R.drawable.ic_favorite_white_trans30_24dp);
            organizerFavBt.setText(R.string.unfav_organizer);
        } else {
            organizerFavBt.setIconResource(R.drawable.ic_favorite_border_white_trans30_24dp);
            organizerFavBt.setText(R.string.fav_organizer);
        }
    }

    /**
     * Called when the organizer gets rated.
     * It queries a new AVG rating and updates the UI.
     */
    public void onResumeSetOrganizerRating() {
        OrganizerRepository organizerRepository = OrganizerRepository.getInstance();
        organizerRepository.getOrganizerById(organizerId).addOnCompleteListener(new OnCompleteListener<Organizer>() {
            @Override
            public void onComplete(@NonNull Task<Organizer> task) {
                if (task.isSuccessful()) {
                    organizerRating = task.getResult().getAvgRating();
                    changeOrganizerRatingUIstate(organizerRating);
                }
            }
        });
    }

    private void changeOrganizerRatingUIstate(float rating) {
        organizerRb.setRating(rating);
    }

    private void onResumeSetOrganizerFavoredState() {
        // Follows organizer?
        isOrganizerFavored = false;
        changeOrganizerFavButtonUIstate(isOrganizerFavored);
        FollowRepository.getInstance().getFollowing().addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().contains(organizerId)) {
                        isOrganizerFavored = true;
                        changeOrganizerFavButtonUIstate(isOrganizerFavored);
                    }
                }
            }
        });
    }

    private void onOrganizerRateClick(View view) {
        OrganizerRateDialog organizerRateDialog = new OrganizerRateDialog();
        organizerRateDialog.show(getFragmentManager(), getString(R.string.organizer_rate_dialog_tag));
    }

    private void onOrganizerAddressTextViewClick(View view) {
        ((OrganizerActivity)getActivity()).showText(getString(R.string.copied_address));

        TextView textView = (TextView) view;
        BaseActivity.copyTextToClipboard("Address", textView.getText(), view.getContext());
    }

    private void onBlockOrganizerButtonClick(View view) {
        ((OrganizerActivity)getActivity()).showText(getString(R.string.organizer_blocked));
        FollowRepository followRepository = FollowRepository.getInstance();
        followRepository.blockOrganizer(organizerId);
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
        CameraPosition cameraPosition = new CameraPosition.Builder().target(coords).zoom(MAP_ZOOM).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

        // Set zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
