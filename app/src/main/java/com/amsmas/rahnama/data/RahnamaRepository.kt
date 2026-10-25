package com.amsmas.rahnama.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository is the single data-access boundary for the presentation layer.
 * Room remains the local source of truth for the app.
 */
class RahnamaRepository(private val dao: RahnamaDao) {
    val people: Flow<List<PersonEntity>> = dao.observePersons()
    val properties: Flow<List<PropertyEntity>> = dao.observeProperties()
    val activeRequests: Flow<List<ClientRequestEntity>> = dao.observeActiveRequests()
    val allRequests: Flow<List<ClientRequestEntity>> = dao.observeAllRequests()

    suspend fun findPersonByPhone(phone: String): PersonEntity? = dao.findPersonByPhone(phone)
    suspend fun findOtherPersonByPhone(phone: String, personId: Long): PersonEntity? = dao.findOtherPersonByPhone(phone, personId)
    suspend fun insertPerson(person: PersonEntity): Long = dao.insertPerson(person)
    suspend fun updatePerson(person: PersonEntity) = dao.updatePerson(person)
    suspend fun updatePersonContactDetails(personId: Long, alternatePhone: String?, address: String?) =
        dao.updatePersonContactDetails(personId, alternatePhone, address)

    suspend fun countProperties(): Int = dao.countProperties()
    suspend fun propertyCodeExists(code: String): Boolean = dao.propertyCodeExists(code)
    suspend fun insertProperty(property: PropertyEntity): Long = dao.insertProperty(property)
    suspend fun insertRequest(request: ClientRequestEntity): Long = dao.insertRequest(request)
    suspend fun updatePropertyStatus(propertyId: Long, status: String) = dao.updatePropertyStatus(propertyId, status)
    suspend fun updateRequestStatus(requestId: Long, status: String) = dao.updateRequestStatus(requestId, status)
}
