package com.example.buildingfexfrontend.finances.data.remote

import com.example.buildingfexfrontend.finances.domain.model.AdminExpense
import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.PayoutEntry
import com.example.buildingfexfrontend.finances.domain.model.SharedUtilityService
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class AdminExpenseDto(
    val id: String? = null,
    val name: String? = null,
    val amount: Double? = null,
    val purchaseDate: String? = null,
    val invoicePhotoUrl: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = AdminExpense(
        id = id.orEmpty(),
        name = name.orEmpty(),
        amount = amount ?: 0.0,
        purchaseDate = purchaseDate.orEmpty(),
        invoicePhotoUrl = invoicePhotoUrl.orEmpty(),
    )
}

data class SharedServiceDto(
    val id: String? = null,
    val type: String? = null,
    val amount: Double? = null,
    val month: String? = null,
    val residentCount: Int? = null,
    val perResidentShare: Double? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = SharedUtilityService(
        id = id.orEmpty(),
        type = type.orEmpty(),
        amount = amount ?: 0.0,
        month = month.orEmpty(),
        residentCount = residentCount ?: 0,
        perResidentShare = perResidentShare ?: 0.0,
    )
}

data class PayoutEntryDto(
    val id: String? = null,
    val paidOn: String? = null,
    val amount: Double? = null,
) {
    fun toDomain() = PayoutEntry(
        id = id.orEmpty(),
        paidOn = paidOn.orEmpty(),
        amount = amount ?: 0.0,
    )
}

data class FixedPayoutDto(
    val id: String? = null,
    val name: String? = null,
    val dni: String? = null,
    val phone: String? = null,
    val salary: Double? = null,
    val intervalDays: Int? = null,
    val nextPaymentDate: String? = null,
    val photoUrl: String? = null,
    val paymentHistory: List<PayoutEntryDto>? = null,
    val createdAt: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = FixedPayoutRecipient(
        id = id.orEmpty(),
        name = name.orEmpty(),
        dni = dni.orEmpty(),
        phone = phone.orEmpty(),
        salary = salary ?: 0.0,
        intervalDays = intervalDays ?: 30,
        nextPaymentDate = nextPaymentDate.orEmpty(),
        photoUrl = photoUrl.orEmpty(),
        paymentHistory = paymentHistory.orEmpty().map { it.toDomain() },
        createdAt = createdAt.orEmpty(),
    )

    fun from(domain: FixedPayoutRecipient) = copy(
        id = domain.id,
        name = domain.name,
        dni = domain.dni,
        phone = domain.phone,
        salary = domain.salary,
        intervalDays = domain.intervalDays,
        nextPaymentDate = domain.nextPaymentDate,
        photoUrl = domain.photoUrl,
        paymentHistory = domain.paymentHistory.map {
            PayoutEntryDto(id = it.id, paidOn = it.paidOn, amount = it.amount)
        },
        createdAt = domain.createdAt,
    )
}

/** Collections bounded-context endpoints. */
interface CollectionsApi {

    @GET("adminManagementExpenses")
    suspend fun expenses(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<AdminExpenseDto>

    @POST("adminManagementExpenses")
    suspend fun createExpense(@Body body: AdminExpenseDto): AdminExpenseDto

    @GET("sharedUtilityServices")
    suspend fun sharedServices(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<SharedServiceDto>

    @POST("sharedUtilityServices")
    suspend fun createSharedService(@Body body: SharedServiceDto): SharedServiceDto

    @GET("fixedPayoutRecipients")
    suspend fun payouts(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<FixedPayoutDto>

    @POST("fixedPayoutRecipients")
    suspend fun createPayout(@Body body: FixedPayoutDto): FixedPayoutDto

    @PATCH("fixedPayoutRecipients/{id}")
    suspend fun patchPayout(@Path("id") id: String, @Body body: FixedPayoutDto): FixedPayoutDto

    // --- resources shared by the shared-services distribution rule ---
    @GET("users")
    suspend fun residents(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<ResidentUserDto>

    @GET("receipts")
    suspend fun receipts(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<ReceiptDto>

    @PATCH("receipts/{id}")
    suspend fun patchReceipt(@Path("id") id: String, @Body body: ReceiptPatch): ReceiptDto

    @POST("receipts")
    suspend fun createReceipt(@Body body: ReceiptDto): ReceiptDto
}
