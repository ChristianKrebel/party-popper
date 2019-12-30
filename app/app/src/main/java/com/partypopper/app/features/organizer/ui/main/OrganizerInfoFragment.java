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
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.GeoPoint;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Organizer;
import com.partypopper.app.database.repository.OrganizerRepository;
import com.partypopper.app.utils.BaseActivity;
import com.squareup.picasso.Picasso;


public class OrganizerInfoFragment extends Fragment implements View.OnClickListener {

    private boolean isOrganizerFavored;
    private MaterialButton organizerFavBt, organizerRateBt, organizerBlockBt;
    private RatingBar organizerRb;
    private static final String ARG_ORGANIZER_ID = "organizer_id";
    @Setter
    private String organizerId;

    private TextView mOrganizerTv, organizerWebsiteTv, organizerPhoneTv, organizerAddressTv, organizerDescriptionTv;
    private ImageView organizerIv;
    private MapFragment organizerLocationMf;
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
    public static OrganizerInfoFragment newInstance(String organizerId) {
        OrganizerInfoFragment fragment = new OrganizerInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ORGANIZER_ID, organizerId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organizerId = getArguments().getString(ARG_ORGANIZER_ID);
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

        showData();

        return v;
    }

    public void showData() {
        Toast.makeText(getContext(), organizerId, Toast.LENGTH_SHORT).show();

        OrganizerRepository organizerRepository = OrganizerRepository.getInstance();
        organizerRepository.getOrganizerById(organizerId).addOnCompleteListener(new OnCompleteListener<Organizer>() {
            @Override
            public void onComplete(@NonNull Task<Organizer> task) {
                if (task.isSuccessful()) {
                    Organizer organizer = task.getResult();

                    mOrganizerTv = getActivity().findViewById(R.id.oOrganizerNameTv);
                    mOrganizerTv.setText(organizer.getName());

                    organizerRb.setRating(organizer.getRating());

                    organizerIv = getActivity().findViewById(R.id.oBannerIv);
                    Picasso.get().load(organizer.getImage()).into(organizerIv);

                    organizerDescriptionTv = getActivity().findViewById(R.id.oOrganizerDescriptionTv);
                    organizerDescriptionTv.setText(organizer.getDescription());

                    organizerWebsiteTv = getActivity().findViewById(R.id.coOrganizerLinkTv);
                    organizerWebsiteTv.setText(organizer.getWebsite());

                    organizerPhoneTv = getActivity().findViewById(R.id.coOrganizerPhoneTv);
                    organizerPhoneTv.setText(organizer.getPhone());

                    organizerAddressTv = getActivity().findViewById(R.id.coOrganizerAddressTv);
                    organizerAddressTv.setText(organizer.getAdress());

                    /*GeoPoint geoPoint = organizer.getCoordinates();
                    coords = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    organizerLocationMf.getMapAsync(onMapReadyCallback);*/
                }
            }
        });
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

            Toast.makeText(view.getContext(), "Fav", Toast.LENGTH_SHORT).show();
        } else {
            organizerFavBt.setIconResource(R.drawable.ic_favorite_border_white_trans30_24dp);
            organizerFavBt.setText(R.string.fav_organizer);

            Toast.makeText(view.getContext(), "Unfav", Toast.LENGTH_SHORT).show();
        }

        isOrganizerFavored = !isOrganizerFavored;
    }

    public void onOrganizerRateClick(View view) {
        OrganizerRateDialog organizerRateDialog = new OrganizerRateDialog();
        organizerRateDialog.show(getFragmentManager(), getString(R.string.organizer_rate_dialog_tag));
    }

    public void onOrganizerAddressTextViewClick(View view) {
        Toast.makeText(view.getContext(), "Address", Toast.LENGTH_SHORT).show();
    }

    public void onBlockOrganizerButtonClick(View view) {
        Toast.makeText(view.getContext(), "Block", Toast.LENGTH_SHORT).show();
    }


}
