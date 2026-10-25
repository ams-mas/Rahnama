package com.amsmas.rahnama.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingEngineTest {
    private fun property(
        code: String = "PR-1405-000001",
        listing: String = "sale",
        status: String = "available",
        province: String = "کابل",
        city: String = "کابل",
        price: Double? = 500000.0,
        rent: Double? = null,
        rooms: Int? = 3,
        area: Double? = 120.0,
        kitchen: Boolean? = true,
        growyCondition: String? = "unspecified",
        createdAt: Long = 1L
    ) = PropertyEntity(
        propertyCode = code,
        ownerId = 1L,
        propertyType = "apartment",
        listingType = listing,
        province = province,
        city = city,
        priceAmount = price,
        monthlyRent = rent,
        bedrooms = rooms,
        areaValue = area,
        kitchenAvailable = kitchen,
        growyCondition = growyCondition,
        status = status,
        createdAt = createdAt
    )

    private fun request(
        listing: String = "sale",
        province: String = "کابل",
        city: String? = "کابل",
        minBudget: Double? = 400000.0,
        maxBudget: Double? = 600000.0,
        maxRent: Double? = null,
        roomsMin: Int? = 2,
        areaMin: Double? = 80.0,
        kitchenRequired: Boolean? = true,
        growyCondition: String = "unspecified"
    ) = ClientRequestEntity(
        clientId = 7L,
        listingType = listing,
        propertyType = "apartment",
        budgetMin = minBudget,
        budgetMax = maxBudget,
        maxMonthlyRent = maxRent,
        province = province,
        city = city,
        bedroomsMin = roomsMin,
        areaMin = areaMin,
        kitchenRequired = kitchenRequired,
        growyCondition = growyCondition
    )

    @Test
    fun saleMatchIncludesReasonsAndScore() {
        val matches = MatchingEngine.matchPropertiesToRequest(request(), listOf(property()))
        assertEquals(1, matches.size)
        assertTrue(matches.first().score in 0..100)
        assertTrue(matches.first().reasons.any { it.contains("شهر مطابق") })
        assertTrue(matches.first().reasons.any { it.contains("مبلغ در محدوده") })
    }

    @Test
    fun saleOutsideBudgetIsExcluded() {
        val matches = MatchingEngine.matchPropertiesToRequest(
            request(maxBudget = 450000.0),
            listOf(property(price = 500000.0))
        )
        assertTrue(matches.isEmpty())
    }

    @Test
    fun rentUsesMonthlyRentInsteadOfSaleBudget() {
        val rentRequest = request(
            listing = "rent",
            minBudget = 10000.0,
            maxBudget = 20000.0,
            maxRent = 7000.0,
            roomsMin = null,
            areaMin = null,
            kitchenRequired = null
        )
        val rentProperty = property(listing = "rent", price = null, rent = 6500.0, rooms = null, area = null, kitchen = null)
        assertEquals(1, MatchingEngine.matchPropertiesToRequest(rentRequest, listOf(rentProperty)).size)
    }

    @Test
    fun unavailablePropertyIsExcluded() {
        assertTrue(MatchingEngine.matchPropertiesToRequest(request(), listOf(property(status = "sold"))).isEmpty())
    }

    @Test
    fun arabicPersianLetterVariantsAreNormalized() {
        val normalizedRequest = request(province = "كابل", city = "كابل")
        val matches = MatchingEngine.matchPropertiesToRequest(
            normalizedRequest,
            listOf(property(province = "کابل", city = "کابل"))
        )
        assertEquals(1, matches.size)
    }

    @Test
    fun insufficientRoomCountIsExcluded() {
        val matches = MatchingEngine.matchPropertiesToRequest(
            request(roomsMin = 4),
            listOf(property(rooms = 3))
        )
        assertTrue(matches.isEmpty())
    }

    @Test
    fun betterMatchesAreSortedFirst() {
        val weaker = property(code = "PR-WEAK", city = "هرات", rooms = null, area = null, kitchen = null, createdAt = 1L)
        val stronger = property(code = "PR-STRONG", createdAt = 2L)
        val matches = MatchingEngine.matchPropertiesToRequest(request(), listOf(weaker, stronger))
        assertEquals("PR-STRONG", matches.first().property.propertyCode)
    }
}
