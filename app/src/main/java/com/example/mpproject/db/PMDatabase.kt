
package com.example.mpproject.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

// 09.10.2024 by Hafiz

@Database(entities = [ParliamentMember::class], version = 1)
abstract class PMDatabase : RoomDatabase() {
    abstract fun parliamentMemberDao(): ParliamentMemberDao

    companion object {
        @Volatile
        private var INSTANCE: PMDatabase? = null

        fun getInstance(context: Context): PMDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PMDatabase::class.java,
                    "pm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}