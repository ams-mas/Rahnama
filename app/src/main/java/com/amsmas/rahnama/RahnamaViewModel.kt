package com.amsmas.rahnama

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amsmas.rahnama.data.ClientRequestEntity
import com.amsmas.rahnama.data.PersonEntity
import com.amsmas.rahnama.data.PropertyCodeGenerator
import com.amsmas.rahnama.data.PropertyEntity
import com.amsmas.rahnama.data.RahnamaDatabase
import com.amsmas.rahnama.data.RahnamaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel boundary between Compose UI and app data.
 * Owns asynchronous write operations; Compose only validates and presents results.
 */
class RahnamaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RahnamaRepository(RahnamaDatabase.getInstance(application).dao())

    val people: Flow<List<PersonEntity>> = repository.people
    val properties: Flow<List<PropertyEntity>> = repository.properties
    val activeRequests: Flow<List<ClientRequestEntity>> = repository.activeRequests
    val allRequests: Flow<List<ClientRequestEntity>> = repository.allRequests

    fun savePerson(person: PersonEntity, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                val phone = person.phoneNumber.trim()
                if (repository.findPersonByPhone(phone) != null) {
                    throw IllegalArgumentException("این شماره تلفن قبلاً برای شخص دیگری ثبت شده است.")
                }
                repository.insertPerson(person.copy(phoneNumber = phone))
                onComplete(Result.success(Unit))
            } catch (error: Exception) {
                onComplete(Result.failure(error))
            }
        }
    }

    fun updatePerson(person: PersonEntity, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                val phone = person.phoneNumber.trim()
                if (repository.findOtherPersonByPhone(phone, person.id) != null) {
                    throw IllegalArgumentException("این شماره تلفن قبلاً برای شخص دیگری ثبت شده است.")
                }
                repository.updatePerson(person.copy(phoneNumber = phone))
                onComplete(Result.success(Unit))
            } catch (error: Exception) {
                onComplete(Result.failure(error))
            }
        }
    }

    fun saveProperty(
        ownerDraft: PersonEntity,
        propertyDraft: PropertyEntity,
        jalaliYear: Int,
        onComplete: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingOwner = repository.findPersonByPhone(ownerDraft.phoneNumber.trim())
                val ownerId = existingOwner?.id ?: repository.insertPerson(ownerDraft)
                if (existingOwner != null && ownerDraft.alternatePhone != null) {
                    repository.updatePersonContactDetails(
                        existingOwner.id,
                        ownerDraft.alternatePhone,
                        ownerDraft.address
                    )
                }

                var sequence = repository.countProperties() + 1
                var code = PropertyCodeGenerator.candidate(sequence, jalaliYear)
                while (repository.propertyCodeExists(code)) {
                    sequence++
                    code = PropertyCodeGenerator.candidate(sequence, jalaliYear)
                }
                repository.insertProperty(propertyDraft.copy(propertyCode = code, ownerId = ownerId))
                onComplete(Result.success(code))
            } catch (error: Exception) {
                onComplete(Result.failure(error))
            }
        }
    }

    fun saveRequest(
        clientDraft: PersonEntity,
        requestDraft: ClientRequestEntity,
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingClient = repository.findPersonByPhone(clientDraft.phoneNumber.trim())
                val clientId = existingClient?.id ?: repository.insertPerson(clientDraft)
                if (existingClient != null && clientDraft.alternatePhone != null) {
                    repository.updatePersonContactDetails(
                        existingClient.id,
                        clientDraft.alternatePhone,
                        null
                    )
                }
                repository.insertRequest(requestDraft.copy(clientId = clientId))
                onComplete(Result.success(Unit))
            } catch (error: Exception) {
                onComplete(Result.failure(error))
            }
        }
    }
}
