package com.example.buildingfexfrontend.finances.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.finances.data.remote.FeeDto
import com.example.buildingfexfrontend.finances.data.remote.FinancesApi
import com.example.buildingfexfrontend.finances.data.remote.PaymentDto
import com.example.buildingfexfrontend.finances.data.remote.ReceiptDto
import com.example.buildingfexfrontend.finances.data.remote.ReceiptPatch
import com.example.buildingfexfrontend.finances.data.remote.SettingsDto
import com.example.buildingfexfrontend.finances.data.remote.mapFees
import com.example.buildingfexfrontend.finances.domain.model.DashboardKpi
import com.example.buildingfexfrontend.finances.domain.model.FeeItem
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.repository.FinancesRepository
import com.example.buildingfexfrontend.finances.domain.repository.ReceiptPatchData
import com.example.buildingfexfrontend.residents.domain.model.Resident

class FinancesRepositoryImpl(
    private val api: FinancesApi,
    private val session: SessionRepository,
) : FinancesRepository {

    override suspend fun getSettings(): FinanceSettings = apiCall {
        try {
            val rows = api.settings(ownerParams(session))
            rows.firstOrNull()?.toDomain() ?: FinanceSettings()
        } catch (e: AppException) {
            if (e.status == 404) FinanceSettings() else throw e
        }
    }

    override suspend fun updateSettings(settings: FinanceSettings): FinanceSettings = apiCall {
        val body = SettingsDto(
            baseMonthlyExpense = settings.baseMonthlyExpense,
            lateFeeRate = settings.lateFeeRate,
            ownerAdminId = session.activeDataOwnerId,
        )
        try {
            api.patchSettings(ownerParams(session), body).toDomain()
        } catch (e: AppException) {
            if (e.status == 404) {
                api.createSettings(body).toDomain()
            } else {
                throw e
            }
        }
    }

    override suspend fun listResidents(): List<Resident> = apiCall {
        api.residents(ownerParams(session, mapOf("role" to "resident")))
            .map { it.toResident() }
            .sortedBy { it.code }
    }

    override suspend fun listReceipts(): List<Receipt> = apiCall {
        api.receipts(ownerParams(session)).map { it.toDomain() }
    }

    override suspend fun listReceiptsByResident(residentId: String): List<Receipt> = apiCall {
        api.receipts(ownerParams(session, mapOf("residentId" to residentId))).map { it.toDomain() }
    }

    override suspend fun createReceipt(receipt: Receipt): Receipt = apiCall {
        api.createReceipt(
            ReceiptDto.from(receipt).copy(ownerAdminId = session.activeDataOwnerId),
        ).toDomain()
    }

    override suspend fun updateReceipt(id: String, patch: ReceiptPatchData): Receipt = apiCall {
        api.patchReceipt(
            id,
            ReceiptPatch(
                status = patch.status,
                lateFee = patch.lateFee,
                extraCharges = patch.extraCharges,
                concept = patch.concept,
                amount = patch.amount,
                dueDate = patch.dueDate,
            ),
        ).toDomain()
    }

    override suspend fun listFees(residentId: String): List<FeeItem> = apiCall {
        val receipts = api.receipts(ownerParams(session, mapOf("residentId" to residentId)))
        val fees = api.fees(ownerParams(session, mapOf("residentId" to residentId)))
        mapFees(receipts, fees, residentId)
    }

    override suspend fun updateFeeStatus(feeId: String, status: String): Unit = apiCall {
        when {
            feeId.startsWith("receipt-") -> {
                val realId = feeId.removePrefix("receipt-")
                val statusEn = if (status == "Pagado") Receipt.STATUS_PAID else Receipt.STATUS_PENDING
                api.patchReceipt(realId, ReceiptPatch(status = statusEn))
                Unit
            }

            feeId.startsWith("fee-") -> {
                val realId = feeId.removePrefix("fee-")
                api.patchFee(realId, ReceiptPatch(status = status))
                Unit
            }

            else -> Unit
        }
    }

    override suspend fun listPayments(residentId: String): List<Payment> = apiCall {
        api.payments(ownerParams(session, mapOf("residentId" to residentId))).map { it.toDomain() }
    }

    override suspend fun listAllPayments(): List<Payment> = apiCall {
        api.payments(ownerParams(session)).map { it.toDomain() }
    }

    override suspend fun addPayment(payment: Payment): Payment = apiCall {
        api.createPayment(
            PaymentDto.from(payment).copy(ownerAdminId = session.activeDataOwnerId),
        ).toDomain()
    }

    override suspend fun getKpis(): DashboardKpi = apiCall {
        try {
            api.kpis(ownerParams(session)).firstOrNull()?.toDomain() ?: DashboardKpi()
        } catch (e: AppException) {
            if (e.status == 404) DashboardKpi() else throw e
        }
    }
}
