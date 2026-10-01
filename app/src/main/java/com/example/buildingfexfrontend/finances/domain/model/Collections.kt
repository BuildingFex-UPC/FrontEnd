package com.example.buildingfexfrontend.finances.domain.model

import com.example.buildingfexfrontend.core.util.Dates

/**
 * Recaudación y gastos de gestión (collections bounded context models).
 */
data class AdminExpense(
    val id: String,
    val name: String,
    val amount: Double = 0.0,
    val purchaseDate: String = "",
    val invoicePhotoUrl: String = "",
)

data class SharedUtilityService(
    val id: String,
    val type: String,
    val amount: Double = 0.0,
    val month: String = "",
    val residentCount: Int = 0,
    val perResidentShare: Double = 0.0,
)

data class PayoutEntry(val id: String, val paidOn: String, val amount: Double)

data class FixedPayoutRecipient(
    val id: String,
    val name: String,
    val dni: String = "",
    val phone: String = "",
    val salary: Double = 0.0,
    val intervalDays: Int = 30,
    val nextPaymentDate: String = "",
    val photoUrl: String = "",
    val paymentHistory: List<PayoutEntry> = emptyList(),
    val createdAt: String = "",
)

/**
 * Domain service that replays due fixed payouts (salary-style) up to [today].
 * Mirrors the web `flushDuePaymentsForRow`.
 */
object PayoutSchedule {

    fun addDays(ymd: String, days: Int): String =
        com.example.buildingfexfrontend.core.util.Dates.addDaysYmd(ymd, days.toLong())

    fun flushDue(row: FixedPayoutRecipient, today: String): FixedPayoutRecipient? {
        var next = row.nextPaymentDate
        if (next.isBlank()) return null
        val interval = maxOf(1, row.intervalDays)
        val history = row.paymentHistory.toMutableList()
        var changed = false
        var guard = 0
        while (com.example.buildingfexfrontend.core.util.Dates.compareYmd(next, today) <= 0 && guard < 5000) {
            guard++
            history += PayoutEntry(
                id = "pay-${row.id}-${history.size}-${System.currentTimeMillis()}",
                paidOn = next,
                amount = row.salary,
            )
            next = addDays(next, interval)
            changed = true
        }
        return if (changed) row.copy(paymentHistory = history, nextPaymentDate = next) else null
    }

    /**
     * Payment periods elapsed since the payout creation (the first period counts on the
     * creation day itself), so a newly created payout is counted right away.
     * Returns 0 when [FixedPayoutRecipient.createdAt] is missing or unparseable.
     */
    fun periodsSinceCreation(row: FixedPayoutRecipient, today: String): Int {
        val created = Dates.ymdOrNull(row.createdAt) ?: return 0
        val end = Dates.ymdOrNull(today) ?: created
        val interval = maxOf(1, row.intervalDays).toLong()
        var count = 0
        var date = created
        var guard = 0
        while (!date.isAfter(end) && guard < 5000) {
            guard++
            count++
            date = date.plusDays(interval)
        }
        return count
    }
}
