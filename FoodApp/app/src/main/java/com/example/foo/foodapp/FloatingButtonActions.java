package com.example.foo.foodapp;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

/**
 * Class used to implements the possible behaviour for the floating button
 */
public class FloatingButtonActions {


    // hide when scrolling down, show when scrolling up
    public static void hideScrollDownShowScrollUp(final FloatingActionButton fab, RecyclerView rview) {
        rview.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy<0 && !fab.isShown())
                    fab.show();
                else if(dy>0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }


    // hide when scrolling, show when not scrolling
    public static void hideWhenScrolling(final FloatingActionButton fab, RecyclerView rview) {
        rview.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }


    // (sperimental) move the floating button left when scrolling down, and right when scrolling up
    public static void moveWhenScrolling(final FloatingActionButton fab, RecyclerView rView) {
        final float originalX = fab.getX();
        rView.addOnScrollListener(new RecyclerView.OnScrollListener(){

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                fab.setX(originalX);
                if (dy > 0 && fab.getX() >= originalX - 2) {
                    fab.setX(fab.getX() - 1);

                } else if (dy < 0) {
                    fab.setX(originalX);
                }
            }
        });
    }
}
