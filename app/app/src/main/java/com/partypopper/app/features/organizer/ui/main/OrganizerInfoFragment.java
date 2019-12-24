package com.partypopper.app.features.organizer.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.partypopper.app.R;
import com.partypopper.app.utils.BaseActivity;


public class OrganizerInfoFragment extends Fragment implements View.OnClickListener {

    private boolean isOrganizerFavored;
    private MaterialButton organizerFavBt, organizerRateBt, organizerBlockBt;
    private TextView organizerAddressTv;
    private RatingBar organizerRb;

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
    public static OrganizerInfoFragment newInstance() {
        return new OrganizerInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        organizerAddressTv = v.findViewById(R.id.coOrganizerAddressTv);
        organizerAddressTv.setOnClickListener(this);

        isOrganizerFavored = false;

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
