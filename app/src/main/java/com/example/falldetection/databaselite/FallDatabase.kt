package com.example.falldetection.databaselite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.falldetection.entity.FallEntity
import com.example.falldetection.dao.FallDao

@Database(entities = [FallEntity::class], version = 1)
abstract class FallDatabase : RoomDatabase() {
    abstract fun fallDao(): FallDao

    companion object {
        @Volatile
        private var INSTANCE: FallDatabase? = null

        fun getDatabase(context: Context): FallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FallDatabase::class.java,
                    "fall_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
