package com.yaksonn.shoppingapp.data.repository

import com.yaksonn.shoppingapp.data.AppDatabase
import com.yaksonn.shoppingapp.data.model.ShoppingList
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val database: AppDatabase
) {
    fun getShoppingListById(shoppingListId: Int) = database.shoppingListDao().getShoppingListById(shoppingListId)

    fun getShoppingListsByArchivedStatus(selectedTab: Int) = database.shoppingListDao().getShoppingListsByArchivedStatus(selectedTab)

    suspend fun insert(shoppingList: ShoppingList) = database.shoppingListDao().insert(shoppingList)

    suspend fun delete(shoppingList: ShoppingList) = database.shoppingListDao().delete(shoppingList)

    suspend fun update(shoppingList: ShoppingList) = database.shoppingListDao().update(shoppingList)
}