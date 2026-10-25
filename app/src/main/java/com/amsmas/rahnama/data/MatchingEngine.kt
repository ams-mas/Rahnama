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

/**
 * On-device ranked matching. Core identity fields must match; optional details
 * improve the score. Unknown property details are not treated as confirmed mismatches.
 */
object MatchingEngine {
    fun matchPropertiesToRequest(
        request: ClientRequestEntity,
        properties: List<PropertyEntity>
    ): List<PropertyMatch> = properties.mapNotNull { property ->
        if (property.status != "available") return@mapNotNull null
        if (property.listingType != request.listingType) return@mapNotNull null
        if (property.propertyType != request.propertyType) return@mapNotNull null
        if (!same(property.currency, request.currency)) return@mapNotNull null
        if (!same(property.province, request.province)) return@mapNotNull null

        if (!request.mapLocationNumber.isNullOrBlank() &&
            !same(property.mapLocationNumber, request.mapLocationNumber)
        ) return@mapNotNull null

        val reasons = mutableListOf("نوع معامله، نوع ملک و ولایت مطابق است")
        var score = 45

        if (request.city.isNullOrBlank()) {
            reasons += "شهر برای مشتری مشخص نشده"
        } else if (same(property.city, request.city)) {
            score += 15
            reasons += "شهر مطابق است"
        } else {
            reasons += "شهر متفاوت است"
        }

        if (!request.district.isNullOrBlank()) {
            if (property.district.isNullOrBlank()) {
                reasons += "ناحیهٔ ملک ثبت نشده"
            } else if (same(property.district, request.district)) {
                score += 8
                reasons += "ناحیه مطابق است"
            } else {
                reasons += "ناحیه متفاوت است"
            }
        }

        val amount = when (request.listingType) {
            "rent" -> property.monthlyRent
            "sale", "growy" -> property.priceAmount
            else -> property.priceAmount
        }
        val minAmount = if (request.listingType == "sale") request.budgetMin else null
        val maxAmount = when (request.listingType) {
            "rent" -> request.maxMonthlyRent
            "growy" -> request.desiredGrowyAmount ?: request.budgetMax
            else -> request.budgetMax
        }
        if (amount == null) {
            reasons += if (request.listingType == "rent") "کرایهٔ ماهانهٔ ملک ثبت نشده" else "قیمت ملک ثبت نشده"
        } else {
            if (minAmount != null && amount < minAmount) return@mapNotNull null
            if (maxAmount != null && amount > maxAmount) return@mapNotNull null
            score += 15
            reasons += if (request.listingType == "rent") "کرایه در محدودهٔ بودجه است" else "مبلغ در محدودهٔ بودجه است"
        }

        if (request.listingType == "growy" &&
            request.growyCondition != "either" &&
            request.growyCondition != "unspecified"
        ) {
            if (property.growyCondition.isNullOrBlank()) {
                reasons += "شرایط گروی ملک ثبت نشده"
            } else if (!sameGrowy(property.growyCondition, request.growyCondition)) {
                return@mapNotNull null
            } else {
                score += 5
                reasons += "شرایط گروی مطابق است"
            }
        }

        val minRooms = request.bedroomsMin
        if (minRooms != null) {
            val rooms = property.bedrooms
            when {
                rooms == null -> reasons += "تعداد اتاق ملک ثبت نشده"
                rooms < minRooms -> return@mapNotNull null
                else -> {
                    score += 7
                    reasons += "تعداد اتاق کافی است"
                }
            }
        }

        if (request.areaMin != null || request.areaMax != null) {
            val area = property.areaValue
            when {
                area == null -> reasons += "مساحت ملک ثبت نشده"
                request.areaMin != null && area < request.areaMin -> return@mapNotNull null
                request.areaMax != null && area > request.areaMax -> return@mapNotNull null
                else -> {
                    score += 5
                    reasons += "مساحت مطابق است"
                }
            }
        }

        fun featureMatches(required: Boolean?, actual: Boolean?, label: String): Boolean {
            if (required == null) return true
            if (actual == null) {
                reasons += "$label ملک مشخص نشده"
                return true
            }
            if (actual != required) return false
            score += 3
            reasons += "$label مطابق است"
            return true
        }

        if (!featureMatches(request.kitchenRequired, property.kitchenAvailable, "آشپزخانه")) return@mapNotNull null
        if (!featureMatches(request.electricityRequired, property.electricityAvailable, "برق")) return@mapNotNull null
        if (!featureMatches(request.borewellRequired, property.borewellAvailable, "برمهٔ آب")) return@mapNotNull null
        if (!featureMatches(request.separateGuestEntranceRequired, property.guestHouseSeparateEntrance, "ورودی جداگانهٔ مهمان‌خانه")) return@mapNotNull null

        if (request.city.isNullOrBlank()) score += 5
        PropertyMatch(property, score.coerceIn(0, 100), reasons)
    }.sortedWith(compareByDescending<PropertyMatch> { it.score }.thenBy { it.property.createdAt })

    fun matchRequestsToProperty(
        property: PropertyEntity,
        requests: List<ClientRequestEntity>
    ): List<RequestMatch> = requests.mapNotNull { request ->
        val match = matchPropertiesToRequest(request, listOf(property)).firstOrNull()
            ?: return@mapNotNull null
        RequestMatch(request, match.score, match.reasons)
    }.sortedByDescending { it.score }

    private fun same(first: String?, second: String?): Boolean =
        normalize(first) == normalize(second)

    private fun normalize(value: String?): String = value.orEmpty()
        .trim()
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace("\u200c", "")
        .lowercase()

    private fun sameGrowy(propertyValue: String, requestValue: String): Boolean {
        fun canonical(value: String): String = when (normalize(value)) {
            "اهل سنت", "سنی", "sunni" -> "sunni"
            "اهل تشیع", "شیعه", "شیعی", "shia" -> "shia"
            else -> normalize(value)
        }
        return canonical(propertyValue) == canonical(requestValue)
    }
}
