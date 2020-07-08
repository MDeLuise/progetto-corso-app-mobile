package com.example.foo.foodapp.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;


/**
 * class representing the entity "food" stored in the database
 */
@Entity(tableName = "foods")
public class Food {

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
    @ColumnInfo(name = "supply")
    private int _supply;

    @ColumnInfo(name = "bar_code")
    private String _barcode;

    @NonNull
    @ColumnInfo(name = "expiration_date")
    private Date _expirationDate;

    @NonNull
    @ColumnInfo(name = "category")
    private Category _category;

    @NonNull
    @ColumnInfo(name = "is_visible")
    private boolean _visibility;


    // methods below


    @NonNull
    public int getId() { return _id; }


    public void setId(@NonNull int id) { _id = id; }


    public String getName() { return _name; }


    public void setName(String name) { _name = name; }


    public double getPrice() { return _price; }


    public void setPrice(double price) { _price = price; }


    public int getSupply() { return _supply; }


    public void setSupply(int supply) { _supply = supply; }


    public String getBarcode() { return _barcode; }


    public void setBarcode(String barcode) { _barcode = barcode; }


    public Date getExpirationDate() { return _expirationDate; }


    public void setExpirationDate(Date expirationDate) { _expirationDate = expirationDate; }


    public Category getCategory() { return _category; }


    public void setCategory(Category category) {_category = category; }


    public boolean getVisibility() { return _visibility; }


    public void setVisibility(boolean visibility) { _visibility = visibility;  }

}

