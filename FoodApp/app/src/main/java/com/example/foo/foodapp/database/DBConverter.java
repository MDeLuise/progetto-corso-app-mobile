package com.example.foo.foodapp.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * class needed in order to store complex data type in the database
 */
public class DBConverter {


    @TypeConverter
    public static Date fromTimestampToDate(Long value) { return value == null ? null : new Date(value); }


    @TypeConverter
    public static Long fromDateToTimestamp(Date date) { return date == null ? null : date.getTime(); }


    @TypeConverter
    public static Category fromIntToCategory(int value) {
        return Category.getName(value);
    }


    @TypeConverter
    public static int fromCategoryToInt(Category category) {
        return category.getValue();
    }

}
