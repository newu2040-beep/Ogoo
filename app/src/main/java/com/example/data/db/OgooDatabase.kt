package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, CustomLutEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OgooDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun customLutDao(): CustomLutDao

    companion object {
        @Volatile
        private var INSTANCE: OgooDatabase? = null

        fun getDatabase(context: Context): OgooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OgooDatabase::class.java,
                    "ogoo_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
