package com.amsmas.rahnama.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RahnamaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerson(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRequest(request: ClientRequestEntity): Long

    @Query("SELECT * FROM persons ORDER BY fullName COLLATE NOCASE")
    fun observePersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findPersonByPhone(phone: String): PersonEntity?

    @Query("UPDATE persons SET alternatePhone = :alternatePhone, address = COALESCE(:address, address) WHERE id = :personId")
    suspend fun updatePersonContactDetails(personId: Long, alternatePhone: String?, address: String?)

    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun observeProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM client_requests WHERE requestStatus = 'active' ORDER BY createdAt DESC")
    fun observeActiveRequests(): Flow<List<ClientRequestEntity>>

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun countProperties(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM properties WHERE propertyCode = :code)")
    suspend fun propertyCodeExists(code: String): Boolean

    @Query("SELECT COUNT(*) FROM properties WHERE status = 'available'")
    fun observeAvailablePropertyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM client_requests WHERE requestStatus = 'active'")
    fun observeActiveRequestCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM properties WHERE status = 'sold'")
    fun observeSoldPropertyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM properties WHERE status = 'rented'")
    fun observeRentedPropertyCount(): Flow<Int>

    @Query("SELECT * FROM properties WHERE status = 'available'")
    suspend fun getAvailableProperties(): List<PropertyEntity>

    @Query("SELECT * FROM client_requests WHERE requestStatus = 'active'")
    suspend fun getActiveRequests(): List<ClientRequestEntity>
}
