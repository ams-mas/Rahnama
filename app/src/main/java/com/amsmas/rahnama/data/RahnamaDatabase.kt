package com.amsmas.rahnama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PersonEntity::class, PropertyEntity::class, ClientRequestEntity::class],
    version = 3,
    exportSchema = false
)
abstract class RahnamaDatabase : RoomDatabase() {
    abstract fun dao(): RahnamaDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN constructionType TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN mapLocationNumber TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN directionPosition TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN kitchenAvailable INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN electricityAvailable INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN borewellAvailable INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN guestHouseAvailable INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN guestHouseSeparateEntrance INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN landBoundaryType TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN hasOwnWall INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN neighborWallSides INTEGER")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN kitchenRequired INTEGER")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN electricityRequired INTEGER")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN borewellRequired INTEGER")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN separateGuestEntranceRequired INTEGER")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN mapLocationNumber TEXT")
                db.execSQL("ALTER TABLE client_requests ADD COLUMN requestedAddressType TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN propertyTypeDetails TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN kotahAvailable INTEGER")
            }
        }

        @Volatile private var INSTANCE: RahnamaDatabase? = null

        fun getInstance(context: Context): RahnamaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RahnamaDatabase::class.java,
                    "rahnama.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
    }
}
