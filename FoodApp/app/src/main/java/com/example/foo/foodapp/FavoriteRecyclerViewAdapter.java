package com.example.foo.foodapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.example.foo.foodapp.database.Category;
import com.example.foo.foodapp.database.Favorite;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;


/**
 * Class used in the RecyclerView to draw the single favorite "card elements"
 */
public class FavoriteRecyclerViewAdapter extends AbstractRecyclerViewAdapter {
    private FoodRecyclerViewAdapter _foodRView;


    FavoriteRecyclerViewAdapter(Context context, SharedPreferences preferences) {
        super(context, preferences);
    }


    void linkFoodRView(FoodRecyclerViewAdapter foodFragment) {
        _foodRView = foodFragment;
    }


    @Override
    protected void initializeEntries() {
        List<Favorite> favorites = _db.getVisibleFavoritesOrderedByName();

        SortingParam sortParam = SortingParam.valueOf(_preferences.getString(
                "sortOption", SortingParam.valueOf("NAME").toString()));
        if (sortParam == SortingParam.DATE) {
            sortParam = SortingParam.NAME;
        }
        boolean ascending = _preferences.getBoolean("orderOption", true);
        String filter = _preferences.getString("filter", "");

        if (sortParam == SortingParam.PRICE) {
                if (filter.isEmpty()) {
                    favorites = _db.getVisibleFavoritesOrderedByPrice();
                } else {
                    favorites = _db.getVisibleAndFilteredFavoritesOrderedByPrice(filter);
                }

        } else if (sortParam == SortingParam.NAME && !filter.isEmpty()) {
            favorites = _db.getVisibleAndFilteredFavoritesOrderedByName(filter);
        }

        if(!ascending) {Collections.reverse(favorites); }


        _totalVisibleElement = favorites.size();

        _entriesId = new int[favorites.size()];
        _entriesName = new String[favorites.size()];
        _entriesPrice = new double[favorites.size()];
        _entriesCategory = new Category[favorites.size()];

        for (int i=0; i < favorites.size(); i++) {
            _entriesId[i] = favorites.get(i).getId();
            _entriesName[i] = favorites.get(i).getName();
            _entriesPrice[i] = favorites.get(i).getPrice();
            _entriesCategory[i] = favorites.get(i).getCategory();
        }
    }


    @Override
    protected void onConfirmDeleteButtonPressed() {
        // unfavorite all food in db linked to favorite to remove
        Favorite selectedFav;
        ViewHolder holder;
        for (int holderPos: _selectedPos) {
            holder = fromPosToViewHolder(holderPos);
            selectedFav = new Favorite();
            selectedFav.setName(holder._nameTextView.getText().toString());
            selectedFav.setPrice(getFavoritePrice(holder));
            selectedFav.setCategory(holder._category);
            _foodRView.unfavoriteAllInstanceOf(selectedFav);
        }

        // remove favorite and update the list
        for (Integer _selectedPo : _selectedPos) {
            _db.deleteFavoriteById(fromPosToViewHolder(_selectedPo)._idOfFood);
        }


        _selectedPos = new TreeSet<>();
        refresh();
    }


    private double getFavoritePrice(ViewHolder holder) {
        String price = "";
        String rawPrice = holder._priceTextView.getText().toString();
        for (int i=0; i < rawPrice.length(); i++) {
            if (Character.isDigit(rawPrice.charAt(i))) {
                price += rawPrice.charAt(i);
            } else if (rawPrice.charAt(i) == ',') {
                price += ".";
            }
        }
        return Double.parseDouble(price);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String currency = getCurrencySymbol();
        holder.setIsRecyclable(false);

        holder._nameTextView.setText(_entriesName[holder.getAdapterPosition()]);
        holder._categoryImageView.setImageResource(getCategoryImgId(_entriesCategory[position]));
        holder._category = _entriesCategory[position];

        holder._priceTextView.setText((_context.getString(R.string.price) + " " + _entriesPrice[position])
                .replace(".",",") + currency);

        holder._idOfFood = _entriesId[holder.getAdapterPosition()];

        holder._position = holder.getAdapterPosition();

        holder._expirationDateTextView.setVisibility(View.GONE);
        holder._supplyTextView.setVisibility(View.GONE);

        holder._firstButton.setImageResource(R.drawable.add_24px);

        holder._firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddFoodActivity(holder._idOfFood);
            }
        });

        holder._secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditPressed(holder.getAdapterPosition());
            }
        });

        holder._thirdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeletePressed(holder);
            }
        });

        insertHolderAtItsPos(holder);
        createListenerForHolder(holder);


        // this is needed because if a holder is not render yet (i.e. is below the viewed entries
        // in the screen), its onBindViewHolder call will be executed when the user scroll down.
        // If the user do this when selection mode is activated, the holder would have the
        // buttons hidden
        if (_isSelectionModeActivated) {
            initialRenderForSelectionModeEnable(holder);
        }
    }


    /**
     * Open the "add food activity", passing to it the saved data for selected favorite
     */
    private void openAddFoodActivity(int id) {
        Favorite selectedFavorite = _db.getFavoriteById(id);

        Intent intent = new Intent(_context, ManageElementActivity.class);
        intent.putExtra("nameToAdd", selectedFavorite.getName());
        intent.putExtra("priceToAdd", "" + selectedFavorite.getPrice());
        intent.putExtra("categoryToAdd", selectedFavorite.getCategory().toString());
        intent.putExtra("noFavoriteSwitch", true);
        intent.putExtra("oldFoodInserted",
                _preferences.getBoolean("oldFoodInserted", false));

        _context.startActivity(intent);
    }


    /**
     * Implements what happen when "edit" button is pressed
     */
    private void onEditPressed(int position) {
        Favorite favoriteToEdit = _db.getFavoriteById(_entriesId[position]);

        Intent intent = new Intent(_context, ManageElementActivity.class);
        intent.putExtra("nameToAdd", favoriteToEdit.getName());
        intent.putExtra("priceToAdd", "" + favoriteToEdit.getPrice());
        intent.putExtra("categoryToAdd", favoriteToEdit.getCategory().toString());

        intent.putExtra("actionIsToEdit", _entriesId[position]);
        intent.putExtra("editFavorite", true);

        _context.startActivity(intent);
    }


    /**
     * Implements what happen when "delete" button is pressed
     */
    private void onDeletePressed(final ViewHolder holder) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(_context);
        builder1.setMessage(_context.getString(R.string.remove_favorite_confirm));
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                _context.getString(R.string.affermative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _selectedPos.add(holder.getAdapterPosition());
                        onConfirmDeleteButtonPressed();

                        Toast.makeText(_context,
                                _context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                    }
                });


        builder1.setNegativeButton(
                _context.getString(R.string.negative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}
