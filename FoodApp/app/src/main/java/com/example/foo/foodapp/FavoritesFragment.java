package com.example.foo.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class representing the favorite fragment present in the homonymous tab
 */
public class FavoritesFragment extends Fragment implements AppFragment {
    private FloatingActionButton _fab;
    private RecyclerView _rView;
    private FoodsFragment _foodFragment;


    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _rView = getView().findViewById(R.id.myRecyclerView);

        _rView.setItemAnimator(new DefaultItemAnimator());
        _rView.addItemDecoration(new DividerItemDecoration(getContext()));

        FavoriteRecyclerViewAdapter favoriteRView = new FavoriteRecyclerViewAdapter(getContext(),
                getActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE));
        _rView.setAdapter(favoriteRView);
        _rView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteRView.linkRView(_rView);
        FloatingButtonActions.hideScrollDownShowScrollUp(_fab, _rView);
        favoriteRView.linkFoodRView(_foodFragment.getFoodRView());
        favoriteRView.linkActivity(getActivity());
        favoriteRView.linkFragment(this);
    }


    /**
     * Used to refresh the fragment every time its tab is selected
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) { onActivityCreated(new Bundle()); }
    }


    @Override
    public void refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }


    public void linkFoodFragment(FoodsFragment foodsFragment) {
        _foodFragment = foodsFragment;
    }


    @Override
    public DeactivableViewPager getMainActivityViewPager() {
        return ((MainActivity) getActivity())._viewPager;
    }


    @Override
    public void manageFloatingButton(FloatingActionButton fab) {
        _fab = fab;
    }


    @Override
    public RecyclerView getRView() {
        return _rView;
    }


}

