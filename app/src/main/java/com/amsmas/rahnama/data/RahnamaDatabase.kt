package com.amsmas.rahnama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PersonEntity::class, PropertyEntity::class, ClientRequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RahnamaDatabase : RoomDatabase() {
    abstract fun dao(): RahnamaDao

    companion object {
        @Volatile private var INSTANCE: RahnamaDatabase? = null

        fun getInstance(context: Context): RahnamaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RahnamaDatabase::class.java,
                    "rahnama.db"
                ).build().also { INSTANCE = it }
            }
    }
}
