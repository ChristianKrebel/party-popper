package com.partypopper.app.features.organizer.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import lombok.Getter;

import com.partypopper.app.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.organizer_tab_text_1, R.string.organizer_tab_text_2};
    private final Context mContext;
    private final Bundle organizerBundle;

    public SectionsPagerAdapter(Context context, FragmentManager fm, Bundle organizerBundle) {
        super(fm);
        mContext = context;
        this.organizerBundle = organizerBundle;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = OrganizerInfoFragment.newInstance(organizerBundle);
                break;
            case 1:
                fragment = OrganizerEventsFragment.newInstance();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}