package com.example.foo.foodapp.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * class representing the entity "favorite" stored in the database
 */
@Entity(tableName = "favorites")
public class Favorite {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int _id;

    @NonNull
    @ColumnInfo(name = "name")
    private String _name;

    @NonNull
    @ColumnInfo(name = "price")
    private double _price;

    @NonNull
    @ColumnInfo(name = "category")
    private Category _category;


    // methods below


    @Override
    public boolean equals(Object otherFavorite) {
        return (otherFavorite instanceof Favorite) &&
                ((Favorite) otherFavorite).getName().equals(getName()) &&
                ((Favorite) otherFavorite).getPrice() == getPrice() &&
                ((Favorite) otherFavorite).getCategory() == getCategory();
    }


    @NonNull
    public int getId() { return _id; }


    public void setId(@NonNull int id) { _id = id; }


    public String getName() { return _name; }


    public void setName(String name) { _name = name; }


    public double getPrice() { return _price; }


    public void setPrice(double price) { _price = price; }


    public Category getCategory() { return _category; }


    public void setCategory(Category category) {_category = category; }


    public Favorite() {}


    public Favorite(Food startingFood) {
        _name = startingFood.getName();
        _price = startingFood.getPrice();
        _category = startingFood.getCategory();
    }
}
