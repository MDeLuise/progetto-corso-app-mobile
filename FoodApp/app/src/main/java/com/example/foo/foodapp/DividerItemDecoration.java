package com.example.foo.foodapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Class used in the RecyclerView to draw the divider between elements
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable _mDivider;


    DividerItemDecoration(Context context) {
        _mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + _mDivider.getIntrinsicHeight();

            _mDivider.setBounds(left, top, right, bottom);
            _mDivider.draw(c);
        }
    }


}
