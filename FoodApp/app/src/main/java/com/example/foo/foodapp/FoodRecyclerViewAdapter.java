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
import com.example.foo.foodapp.database.Food;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;


/**
 * Class used in the RecyclerView to draw the single food "card elements"
 */
public class FoodRecyclerViewAdapter extends AbstractRecyclerViewAdapter {


    public FoodRecyclerViewAdapter(Context context, SharedPreferences preferences) {
        super(context, preferences);
    }


    @Override
    protected void initializeEntries() {
        List <Food> foods = null;

        SortingParam sortParam = SortingParam.valueOf(_preferences.getString("sortOption",
                SortingParam.valueOf("DATE").toString()));
        boolean ascending = _preferences.getBoolean("orderOption", true);
        String filter = _preferences.getString("filter", "");

        switch (sortParam) {
            case NAME:
                if (filter.isEmpty()) {
                    foods = _db.getVisibleFoodsOrderedByName();
                } else {
                    foods = _db.getVisibleAndFilteredFoodsOrderedByName(filter);
                }
                break;
            case DATE:
                if (filter.isEmpty()) {
                    foods = _db.getVisibleFoodsOrderedByExpirationDate();
                } else {
                    foods = _db.getVisibleAndFilteredFoodsOrderedByExpirationDate(filter);
                }
                break;
            case PRICE:
                if (filter.isEmpty()) {
                    foods = _db.getVisibleFoodsOrderedByPrice();
                } else {
                    foods = _db.getVisibleAndFilteredFoodsOrderedByPrice(filter);
                }
                break;
        }

        if(!ascending) { Collections.reverse(foods); }


        _totalVisibleElement = foods.size();

        _entriesId = new int[foods.size()];
        _entriesName = new String[foods.size()];
        _entriesSupply = new int[foods.size()];
        _entriesExpirationDate = new Date[foods.size()];
        _entriesPrice = new double[foods.size()];
        _entriesCategory = new Category[foods.size()];

        for ( int i=0; i < foods.size(); i++ ) {
            _entriesId[i] = foods.get(i).getId();
            _entriesName[i] = foods.get(i).getName();
            _entriesSupply[i] = foods.get(i).getSupply();
            _entriesExpirationDate[i] = foods.get(i).getExpirationDate();
            _entriesPrice[i] = foods.get(i).getPrice();
            _entriesCategory[i] = foods.get(i).getCategory();
        }
    }


    @Override
    protected void onConfirmDeleteButtonPressed() {
        // remove food and update the list
        for (Integer _selectedPo : _selectedPos) {
            _db.hideFoodById(fromPosToViewHolder(_selectedPo)._idOfFood);
        }

        _selectedPos = new TreeSet<>();
        refresh();
    }


