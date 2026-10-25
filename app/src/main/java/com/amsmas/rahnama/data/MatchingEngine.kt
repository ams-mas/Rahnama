package com.amsmas.rahnama.data

data class PropertyMatch(
    val property: PropertyEntity,
    val score: Int,
    val reasons: List<String>
)

data class RequestMatch(
    val request: ClientRequestEntity,
    val score: Int,
    val reasons: List<String>
)

object MatchingEngine {
    fun matchPropertiesToRequest(
        request: ClientRequestEntity,
        properties: List<PropertyEntity>
    ): List<PropertyMatch> = properties.mapNotNull { property ->
        if (property.status != "available") return@mapNotNull null
        if (property.listingType != request.listingType) return@mapNotNull null
        if (property.propertyType != request.propertyType) return@mapNotNull null
        if (!property.currency.equals(request.currency, ignoreCase = true)) return@mapNotNull null
        if (!property.province.equals(request.province, ignoreCase = true)) return@mapNotNull null

        val reasons = mutableListOf("نوع معامله، ملک و ولایت مطابق است")
        var score = 60
        if (request.city.isNullOrBlank() || property.city.equals(request.city, ignoreCase = true)) {
            score += 15
            reasons += "شهر مطابق است"
        }
        if (!request.district.isNullOrBlank() && property.district.equals(request.district, ignoreCase = true)) {
            score += 10
            reasons += "ناحیه مطابق است"
        }

        val price = property.priceAmount
        if (price != null) {
            if (request.budgetMin != null && price < request.budgetMin) return@mapNotNull null
            if (request.budgetMax != null && price > request.budgetMax) return@mapNotNull null
            score += 10
            reasons += "مبلغ در محدوده بودجه است"
        }
        val monthly = when (property.listingType) {
            "rent" -> property.priceAmount
            "growy" -> property.monthlyRent
            else -> null
        }
        if (request.maxMonthlyRent != null && monthly != null && monthly > request.maxMonthlyRent) {
            return@mapNotNull null
        }
        if (monthly != null) {
            score += 5
            reasons += "کرایه ماهانه مطابق است"
        }
        if (property.listingType == "growy" &&
            request.growyCondition != "either" &&
            request.growyCondition != "unspecified" &&
            request.growyCondition != property.growyCondition
        ) return@mapNotNull null

        PropertyMatch(property, score.coerceAtMost(100), reasons)
    }.sortedByDescending { it.score }

    fun matchRequestsToProperty(
        property: PropertyEntity,
        requests: List<ClientRequestEntity>
    ): List<RequestMatch> = requests.mapNotNull { request ->
        val match = matchPropertiesToRequest(request, listOf(property)).firstOrNull()
            ?: return@mapNotNull null
        RequestMatch(request, match.score, match.reasons)
    }.sortedByDescending { it.score }
}
