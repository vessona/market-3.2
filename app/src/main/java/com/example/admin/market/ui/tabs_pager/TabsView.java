package com.example.admin.market.ui.tabs_pager;

import android.content.Context;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import com.example.admin.market.R;
import java.util.ArrayList;
import java.util.List;

public class TabsView extends FrameLayout  {

    private final ViewPager viewPager;

    public TabsView(final Context context) {
        super(context);
        View.inflate(getContext(), R.layout.view_tabs, this);
        final TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.pager);
        tabLayout.setupWithViewPager(viewPager, true);
    }


    public void show(FragmentManager fragmentManager,
                     List<Pair<Fragment, String>> namedFragments) {
        final List<Fragment> fragments = new ArrayList<>(namedFragments.size());
        final List<String> names = new ArrayList<>(namedFragments.size());
        for (final Pair<Fragment, String> item : namedFragments) {
            fragments.add(item.first);
            names.add(item.second);
        }
        viewPager.setAdapter(
                new TabsAdapter(fragmentManager, fragments, names)
        );
    }
}
