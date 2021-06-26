package com.yaksonn.shoppingapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yaksonn.shoppingapp.data.model.ShoppingList

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list WHERE id = :shoppingListId")
    fun getShoppingListById(shoppingListId: Int): ShoppingList

    //in room sql 0 stands for false and unarchived list of shopping lists is on tab at position 0
    @Query("SELECT * FROM shopping_list WHERE is_archived = :selectedTab ORDER BY date DESC")
    fun getShoppingListsByArchivedStatus(selectedTab: Int): LiveData<List<ShoppingList>>

    @Insert
    suspend fun insert(shoppingList: ShoppingList)

    @Delete
    suspend fun delete(shoppingList: ShoppingList)

    @Update
    suspend fun update(shoppingList: ShoppingList)
}