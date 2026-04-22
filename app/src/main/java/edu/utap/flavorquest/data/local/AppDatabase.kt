package edu.utap.flavorquest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.utap.flavorquest.data.local.dao.RecipeDao
import edu.utap.flavorquest.data.local.dao.RestaurantDao
import edu.utap.flavorquest.data.local.dao.SearchHistoryDao
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.data.model.SearchHistory

@Database(
    entities = [Recipe::class, Restaurant::class, SearchHistory::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flavorquest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}