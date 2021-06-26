package com.yaksonn.shoppingapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yaksonn.shoppingapp.data.model.Grocery

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery WHERE shopping_list_id = :shoppingListId ORDER BY is_done = 1")
    fun getGroceryForShoppingList(shoppingListId: Int): LiveData<List<Grocery>>

    @Query("SELECT * FROM grocery WHERE shopping_list_id = :shoppingListId")
    fun getGroceryOfDeletedShoppingList(shoppingListId: Int): List<Grocery>

    @Query("SELECT * FROM grocery WHERE shopping_list_id = :shoppingListId")
    fun getGroceryOfUpdatedShoppingList(shoppingListId: Int): List<Grocery>

    @Transaction
    suspend fun deleteGroceryFromDeletedShoppingList(shoppingListId: Int) {
        val list = getGroceryOfDeletedShoppingList(shoppingListId)
        for (grocery in list) {
            delete(grocery)
        }
    }

    @Transaction
    suspend fun updateGroceryFromDeletedShoppingList(shoppingListId: Int) {
        val list = getGroceryOfUpdatedShoppingList(shoppingListId)
        for (grocery in list) {
            update(grocery)
        }
    }

    @Insert
    suspend fun insert(grocery: Grocery)

    @Delete
    suspend fun delete(grocery: Grocery)

    @Update
    suspend fun update(grocery: Grocery)
}