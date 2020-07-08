package com.example.foo.foodapp.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;


/**
 * class representing the database object
 */
@Database(entities = {Food.class, Favorite.class}, version = 1)
@TypeConverters({DBConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract FoodDAO getFoodDAO();

}
