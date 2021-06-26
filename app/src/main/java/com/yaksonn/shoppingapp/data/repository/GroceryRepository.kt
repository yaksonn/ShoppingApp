package com.yaksonn.shoppingapp.data.repository

import com.yaksonn.shoppingapp.data.AppDatabase
import com.yaksonn.shoppingapp.data.model.Grocery
import javax.inject.Inject

class GroceryRepository @Inject constructor(
    private val database: AppDatabase
) {
    fun getGroceryForShoppingList(shoppingListId: Int) =
        database.groceryDao().getGroceryForShoppingList(shoppingListId)

    suspend fun deleteGroceryFromDeletedShoppingList(shoppingListId: Int) =
        database.groceryDao().deleteGroceryFromDeletedShoppingList(shoppingListId)

    suspend fun insert(grocery: Grocery) = database.groceryDao().insert(grocery)

    suspend fun delete(grocery: Grocery) = database.groceryDao().delete(grocery)

    suspend fun update(grocery: Grocery) = database.groceryDao().update(grocery)
}