package com.amsmas.rahnama.data

import java.time.LocalDate
import java.time.temporal.ChronoField

object PropertyCodeGenerator {
    /**
     * Produces a display candidate. The Room unique index is the final duplicate guard.
     * For multi-user/cloud synchronization, replace with a server-side atomic sequence.
     */
    fun candidate(sequence: Int, year: Int = LocalDate.now().get(ChronoField.YEAR)): String {
        require(sequence > 0) { "Sequence must be positive" }
        return "PR-$year-${sequence.toString().padStart(6, '0')}"
    }
}
