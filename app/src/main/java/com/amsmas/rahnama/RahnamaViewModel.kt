package com.amsmas.rahnama

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.amsmas.rahnama.data.ClientRequestEntity
import com.amsmas.rahnama.data.PersonEntity
import com.amsmas.rahnama.data.PropertyEntity
import com.amsmas.rahnama.data.RahnamaDatabase
import com.amsmas.rahnama.data.RahnamaRepository
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel boundary between Compose UI and app data.
 * It exposes observable records and delegates persistence to the repository.
 */
class RahnamaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RahnamaRepository(RahnamaDatabase.getInstance(application).dao())

    val people: Flow<List<PersonEntity>> = repository.people
    val properties: Flow<List<PropertyEntity>> = repository.properties
    val activeRequests: Flow<List<ClientRequestEntity>> = repository.activeRequests

    suspend fun findPersonByPhone(phone: String): PersonEntity? = repository.findPersonByPhone(phone)
    suspend fun insertPerson(person: PersonEntity): Long = repository.insertPerson(person)
    suspend fun updatePersonContactDetails(personId: Long, alternatePhone: String?, address: String?) =
        repository.updatePersonContactDetails(personId, alternatePhone, address)

    suspend fun countProperties(): Int = repository.countProperties()
    suspend fun propertyCodeExists(code: String): Boolean = repository.propertyCodeExists(code)
    suspend fun insertProperty(property: PropertyEntity): Long = repository.insertProperty(property)
    suspend fun insertRequest(request: ClientRequestEntity): Long = repository.insertRequest(request)
}
