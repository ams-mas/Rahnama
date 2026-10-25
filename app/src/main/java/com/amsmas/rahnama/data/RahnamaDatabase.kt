package com.amsmas.rahnama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PersonEntity::class, PropertyEntity::class, ClientRequestEntity::class],
    version = 4,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Keep every existing property; only rename duplicate codes before enforcing uniqueness.
                db.execSQL(
                    "UPDATE properties SET propertyCode = propertyCode || '-DUP-' || id " +
                    "WHERE id NOT IN (SELECT MIN(id) FROM properties GROUP BY propertyCode)"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_properties_propertyCode ON properties(propertyCode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_properties_listingType_propertyType_status ON properties(listingType, propertyType, status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_properties_province_city_district ON properties(province, city, district)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_persons_phoneNumber ON persons(phoneNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_client_requests_listingType_propertyType_requestStatus ON client_requests(listingType, propertyType, requestStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_client_requests_province_city ON client_requests(province, city)")
            }
        }

        @Volatile private var INSTANCE: RahnamaDatabase? = null

        fun getInstance(context: Context): RahnamaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RahnamaDatabase::class.java,
                    "rahnama.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
