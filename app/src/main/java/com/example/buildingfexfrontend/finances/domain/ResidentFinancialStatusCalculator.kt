package com.example.buildingfexfrontend.finances.domain

import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.residents.domain.model.Resident
import java.time.LocalDate

data class ResidentFinancialStatus(
    val resident: Resident,
    val nextPaymentDate: String,
    val daysUntilNextPayment: Int,
    /** 'Paid' | 'Pending' | 'Overdue' */
    val overallStatus: String,
    val receipts: List<Receipt>,
    val pendingAmount: Double,
) {
    val isOverdue: Boolean get() = overallStatus == "Overdue"
    val isPaid: Boolean get() = overallStatus == "Paid"
}

/**
 * Port of the web `financesStore.getResidentFinancialStatus` business rules:
 * next payment date, countdown and overall status per resident.
 */
object ResidentFinancialStatusCalculator {

    fun calculate(
        residents: List<Resident>,
        receipts: List<Receipt>,
        today: LocalDate = LocalDate.now(),
    ): List<ResidentFinancialStatus> {
        val todayStr = Dates.localDateIsoString(java.util.Date.from(today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
        return residents.map { resident ->
            val own = receipts.filter { it.residentId == resident.id }
            var nextPaymentDate = parseDate(resident.admissionDate) ?: parseDate("2024-01-01")!!
            nextPaymentDate = nextPaymentDate.plusDays(30)

            if (own.isNotEmpty()) {
                val unpaid = own.filter { it.status != Receipt.STATUS_PAID }
                if (unpaid.isNotEmpty()) {
                    val oldest = unpaid.minOfOrNull { parseDate(it.dueDate) ?: today }
                    if (oldest != null) nextPaymentDate = oldest
                } else {
                    val latest = own.maxOfOrNull { parseDate(it.dueDate) ?: today }
                    if (latest != null) nextPaymentDate = latest.plusDays(30)
                }
            }

            val daysUntil = (nextPaymentDate.toEpochDay() - today.toEpochDay()).toInt()

            val hasOverdue = own.any {
                it.status == Receipt.STATUS_OVERDUE ||
                    (it.status == Receipt.STATUS_PENDING && todayStr >= it.dueDate)
            }
            val hasPending = own.any { it.status == Receipt.STATUS_PENDING }

            val overall = when {
                hasOverdue -> "Overdue"
                own.isEmpty() && daysUntil <= 0 -> "Overdue"
                hasPending -> "Pending"
                own.isNotEmpty() -> "Paid"
                else -> "Pending"
            }

            ResidentFinancialStatus(
                resident = resident,
                nextPaymentDate = nextPaymentDate.toString(),
                daysUntilNextPayment = daysUntil,
                overallStatus = overall,
                receipts = own,
                pendingAmount = own.filter { it.status != Receipt.STATUS_PAID }
                    .sumOf { it.amount + it.lateFee },
            )
        }
    }

    private fun parseDate(value: String?): LocalDate? = Dates.ymdOrNull(value)
}
