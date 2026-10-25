package com.amsmas.rahnama.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "persons", indices = [Index(value = ["phoneNumber"])])
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val alternatePhone: String? = null,
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val roles: String = "other",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["propertyCode"], unique = true),
        Index(value = ["listingType", "propertyType", "status"]),
        Index(value = ["province", "city", "district"])
    ]
)
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyCode: String,
    val ownerId: Long,
    val propertyType: String,
    val listingType: String,
    val province: String,
    val city: String,
    val district: String? = null,
    val address: String? = null,
    val areaValue: Double? = null,
    val areaUnit: String? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val constructionType: String? = null,
    val mapLocationNumber: String? = null,
    val directionPosition: String? = null,
    val kitchenAvailable: Boolean? = null,
    val electricityAvailable: Boolean? = null,
    val borewellAvailable: Boolean? = null,
    val guestHouseAvailable: Boolean? = null,
    val guestHouseSeparateEntrance: Boolean? = null,
    val landBoundaryType: String? = null,
    val hasOwnWall: Boolean? = null,
    val neighborWallSides: Int? = null,
    val description: String? = null,
    val priceAmount: Double? = null,
    val currency: String = "AFN",
    val monthlyRent: Double? = null,
    val growyCondition: String? = null,
    val status: String = "available",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "client_requests",
    indices = [
        Index(value = ["listingType", "propertyType", "requestStatus"]),
        Index(value = ["province", "city"])
    ]
)
data class ClientRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val listingType: String,
    val propertyType: String,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null,
    val currency: String = "AFN",
    val maxMonthlyRent: Double? = null,
    val desiredGrowyAmount: Double? = null,
    val growyCondition: String = "unspecified",
    val province: String,
    val city: String? = null,
    val district: String? = null,
    val bedroomsMin: Int? = null,
    val kitchenRequired: Boolean? = null,
    val electricityRequired: Boolean? = null,
    val borewellRequired: Boolean? = null,
    val separateGuestEntranceRequired: Boolean? = null,
    val mapLocationNumber: String? = null,
    val requestedAddressType: String? = null,
    val areaMin: Double? = null,
    val areaMax: Double? = null,
    val notes: String? = null,
    val requestStatus: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)
