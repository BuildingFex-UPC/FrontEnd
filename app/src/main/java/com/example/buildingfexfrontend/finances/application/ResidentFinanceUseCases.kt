package com.example.buildingfexfrontend.finances.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.model.FeeItem
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.finances.domain.repository.FinancesRepository
import com.example.buildingfexfrontend.finances.domain.repository.PaymentCheckout
import com.example.buildingfexfrontend.finances.domain.repository.PaymentsGateway

data class ResidentAccount(
    val fees: List<FeeItem>,
    val payments: List<Payment>,
    val settings: FinanceSettings,
) {
    val pendingAmount: Double get() = fees.filter { !it.isPaid }.sumOf { it.amount }
    val paidTotal: Double get() = payments.sumOf { it.amount }
    val hasPending: Boolean get() = fees.any { !it.isPaid }
}

/** Resident-side finance use cases (fees, history and Mercado Pago checkout). */
class ResidentFinanceUseCases(
    private val repository: FinancesRepository,
    private val payments: PaymentsGateway,
    private val session: SessionRepository,
) {
    val account = ResidentAccountUseCase(repository, session)
    val checkout = StartCheckoutUseCase(payments, session)
    val confirm = ConfirmMaintenanceUseCase(payments, session)
    val recordPayment = RecordPaymentUseCase(repository, session)
    val markPaidOffline = MarkPendingFeesPaidUseCase(repository, session)
    val autoApplyLateFees = ResidentAutoApplyLateFeesUseCase(repository, session)
}

/**
 * Resident-scoped automatic mora: only this resident's receipts are touched
 * so they always see the correct amount as soon as a payment expires.
 */
class ResidentAutoApplyLateFeesUseCase(
    private val repository: FinancesRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): Int =
        AutoApplyLateFeesUseCase(repository)(residentId(session))
}

class ResidentAccountUseCase(
    private val repository: FinancesRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): ResidentAccount {
        val residentId = residentId(session)
        val fees = repository.listFees(residentId)
        val payments = repository.listPayments(residentId)
        val settings = repository.getSettings()
        return ResidentAccount(fees, payments, settings)
    }
}

class StartCheckoutUseCase(
    private val payments: PaymentsGateway,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(payerEmail: String? = null): PaymentCheckout {
        val residentId = residentId(session)
        return payments.checkoutMaintenanceFees(residentId, payerEmail)
    }
}

class ConfirmMaintenanceUseCase(
    private val payments: PaymentsGateway,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(paymentId: String? = null, demo: Boolean = false) =
        payments.confirmMaintenancePayment(residentId(session), paymentId, demo)
}

/**
 * Registers one payment row per pending fee after a successful checkout,
 * mirroring the web `recordPayment`.
 */
class RecordPaymentUseCase(
    private val repository: FinancesRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(reference: String?, paidAt: String?): List<Payment> {
        val residentId = residentId(session)
        val fees = repository.listFees(residentId)
        val pending = fees.filter { !it.isPaid }
        if (pending.isEmpty()) return emptyList()
        val timestamp = paidAt ?: Dates.nowIso()
        val recorded = mutableListOf<Payment>()
        pending.forEach { fee ->
            repository.updateFeeStatus(fee.id, "Pagado")
            val payment = repository.addPayment(
                Payment(
                    residentId = residentId,
                    feeId = fee.id,
                    feeMonth = fee.month,
                    concept = fee.concept,
                    amount = fee.amount,
                    paidAt = timestamp,
                    method = "Mercado Pago",
                    reference = reference,
                ),
            )
            recorded += payment
        }
        return recorded
    }
}

/** Local fallback used when the backend is unreachable after a payment. */
class MarkPendingFeesPaidUseCase(
    private val repository: FinancesRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): Int {
        val residentId = residentId(session)
        val pending = repository.listFees(residentId).filter { !it.isPaid }
        pending.forEach { repository.updateFeeStatus(it.id, "Pagado") }
        return pending.size
    }
}

private fun residentId(session: SessionRepository): String =
    session.current()?.profile?.id
        ?: throw AppException("RESIDENT_NOT_FOUND", "No resident session")
