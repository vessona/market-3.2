package com.example.admin.market.ui.tabs_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;


public class TabsAdapter extends FragmentPagerAdapter { //Implementation of PagerAdapter that represents each page as a Fragment that is persistently kept in the fragment manager as long as the user can return to the page.

    private final List<Fragment> fragments;
    private final List<String> titles;

    TabsAdapter(final FragmentManager fm,
                final List<Fragment> fragments,
                final List<String> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(final int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return titles.get(position);
    }
}
