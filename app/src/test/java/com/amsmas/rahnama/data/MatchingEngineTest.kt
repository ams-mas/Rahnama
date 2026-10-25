package com.amsmas.rahnama.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingEngineTest {
    @Test
    fun matchesSalePropertyWhenLocationAndBudgetFit() {
        val property = PropertyEntity(
            id = 1, propertyCode = "PR-2026-000001", ownerId = 10,
            propertyType = "house", listingType = "sale",
            province = "Kabul", city = "Kabul", priceAmount = 1000000.0
        )
        val request = ClientRequestEntity(
            id = 1, clientId = 20, listingType = "sale",
            propertyType = "house", budgetMin = 800000.0, budgetMax = 1200000.0,
            province = "Kabul", city = "Kabul"
        )
        val result = MatchingEngine.matchPropertiesToRequest(request, listOf(property))
        assertEquals(1, result.size)
        assertTrue(result.first().score > 0)
    }

    @Test
    fun doesNotMatchWhenBudgetMaxIsExceeded() {
        val property = PropertyEntity(
            id = 1, propertyCode = "PR-2026-000002", ownerId = 10,
            propertyType = "house", listingType = "sale",
            province = "Kabul", city = "Kabul", priceAmount = 1500000.0
        )
        val request = ClientRequestEntity(
            id = 1, clientId = 20, listingType = "sale",
            propertyType = "house", budgetMax = 1000000.0,
            province = "Kabul", city = "Kabul"
        )
        assertTrue(MatchingEngine.matchPropertiesToRequest(request, listOf(property)).isEmpty())
    }
}
