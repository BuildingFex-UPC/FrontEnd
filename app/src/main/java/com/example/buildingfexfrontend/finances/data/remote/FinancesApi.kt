package com.example.buildingfexfrontend.finances.data.remote

import com.example.buildingfexfrontend.finances.domain.model.DashboardKpi
import com.example.buildingfexfrontend.finances.domain.model.FeeItem
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import com.example.buildingfexfrontend.finances.domain.model.MonthPoint
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.residents.domain.model.Resident
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class ReceiptDto(
    val id: String? = null,
    val residentId: String? = null,
    val issueDate: String? = null,
    val dueDate: String? = null,
    val amount: Double? = null,
    val status: String? = null,
    val lateFee: Double? = null,
    val extraCharges: Double? = null,
    val concept: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = Receipt(
        id = id?.toString(),
        residentId = residentId.orEmpty(),
        issueDate = issueDate.orEmpty(),
        dueDate = dueDate.orEmpty(),
        amount = amount ?: 0.0,
        status = status ?: Receipt.STATUS_PENDING,
        lateFee = lateFee ?: 0.0,
        extraCharges = extraCharges ?: 0.0,
        concept = concept.orEmpty(),
        ownerAdminId = ownerAdminId,
    )

    companion object {
        fun from(domain: Receipt) = ReceiptDto(
            id = domain.id,
            residentId = domain.residentId,
            issueDate = domain.issueDate,
            dueDate = domain.dueDate,
            amount = domain.amount,
            status = domain.status,
            lateFee = domain.lateFee,
            extraCharges = domain.extraCharges,
            concept = domain.concept,
            ownerAdminId = domain.ownerAdminId,
        )
    }
}

data class ReceiptPatch(
    val status: String? = null,
    val lateFee: Double? = null,
    val extraCharges: Double? = null,
    val concept: String? = null,
    val amount: Double? = null,
    val dueDate: String? = null,
)

data class FeeDto(
    val id: String? = null,
    val residentId: String? = null,
    val month: String? = null,
    val amount: Double? = null,
    val dueDate: String? = null,
    val status: String? = null,
    val concept: String? = null,
    val ownerAdminId: String? = null,
)

data class PaymentDto(
    val id: String? = null,
    val residentId: String? = null,
    val feeId: String? = null,
    val feeMonth: String? = null,
    val concept: String? = null,
    val amount: Double? = null,
    val paidAt: String? = null,
    val method: String? = null,
    val reference: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = Payment(
        id = id?.toString(),
        residentId = residentId.orEmpty(),
        feeId = feeId,
        feeMonth = feeMonth,
        concept = concept,
        amount = amount ?: 0.0,
        paidAt = paidAt.orEmpty(),
        method = method ?: "Mercado Pago",
        reference = reference,
        ownerAdminId = ownerAdminId,
    )

    companion object {
        fun from(domain: Payment) = PaymentDto(
            id = domain.id,
            residentId = domain.residentId,
            feeId = domain.feeId,
            feeMonth = domain.feeMonth,
            concept = domain.concept,
            amount = domain.amount,
            paidAt = domain.paidAt,
            method = domain.method,
            reference = domain.reference,
            ownerAdminId = domain.ownerAdminId,
        )
    }
}

data class SettingsDto(
    val id: String? = null,
    val baseMonthlyExpense: Double? = null,
    val lateFeeRate: Double? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = FinanceSettings(
        baseMonthlyExpense = baseMonthlyExpense ?: 150.0,
        lateFeeRate = lateFeeRate ?: 0.05,
    )
}

data class MonthPointDto(
    val monthKey: String? = null,
    val income: Double? = null,
    val expenses: Double? = null,
)

data class KpiDto(
    val totalResidents: Double? = null,
    val occupiedUnits: Double? = null,
    val emptyUnits: Double? = null,
    val totalDebt: Double? = null,
    val totalPendingDebt: Double? = null,
    val monthlyChart: List<MonthPointDto>? = null,
) {
    fun toDomain() = DashboardKpi(
        totalResidents = (totalResidents ?: 0.0).toInt(),
        occupiedUnits = (occupiedUnits ?: 0.0).toInt(),
        emptyUnits = (emptyUnits ?: 0.0).toInt(),
        totalDebt = totalDebt ?: totalPendingDebt ?: 0.0,
        monthlyChart = monthlyChart.orEmpty().map {
            MonthPoint(
                monthKey = it.monthKey.orEmpty(),
                income = it.income ?: 0.0,
                expenses = it.expenses ?: 0.0,
            )
        },
    )
}

