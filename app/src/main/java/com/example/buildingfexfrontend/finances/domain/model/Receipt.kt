package com.example.buildingfexfrontend.finances.domain.model

/**
 * Receipt (boleta de mantenimiento) entity.
 * status: 'Pending' | 'Paid' | 'Overdue'
 */
data class Receipt(
    val id: String? = null,
    val residentId: String,
    val issueDate: String = "",
    val dueDate: String,
    val amount: Double = 0.0,
    val status: String = STATUS_PENDING,
    val lateFee: Double = 0.0,
    val extraCharges: Double = 0.0,
    val concept: String = "",
    val ownerAdminId: String? = null,
) {
    fun isOverdue(): Boolean = com.example.buildingfexfrontend.core.util.Dates.isPastDue(dueDate)

    fun totalAmount(): Double = amount + lateFee + extraCharges

    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_PAID = "Paid"
        const val STATUS_OVERDUE = "Overdue"

        /**
         * Tag appended to `concept` when an admin waives the mora. It is
         * persisted server-side so automatic late fees never re-charge a
         * forgiven receipt (data value: never translated).
         */
        const val MORA_WAIVED_MARKER = "mora perdonada"
    }
}

data class FeeItem(
    val id: String,
    val originalId: String,
    val type: String,
    val residentId: String,
    val month: String,
    val amount: Double,
    val dueDate: String,
    val status: String,
    val concept: String,
) {
    val isPaid: Boolean get() = status == "Pagado"
}

data class FinanceSettings(
    val baseMonthlyExpense: Double = 150.0,
    val lateFeeRate: Double = 0.05,
)

data class Payment(
    val id: String? = null,
    val residentId: String,
    val feeId: String? = null,
    val feeMonth: String? = null,
    val concept: String? = null,
    val amount: Double = 0.0,
    val paidAt: String = "",
    val method: String = "Mercado Pago",
    val reference: String? = null,
    val ownerAdminId: String? = null,
)

data class MonthPoint(val monthKey: String, val income: Double, val expenses: Double)

data class DashboardKpi(
    val totalResidents: Int = 0,
    val occupiedUnits: Int = 0,
    val emptyUnits: Int = 0,
    val totalDebt: Double = 0.0,
    val monthlyChart: List<MonthPoint> = emptyList(),
)
