package com.partypopper.app.features.organizer.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.partypopper.app.R;

import androidx.fragment.app.Fragment;


public class OrganizerEventsFragment extends Fragment {

    public OrganizerEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrganizerInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizerEventsFragment newInstance() {
        return new OrganizerEventsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_organizer_events, container, false);
    }

}
