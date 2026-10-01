package com.example.buildingfexfrontend.finances.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class CheckoutBody(
    val residentId: String,
    val ownerAdminId: String? = null,
    val payerEmail: String? = null,
)

data class CheckoutDto(
    val preferenceId: String? = null,
    val initPoint: String? = null,
    val demo: Boolean? = null,
)

data class ConfirmBody(
    val residentId: String,
    val ownerAdminId: String? = null,
    val paymentId: String? = null,
    val demo: Boolean = false,
)

data class ConfirmDto(
    val reconciled: Boolean? = null,
    val itemsPaid: Int? = null,
    val paidAt: String? = null,
)

/** Mercado Pago Checkout Pro endpoints. */
interface PaymentsApi {

    @POST("api/v1/payments/checkout")
    suspend fun checkout(@Body body: CheckoutBody): CheckoutDto

    @POST("api/v1/payments/confirm")
    suspend fun confirm(@Body body: ConfirmBody): ConfirmDto
}
