package com.example.foo.foodapp;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

/**
 * Interface which should be implemented in the app fragments, it's not explicitly used in the
 * MainActivity but it's a useful remainder of which function(s) should be exist in the API
 */
public interface AppFragment {

    void manageFloatingButton(FloatingActionButton fab);
    RecyclerView getRView();
    void refresh();
    DeactivableViewPager getMainActivityViewPager();

}
