package com.amsmas.rahnama

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.amsmas.rahnama.data.ClientRequestEntity
import com.amsmas.rahnama.data.PersonEntity
import com.amsmas.rahnama.data.PropertyEntity
import com.amsmas.rahnama.data.RahnamaDatabase
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel boundary between Compose UI and Room persistence.
 * UI observes flows and invokes these methods instead of accessing the DAO directly.
 */
class RahnamaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RahnamaDatabase.getInstance(application).dao()

    val people: Flow<List<PersonEntity>> = dao.observePersons()
    val properties: Flow<List<PropertyEntity>> = dao.observeProperties()
    val activeRequests: Flow<List<ClientRequestEntity>> = dao.observeActiveRequests()

    suspend fun findPersonByPhone(phone: String): PersonEntity? = dao.findPersonByPhone(phone)
    suspend fun insertPerson(person: PersonEntity): Long = dao.insertPerson(person)
    suspend fun updatePersonContactDetails(personId: Long, alternatePhone: String?, address: String?) =
        dao.updatePersonContactDetails(personId, alternatePhone, address)

    suspend fun countProperties(): Int = dao.countProperties()
    suspend fun propertyCodeExists(code: String): Boolean = dao.propertyCodeExists(code)
    suspend fun insertProperty(property: PropertyEntity): Long = dao.insertProperty(property)
    suspend fun insertRequest(request: ClientRequestEntity): Long = dao.insertRequest(request)
}
