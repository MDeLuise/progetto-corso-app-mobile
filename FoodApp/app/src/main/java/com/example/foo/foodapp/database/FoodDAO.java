package com.example.foo.foodapp.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * class representing the database API
 */
@Dao
public interface FoodDAO {

    /**
     * =============================================================================================
     * ========================= BELOW THERE ARE THE Food functions for database ===================
     * =============================================================================================
     */

    @Insert
    void insert(Food... items);

    @Query("SELECT * FROM foods")
    List<Food> getFoods();

    @Query("SELECT * FROM foods where id = :id")
    Food getFoodFromId(int id);

    @Query("SELECT * FROM foods ORDER BY id desc")
    List<Food> getFoodsOrderedById();

    @Query("UPDATE foods SET is_visible = 'N' WHERE id = :id")
    void hideFoodById(int id);

    @Query("SELECT * FROM foods WHERE is_visible")
    List<Food> getVisibleFoods();

    @Query("DELETE FROM foods WHERE NOT is_visible")
    void removeHiddenFoods();

    @Query("SELECT * FROM foods WHERE is_visible ORDER BY expiration_date asc")
    List<Food> getVisibleFoodsOrderedByExpirationDate();

    @Query("SELECT * FROM foods WHERE is_visible ORDER BY LOWER(name) asc")
    List<Food> getVisibleFoodsOrderedByName();

    @Query("SELECT * FROM foods WHERE is_visible ORDER BY price asc")
    List<Food> getVisibleFoodsOrderedByPrice();

    @Query("SELECT * FROM foods WHERE is_visible ORDER BY supply asc")
    List<Food> getVisibleFoodsOrderedBySupply();

    @Query("SELECT * FROM foods WHERE is_visible AND name LIKE '%' || :filter || '%' ORDER BY expiration_date asc")
    List<Food> getVisibleAndFilteredFoodsOrderedByExpirationDate(String filter);

    @Query("SELECT * FROM foods WHERE is_visible AND name LIKE '%' || :filter || '%' ORDER BY name asc")
    List<Food> getVisibleAndFilteredFoodsOrderedByName(String filter);

    @Query("SELECT * FROM foods WHERE is_visible AND name LIKE '%' || :filter || '%' ORDER BY price asc")
    List<Food> getVisibleAndFilteredFoodsOrderedByPrice(String filter);

    @Query("SELECT * FROM foods WHERE is_visible AND name LIKE '%' || :filter || '%' ORDER BY supply asc")
    List<Food> getVisibleAndFilteredFoodsOrderedBySupply(String filter);


    /**
     * ===================================================================================================
     * ======================= BELOW THERE ARE THE Favorite functions for database =======================
     * ===================================================================================================
     */

    @Insert
    void insert(Favorite... items);

    @Query("SELECT * FROM favorites")
    List<Favorite> getFavorites();

    @Query("SELECT * FROM favorites WHERE id = :id")
    Favorite getFavoriteById(int id);

    @Query("DELETE FROM favorites WHERE id = :id")
    void deleteFavoriteById(int id);

    @Query("SELECT * FROM favorites ORDER BY LOWER(name) asc")
    List<Favorite> getVisibleFavoritesOrderedByName();

    @Query("SELECT * FROM favorites ORDER BY price asc")
    List<Favorite> getVisibleFavoritesOrderedByPrice();

    @Query("SELECT * FROM favorites WHERE name LIKE '%' || :filter || '%' ORDER BY name asc")
    List<Favorite> getVisibleAndFilteredFavoritesOrderedByName(String filter);

    @Query("SELECT * FROM favorites WHERE name LIKE '%' || :filter || '%' ORDER BY price asc")
    List<Favorite> getVisibleAndFilteredFavoritesOrderedByPrice(String filter);


    /**
     * =============================================================================================
     * ========================= BELOW THERE ARE THE DELETE ALL FUNCTIONS ==========================
     * =============================================================================================
     */

    @Query("DELETE FROM foods")
    void initializeFoodsTable();

    @Query("DELETE FROM favorites")
    void initializeFavoritesTable();

}
