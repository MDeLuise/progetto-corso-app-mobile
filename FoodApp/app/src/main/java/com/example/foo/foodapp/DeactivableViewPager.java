package com.example.foo.foodapp;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom ViewPager implementing the following behaviour:
 * 1) when the user select multiple element of a list (summary or favorite), then the
 *    swiping between tabs are deactivated
 * 2) when the use has selected no element, then the swiping between tabs are activated
 */
public class DeactivableViewPager extends ViewPager {

    private boolean _enabled;


    public DeactivableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        _enabled = true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (_enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (_enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }


    public void setPagingEnabled(boolean enabled) {
        _enabled = enabled;
    }

}