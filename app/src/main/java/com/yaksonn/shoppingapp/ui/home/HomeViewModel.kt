package com.yaksonn.shoppingapp.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaksonn.shoppingapp.data.model.ShoppingList
import com.yaksonn.shoppingapp.data.repository.GroceryRepository
import com.yaksonn.shoppingapp.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groceryRepository: GroceryRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    var selectedTab = MutableLiveData<Int>().apply {
        //init value on start
        value = 0
    }

    fun setSelectedTab(selectedTabI: Int) {
        selectedTab.value = selectedTabI
    }

    fun insertNewShoppingList(shoppingListName: String) =
        viewModelScope.launch(Dispatchers.Default) {
            val shoppingList = ShoppingList(
                0,
                shoppingListName,
                0,
                0,
                Date(),
                false, "", null
            )
            shoppingListRepository.insert(shoppingList)
        }

    fun updateNewShoppingList(shoppingList: ShoppingList, desc: String) =
        viewModelScope.launch(Dispatchers.Default) {
            val shoppingList = ShoppingList(
                shoppingList.id,
                shoppingList.name,
                shoppingList.doneGroceries,
                shoppingList.allGroceries,
                Date(),
                false, desc, null
            )
            shoppingListRepository.insert(shoppingList)
        }

    private var swipedShoppingListId: Int = -1
    private var deleteGroceryJob: Job? = null
    private var updateGroceryJob: Job? = null

    private fun deleteGroceryFromDeletedShoppingList() =
        viewModelScope.launch(Dispatchers.Default) {
            delay(3000)
            groceryRepository.deleteGroceryFromDeletedShoppingList(swipedShoppingListId)
        }

    private fun updateGroceryFromDeletedShoppingList() =
        viewModelScope.launch(Dispatchers.Default) {
            delay(3000)
            groceryRepository.deleteGroceryFromDeletedShoppingList(swipedShoppingListId)
        }

    fun undoDeletedShoppingList(shoppingList: ShoppingList) =
        viewModelScope.launch(Dispatchers.Default) {
            shoppingListRepository.insert(shoppingList)
            deleteGroceryJob!!.cancel()
        }

    fun deleteShoppingListOnSwipe(shoppingList: ShoppingList) =
        viewModelScope.launch(Dispatchers.Default) {
            shoppingListRepository.delete(shoppingList)

            swipedShoppingListId = shoppingList.id
            deleteGroceryJob = deleteGroceryFromDeletedShoppingList()
            deleteGroceryJob!!.start()
        }

    val shoppingListFilteredByArchived = Transformations.switchMap(selectedTab) {
        shoppingListRepository.getShoppingListsByArchivedStatus(it)
    }
}