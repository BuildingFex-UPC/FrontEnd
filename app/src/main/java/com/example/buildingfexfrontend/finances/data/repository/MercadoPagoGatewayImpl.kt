package com.example.buildingfexfrontend.finances.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.finances.data.remote.CheckoutBody
import com.example.buildingfexfrontend.finances.data.remote.ConfirmBody
import com.example.buildingfexfrontend.finances.data.remote.PaymentsApi
import com.example.buildingfexfrontend.finances.domain.repository.PaymentCheckout
import com.example.buildingfexfrontend.finances.domain.repository.PaymentConfirmation
import com.example.buildingfexfrontend.finances.domain.repository.PaymentsGateway

class MercadoPagoGatewayImpl(
    private val api: PaymentsApi,
    private val session: SessionRepository,
) : PaymentsGateway {

    override suspend fun checkoutMaintenanceFees(
        residentId: String,
        payerEmail: String?,
    ): PaymentCheckout = apiCall {
        val dto = api.checkout(
            CheckoutBody(
                residentId = residentId,
                ownerAdminId = session.activeDataOwnerId,
                payerEmail = payerEmail,
            ),
        )
        PaymentCheckout(
            preferenceId = dto.preferenceId,
            initPoint = dto.initPoint,
            demo = dto.demo == true,
        )
    }

    override suspend fun confirmMaintenancePayment(
        residentId: String,
        paymentId: String?,
        demo: Boolean,
    ): PaymentConfirmation = apiCall {
        val dto = api.confirm(
            ConfirmBody(
                residentId = residentId,
                ownerAdminId = session.activeDataOwnerId,
                paymentId = paymentId,
                demo = demo,
            ),
        )
        PaymentConfirmation(
            reconciled = dto.reconciled == true,
            itemsPaid = dto.itemsPaid ?: 0,
            paidAt = dto.paidAt,
        )
    }
}
