package com.yaksonn.shoppingapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery")
data class Grocery(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "quantity")
    var quantity: Int,

    @ColumnInfo(name = "pieces")
    var pieces: Int,

    @ColumnInfo(name = "is_done")
    var isDone: Boolean,

    @ColumnInfo(name = "shopping_list_id")
    var shoppingListId: Int,



)