    /**
     * Creates the single "card" and link action to the click on that
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ITALIAN);
        String currency = getCurrencySymbol();
        holder.setIsRecyclable(false);

        holder._nameTextView.setText(_entriesName[holder.getAdapterPosition()]);
        holder._expirationDateTextView.setText(_context.getString(R.string.expiration) + " " +
                dateFormat.format(_entriesExpirationDate[holder.getAdapterPosition()]));
        holder._supplyTextView.setText(_context.getString(R.string.supply) + " " +
                _entriesSupply[holder.getAdapterPosition()]);
        holder._priceTextView.setText((_context.getString(R.string.price) + " " +
                _entriesPrice[holder.getAdapterPosition()]).replace(".", ",") + currency);
        holder._categoryImageView.setImageResource(getCategoryImgId(_entriesCategory[holder.getAdapterPosition()]));
        holder._category = _entriesCategory[holder.getAdapterPosition()];
        holder._idOfFood = _entriesId[holder.getAdapterPosition()];
        holder._position = holder.getAdapterPosition();


        final boolean isInFavoriteDB = _db.getFavorites().contains(
                new Favorite(_db.getFoodFromId(_entriesId[holder.getAdapterPosition()])));
        if (isInFavoriteDB) {
            holder._firstButton.setImageResource(R.drawable.ic_heart);
        }


        holder._firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favorite thisFavorite = new Favorite(_db.getFoodFromId(_entriesId[holder.getAdapterPosition()]));
                final boolean isInFavoriteDB = _db.getFavorites().contains(
                        new Favorite(_db.getFoodFromId(_entriesId[holder.getAdapterPosition()])));

                if (!isInFavoriteDB) { // case of favorite added
                    _db.insert(thisFavorite);
                    Toast.makeText(_context,
                            _context.getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                } else { // case of favorite removed
                    List<Favorite> allFavorite = _db.getFavorites();
                    for (Favorite favorite: allFavorite) {
                        if (favorite.equals(thisFavorite)) {
                            _db.deleteFavoriteById(favorite.getId());
                            Toast.makeText(_context,
                                    _context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                changeAllInstanceOfFavoriteButtons(holder, !isInFavoriteDB);
            }
        });


        holder._secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditPressed(_entriesId[holder.getAdapterPosition()]);
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
     * Implements what happen when "delete" button is pressed
     */
    private void onDeletePressed(final ViewHolder holder) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(_context);
        builder1.setMessage(_context.getString(R.string.remove_food_confirm));
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                _context.getString(R.string.affermative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _selectedPos.add(holder.getAdapterPosition());
                        onConfirmDeleteButtonPressed();

                        Toast.makeText(_context,
                                _context.getString(R.string.removed_food), Toast.LENGTH_SHORT).show();
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


    /**
     * Implements what happen when "edit" button is pressed
     */
    private void onEditPressed(final int idOfFood) {
        Food foodToEdit = _db.getFoodFromId(idOfFood);

        Intent intent = new Intent(_context, ManageElementActivity.class);
        intent.putExtra("nameToAdd", foodToEdit.getName());
        intent.putExtra("priceToAdd", "" + foodToEdit.getPrice());
        intent.putExtra("categoryToAdd", foodToEdit.getCategory().toString());
        intent.putExtra("supplyToAdd", "" + foodToEdit.getSupply());

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
        intent.putExtra("expirationDateToAdd", dateFormat.format(foodToEdit.getExpirationDate()));

        intent.putExtra("actionIsToEdit", idOfFood);
        intent.putExtra("noFavoriteSwitch", true);
        intent.putExtra("oldFoodInserted",
                _preferences.getBoolean("oldFoodInserted", false));

        _context.startActivity(intent);
    }


    /**
     * Change all food instance equals to a given one as on their there was clicked the "favorite"
     * button
     * @param changed instance of element changed
     * @param isNowFavorite true = changed is now in favorite, false = is removed from it
     */
    private void changeAllInstanceOfFavoriteButtons(ViewHolder changed, boolean isNowFavorite) {
        Favorite favChanged = new Favorite(_db.getFoodFromId(changed._idOfFood));
        Favorite favMaybeToChange;
        for (int i=0; i<_holders.size(); i++) {
            favMaybeToChange = new Favorite(_db.getFoodFromId(_holders.get(i)._idOfFood));
            if (favChanged.equals(favMaybeToChange)) {
                if (isNowFavorite) {
                    _holders.get(i)._firstButton.setImageResource(R.drawable.ic_heart);
                } else {
                    _holders.get(i)._firstButton.setImageResource(R.drawable.ic_heart_outline);
                }
            }
        }
    }


    void unfavoriteAllInstanceOf(Favorite fav) {
        Favorite favMaybeToChange;
        for (int i=0; i<_holders.size(); i++) {
            favMaybeToChange = new Favorite(_db.getFoodFromId(_holders.get(i)._idOfFood));
            if (fav.equals(favMaybeToChange)) {
                _holders.get(i)._firstButton.setImageResource(R.drawable.ic_heart_outline);
            }
        }
    }




}