data class ResidentUserDto(
    val id: String? = null,
    val name: String? = null,
    val floor: String? = null,
    val code: String? = null,
    val email: String? = null,
    val password: String? = null,
    val admissionDate: String? = null,
) {
    fun toResident() = Resident(
        id = id.orEmpty(),
        name = name.orEmpty(),
        floor = floor.orEmpty(),
        code = code.orEmpty(),
        email = email.orEmpty(),
        hasCredentials = !email.isNullOrBlank() && !password.isNullOrBlank(),
        admissionDate = admissionDate,
    )
}

/** Finances bounded-context endpoints (json-server style resources). */
interface FinancesApi {

    @GET("financeSettings")
    suspend fun settings(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<SettingsDto>

    @POST("financeSettings")
    suspend fun createSettings(@Body body: SettingsDto): SettingsDto

    @PATCH("financeSettings")
    suspend fun patchSettings(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
        @Body body: SettingsDto,
    ): SettingsDto

    @GET("users")
    suspend fun residents(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<ResidentUserDto>

    @GET("receipts")
    suspend fun receipts(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<ReceiptDto>

    @POST("receipts")
    suspend fun createReceipt(@Body body: ReceiptDto): ReceiptDto

    @PATCH("receipts/{id}")
    suspend fun patchReceipt(@Path("id") id: String, @Body body: ReceiptPatch): ReceiptDto

    @GET("fees")
    suspend fun fees(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<FeeDto>

    @PATCH("fees/{id}")
    suspend fun patchFee(@Path("id") id: String, @Body body: ReceiptPatch): FeeDto

    @GET("payments")
    suspend fun payments(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<PaymentDto>

    @POST("payments")
    suspend fun createPayment(@Body body: PaymentDto): PaymentDto

    @GET("kpi")
    suspend fun kpis(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<KpiDto>
}

/**
 * Maps the combined receipts + fees rows into the unified resident fee view.
 * Residents are only charged the base quota + late fee (shared-service extras
 * never count here), and a fee row duplicating a receipt's due date is skipped
 * so the same period is never billed twice.
 */
fun mapFees(receipts: List<ReceiptDto>, fees: List<FeeDto>, residentId: String): List<FeeItem> {
    val mappedReceipts = receipts.map { dto ->
        FeeItem(
            id = "receipt-${dto.id}",
            originalId = dto.id.orEmpty(),
            type = "receipt",
            residentId = dto.residentId.orEmpty(),
            month = com.example.buildingfexfrontend.core.util.Dates.monthYearLabel(dto.dueDate),
            amount = (dto.amount ?: 0.0) + (dto.lateFee ?: 0.0),
            dueDate = dto.dueDate.orEmpty(),
            status = if (dto.status == Receipt.STATUS_PAID) "Pagado" else "Pendiente",
            concept = dto.concept?.takeIf { it.isNotBlank() } ?: "Mantenimiento",
        )
    }
    val receiptDueDates = receipts
        .mapNotNull { dto -> dto.dueDate?.takeIf { it.isNotBlank() }?.take(10) }
        .toSet()
    val mappedFees = fees
        .filter { dto -> dto.dueDate.orEmpty().take(10) !in receiptDueDates }
        .map { dto ->
            FeeItem(
                id = "fee-${dto.id}",
                originalId = dto.id.orEmpty(),
                type = "fee",
                residentId = dto.residentId.orEmpty(),
                month = dto.month ?: "Mes actual",
                amount = dto.amount ?: 0.0,
                dueDate = dto.dueDate.orEmpty(),
                status = dto.status ?: "Pendiente",
                concept = dto.concept?.takeIf { it.isNotBlank() } ?: "Cuota mantenimiento",
            )
        }
    val combined = mappedReceipts + mappedFees
    return combined.sortedByDescending { it.dueDate }
}
