package com.yaksonn.shoppingapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yaksonn.shoppingapp.data.converters.DateConverter
import com.yaksonn.shoppingapp.data.dao.GroceryDao
import com.yaksonn.shoppingapp.data.dao.ShoppingListDao
import com.yaksonn.shoppingapp.data.model.Grocery
import com.yaksonn.shoppingapp.data.model.ShoppingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


@Database(
    entities = [ShoppingList::class, Grocery::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private const val DATABASE_NAME: String = "shopping_app_database"

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.Default).launch {
                                instance!!.shoppingListDao().insert(
                                    ShoppingList(
                                        1,
                                        "Örnek Alışveriş Listesi",
                                        1,
                                        2,
                                        Date(),
                                        false,"",null
                                    )
                                )
                                instance!!.groceryDao()
                                    .insert(Grocery(0, "Süt", 4, 1,false, 1))
                                instance!!.groceryDao()
                                    .insert(Grocery(0, "Domates", 1, 2,true, 1))
                            }
                        }
                    })
                    .build()
            return instance!!
        }
    }
}