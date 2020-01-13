package com.partypopper.app.features.organizer.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.partypopper.app.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class OrganizerSectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.organizer_tab_text_1, R.string.organizer_tab_text_2};
    private final Context mContext;
    private final Bundle organizerBundle;
    private Fragment one, two;

    /**
     * Constructor.
     * The Bundle is used for arguments.
     *
     * @param context
     * @param fm
     * @param organizerBundle used for arguments
     */
    public OrganizerSectionsPagerAdapter(Context context, FragmentManager fm, Bundle organizerBundle) {
        super(fm);
        mContext = context;
        this.organizerBundle = organizerBundle;
    }

    /**
     * Returns the current fragment and initializes it if it was not already.
     *
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (one == null) {
                    one = OrganizerInfoFragment.newInstance(organizerBundle);
                }
                return one;
            case 1:
                if (two == null) {
                    two = OrganizerEventsFragment.newInstance(organizerBundle.getString("organizerId"));
                }
                return two;
        }
        return null;
    }

    /**
     * Returns the title of the tab.
     *
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    /**
     * Returns the total count of tabs.
     *
     * @return
     */
    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}