package com.example.buildingfexfrontend.finances.domain.repository

/** Result of starting a Mercado Pago Checkout Pro session. */
data class PaymentCheckout(
    val preferenceId: String?,
    val initPoint: String?,
    val demo: Boolean,
)

data class PaymentConfirmation(
    val reconciled: Boolean,
    val itemsPaid: Int,
    val paidAt: String?,
)

/** Outbound port for the Mercado Pago payment provider. */
interface PaymentsGateway {

    suspend fun checkoutMaintenanceFees(residentId: String, payerEmail: String?): PaymentCheckout

    suspend fun confirmMaintenancePayment(
        residentId: String,
        paymentId: String?,
        demo: Boolean,
    ): PaymentConfirmation
}
