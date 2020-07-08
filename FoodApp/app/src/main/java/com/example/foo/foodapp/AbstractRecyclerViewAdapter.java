package com.example.foo.foodapp;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foo.foodapp.database.AppDatabase;
import com.example.foo.foodapp.database.Category;
import com.example.foo.foodapp.database.FoodDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Abstract class used in the RecyclerView to draw the single "card elements"
 */
public abstract class AbstractRecyclerViewAdapter extends
        RecyclerView.Adapter<AbstractRecyclerViewAdapter.ViewHolder> {

    Context _context;
    FoodDAO _db;
    int[] _entriesId;
    String[] _entriesName;
    int[] _entriesSupply;
    Date[] _entriesExpirationDate;
    double[] _entriesPrice;
    Category[] _entriesCategory;
    int _totalVisibleElement;
    SharedPreferences _preferences;
    List<ViewHolder> _holders;
    SortedSet<Integer> _selectedPos;
    private FragmentActivity _activity;
    private RecyclerView _rView;
    boolean _isSelectionModeActivated;
    private AppFragment _fragment;


    AbstractRecyclerViewAdapter(Context contexts, SharedPreferences preferences) {
        _context = contexts;
        _preferences = preferences;
        _holders = new ArrayList<>();
        _selectedPos = new TreeSet<>();
        _isSelectionModeActivated = false;
        linkDB();
        initializeEntries();
    }


    /**
     * Link the fragment holding this object to this
     * @param frag fragment to link
     */
    void linkFragment(AppFragment frag) {
        _fragment = frag;
    }


    /**
     * Refresh the fragment when no other operations are in pending
     */
    void refresh() {
        Handler uiHandler = new Handler();
        uiHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                _fragment.refresh();
            }
        });
    }


    /**
     * Given a category, return the resources ID related to the category image
     * @param category Category of which we want to have the image
     * @return image resource
     */
    int getCategoryImgId(Category category) {
        int categoryImgId = R.drawable.food;
        switch (category) {
            case CASEARI:
                categoryImgId = R.drawable.dairy;
                break;
            case CARNE:
                categoryImgId = R.drawable.meat;
                break;
            case PESCE:
                categoryImgId = R.drawable.fish;
                break;
            case BEVANDA:
                categoryImgId = R.drawable.drink;
                break;
            case DOLCI:
                categoryImgId = R.drawable.dessert;
                break;
            case FRUTTA:
                categoryImgId = R.drawable.fruit;
                break;
            case VERDURA:
                categoryImgId = R.drawable.vegetable;
                break;
            case PANETTERIA:
                categoryImgId = R.drawable.bakery;
                break;
        }
        return categoryImgId;
    }


    /**
     * Load elements to be displayed in the view
     */
    protected abstract void initializeEntries();


    /**
     * Connect the activity which hold this RecycleViewAdapter
     * @param activity FragmentActivity which hold this RecycleViewAdapter
     */
    void linkActivity(FragmentActivity activity) {
        _activity = activity;
    }


    /**
     * Link the database to this class
     */
    private void linkDB() {
        AppDatabase database = Room.databaseBuilder(_context, AppDatabase.class,
                _context.getString(R.string.database_name))
                .allowMainThreadQueries()
                .build();

        _db = database.getFoodDAO();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_row, parent, false);
        return new AbstractRecyclerViewAdapter.ViewHolder(itemView);
    }


    @Override
    public int getItemCount() {
        return _totalVisibleElement;
    }


    /**
     * Give the current currency symbol used
     * @return currency symbol
     */
    String getCurrencySymbol() {
        return _preferences.getString("currency","€");
    }


    /**
     * Insert the given holder in the list of holder, in the correct position
     * @param newHolder holder to insert
     */
    void insertHolderAtItsPos(ViewHolder newHolder) {
        List<ViewHolder> toRemove = new ArrayList<>();
        for (ViewHolder holders: _holders) {
            if (holders.getAdapterPosition() == newHolder.getAdapterPosition()) {
                toRemove.add(holders);
            }
        }

        for (ViewHolder holders: toRemove) {
            _holders.remove(holders);
        }

        _holders.add(newHolder);
    }


    /**
     * Deselect all "card" in the recycle view
     */
    private void deselectAll() {
        _fragment.getMainActivityViewPager().setPagingEnabled(true);
        for (ViewHolder holder: _holders) {
            holder._view.setBackgroundColor(Color.WHITE);
            holder._view.findViewById(R.id.buttonsLayout).setBackgroundColor(Color.WHITE);
            holder._view.findViewById(R.id.buttonsLayout).setVisibility(View.VISIBLE);
        }

        // change back the toolbar text
        ((Toolbar) _activity.findViewById(R.id.toolbar)).setTitle(R.string.app_name);

        // if status bar was modified change it back
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            _activity.getWindow().addFlags(0);
            _activity.getWindow().setStatusBarColor(
                    _activity.getResources().getColor(R.color.colorPrimaryDark));
        }

        // change back the behaviour of appbar
        AppBarLayout.LayoutParams appBar = (AppBarLayout.LayoutParams) _activity.findViewById(
                R.id.toolbar).getLayoutParams();
        appBar.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        // change back the color of the toolbar
        _activity.findViewById(R.id.toolbar).setBackgroundColor(
                _activity.getResources().getColor(R.color.colorPrimary));

        // make visible the toolbar buttons
        _activity.findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
        _activity.findViewById(R.id.sortButton).setVisibility(View.VISIBLE);

        // hide the "selection toolbar" and show the "normal toolbar"
        _activity.findViewById(R.id.tabs).setVisibility(View.VISIBLE);
        _activity.findViewById(R.id.selectionTabs).setVisibility(View.GONE);

        // make visible the floating button
        ((FloatingActionButton) (_activity.findViewById(R.id.addFoodFloatingButton))).show();
        FloatingButtonActions.hideScrollDownShowScrollUp(
                (FloatingActionButton) (_activity.findViewById(R.id.addFoodFloatingButton)),
                _rView);

        _isSelectionModeActivated = false;
    }


    /**
     * Given an ViewHolder, create the ClickListener for it
     * @param holder ViewHolder for which to create the ClickListener
     */
    void createListenerForHolder(ViewHolder holder) {

            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(final View view, int position, boolean isLongClick) {
                    _fragment.getMainActivityViewPager().setPagingEnabled(false);

                    if (isLongClick || _selectedPos.size() > 0) {
                        if (_selectedPos.contains(position)) {
                            _selectedPos.remove(position);
                            view.findViewById(R.id.entireCard).setBackgroundColor(
                                    _activity.getResources().getColor(R.color.notSelectedCard));
                            view.findViewById(R.id.buttonsLayout).setBackgroundColor(
                                    _activity.getResources().getColor(R.color.notSelectedCard));
                        } else {
                            _selectedPos.add(position);
                            view.findViewById(R.id.entireCard).setBackgroundColor(
                                    _activity.getResources().getColor(R.color.selectedCard));
                            view.findViewById(R.id.buttonsLayout).setBackgroundColor(
                                    _activity.getResources().getColor(R.color.selectedCard));
                        }
                    }

                    boolean atLeastOneSelected = _selectedPos.size() > 0;
                    for (ViewHolder holder: _holders) {
                        if (atLeastOneSelected) {
                            holder._view.findViewById(R.id.buttonsLayout).setVisibility(View.INVISIBLE);
                        } else {
                            holder._view.findViewById(R.id.buttonsLayout).setVisibility(View.VISIBLE);
                        }
                    }


                    if (atLeastOneSelected) {

                        // modify toolbar
                        Toolbar toolbar = _activity.findViewById(R.id.toolbar);
                        toolbar.setTitle(_context.getString(R.string.selection_toolbar_message)
                                .replace("NUM", "" + _selectedPos.size()));
                        ((AppBarLayout.LayoutParams) toolbar.getLayoutParams()).setScrollFlags(0);
                        toolbar.setBackgroundColor(_activity.getResources().getColor(
                                R.color.colorSecondary));

                        // expand the appbar
                        ((AppBarLayout) _activity.findViewById(R.id.appbar)).setExpanded(true);

                        // hide the appbar buttons
                        _activity.findViewById(R.id.searchButton).setVisibility(View.INVISIBLE);
                        _activity.findViewById(R.id.sortButton).setVisibility(View.INVISIBLE);

                        // hide the "normal" tab bar and show the "tab bar for selection"
                        _activity.findViewById(R.id.tabs).setVisibility(View.GONE);
                        _activity.findViewById(R.id.selectionTabs).setVisibility(View.VISIBLE);
                        _activity.findViewById(R.id.selectionTabs).setBackgroundColor(
                                _activity.getResources().getColor(R.color.colorSecondary));

                        // if is possible, change even the status bar color
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                            _activity.getWindow().addFlags(
                                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            _activity.getWindow().setStatusBarColor(
                                    _activity.getResources().getColor(R.color.colorSecondary));
                        }

                        // create what to do when the user click on tab inside "selection tab"
                        ((TabLayout) _activity.findViewById(R.id.selectionTabs)).clearOnTabSelectedListeners();
                        ((TabLayout) _activity.findViewById(R.id.selectionTabs)).addOnTabSelectedListener(
                                new TabLayout.OnTabSelectedListener() {
                                    @Override
                                    public void onTabSelected(TabLayout.Tab tab) {
                                        switch (tab.getPosition()) {

                                            case 0: // case of "remove selected"
                                                final AlertDialog alertDialog = new AlertDialog.Builder(_activity)
                                                        .setTitle(R.string.attention_string)
                                                        .setMessage(_context.getString(R.string.are_you_sure_to_delete)
                                                                .replace("NUM", "" + _selectedPos.size()))

                                                        .setPositiveButton(_context.getString(R.string.affermative),
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        Toast.makeText(_context,
                                                                                _context.getString(R.string.notify_delete_elements).
                                                                                        replace("NUM",
                                                                                                "" + _selectedPos.size()),
                                                                                Toast.LENGTH_SHORT).show();
                                                                        onConfirmDeleteButtonPressed();
                                                                        deselectAll();
                                                                        _selectedPos = new TreeSet<>();
                                                                    }})

                                                        .setNegativeButton(_context.getString(R.string.negative), null)

                                                        .create();

                                                alertDialog.setCanceledOnTouchOutside(true);
                                                alertDialog.show();
                                                break;

                                                case 1: // case of "undo" selected
                                                _selectedPos = new TreeSet<>();
                                                deselectAll();
                                                break;

                                        }
                                    }

                                    @Override
                                    public void onTabUnselected(TabLayout.Tab tab) {}

                                    @Override
                                    public void onTabReselected(TabLayout.Tab tab) {
                                        onTabSelected(tab);
                                    }
                                });

                        // make the floating button visible
                        _activity.findViewById(R.id.addFoodFloatingButton).setVisibility(View.GONE);
                        _rView.clearOnScrollListeners();


                        _isSelectionModeActivated = true;


                    } else { // if no element is selected
                        deselectAll();
                        FloatingButtonActions.hideScrollDownShowScrollUp(
                                (FloatingActionButton) _activity.findViewById(R.id.addFoodFloatingButton),
                                _rView);
                    }

                }});
    }


    /**
     * Implement what happen when the delete button is pressed
     */
    protected abstract void onConfirmDeleteButtonPressed();


    /**
     * Connect an RecycleView to this class
     * @param rView RecyclerView to connect
     */
    void linkRView(RecyclerView rView) {
        _rView = rView;
    }


    /**
     * Return the holder in the given position
     * @param pos position of holder to return
     * @return holder in pos position
     */
    ViewHolder fromPosToViewHolder(int pos) {
        for (ViewHolder holder: _holders) {
            if (holder.getAdapterPosition() == pos) {
                return holder;
            }
        }

        return null;
    }


    /**
     * Given a ViewHolder, render it when selection mode is activated
     * @param holder ViewHolder to render
     */
    void initialRenderForSelectionModeEnable(ViewHolder holder) {
        holder._view.findViewById(R.id.buttonsLayout).setVisibility(View.INVISIBLE);

        if (_selectedPos.contains(holder.getAdapterPosition())) {
            holder._view.findViewById(R.id.entireCard).setBackgroundColor(
                    _activity.getResources().getColor(R.color.selectedCard));
        }
    }






    /**
     * Inner class representing the view "single card" in the RecyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        TextView _nameTextView;
        TextView _expirationDateTextView;
        TextView _supplyTextView;
        TextView _priceTextView;
        ImageView _categoryImageView;
        Category _category;
        ItemClickListener _clickListener;
        ImageButton _firstButton;
        ImageButton _secondButton;
        ImageButton _thirdButton;
        int _position;
        int _idOfFood;
        View _view;


        ViewHolder(View itemView) {
            super(itemView);
            _view = itemView;
            _view.setBackgroundColor(Color.WHITE);
            _view.findViewById(R.id.buttonsLayout).setBackgroundColor(Color.WHITE);
            _view.findViewById(R.id.buttonsLayout).setVisibility(View.VISIBLE);

            _nameTextView = itemView.findViewById(R.id.nameOfFoodLabel);
            _expirationDateTextView = itemView.findViewById(R.id.expirationDateOfFoodLabel);
            _priceTextView = itemView.findViewById(R.id.priceOfFoodLabel);
            _supplyTextView = itemView.findViewById(R.id.quantityOfFoodLabel);
            _categoryImageView = itemView.findViewById(R.id.foodImage);

            _firstButton = itemView.findViewById(R.id.firstRowButton);
            _secondButton = itemView.findViewById(R.id.secondRowButton);
            _thirdButton = itemView.findViewById(R.id.thirdRowButton);


            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        /**
         * Set a clickListener to the ViewHolder
         * @param itemClickListener to connect to the ViewHolder
         */
        void setClickListener(ItemClickListener itemClickListener) {
            _clickListener = itemClickListener;
        }


        @Override
        public void onClick(View view) {
            _clickListener.onClick(view, getLayoutPosition(), false); // or getAdapterPosition()
        }


        @Override
        public boolean onLongClick(View view) {
            _clickListener.onClick(view, getLayoutPosition(), true); // or getAdapterPosition()
            return true;
        }
    }





    /**
     * Interface which must be implemented in the clickListener linked to an ViewHolder
     */
    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }




}
