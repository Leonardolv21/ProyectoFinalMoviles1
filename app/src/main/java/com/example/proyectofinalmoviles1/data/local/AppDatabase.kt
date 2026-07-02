package com.example.proyectofinalmoviles1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectofinalmoviles1.data.local.dao.*
import com.example.proyectofinalmoviles1.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        MatchEntity::class,
        PredictionEntity::class,
        StadiumEntity::class,
        ParticipantEntity::class,
        SyncMetadataEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun matchDao(): MatchDao
    abstract fun predictionDao(): PredictionDao
    abstract fun stadiumDao(): StadiumDao
    abstract fun participantDao(): ParticipantDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiniela_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
