package com.example.mpproject.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mpproject.PMApplication

// 29.09.2024 by Arman Yerkeshev 2214297
// Database class, representing the local database
@Database(entities = [ParliamentMember::class], version = 1)
abstract class PMDatabase : RoomDatabase() {
    abstract fun memberDao(): ParliamentMemberDao

    companion object {
        @Volatile
        private var Instance: PMDatabase? = null

        fun getInstance(): PMDatabase {
            if (Instance == null) {
                synchronized(this) {
                    Instance = Room.databaseBuilder(
                        PMApplication.appContext, PMDatabase::class.java, "eduskunta-db"
                    ).build()
                }
            }
            return Instance!!
        }
    }
}