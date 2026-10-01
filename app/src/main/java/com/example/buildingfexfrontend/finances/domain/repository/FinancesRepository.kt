package com.example.buildingfexfrontend.finances.domain.repository

import com.example.buildingfexfrontend.finances.domain.model.DashboardKpi
import com.example.buildingfexfrontend.finances.domain.model.FeeItem
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.residents.domain.model.Resident

interface FinancesRepository {

    suspend fun getSettings(): FinanceSettings

    suspend fun updateSettings(settings: FinanceSettings): FinanceSettings

    suspend fun listResidents(): List<Resident>

    suspend fun listReceipts(): List<Receipt>

    suspend fun listReceiptsByResident(residentId: String): List<Receipt>

    suspend fun createReceipt(receipt: Receipt): Receipt

    suspend fun updateReceipt(id: String, patch: ReceiptPatchData): Receipt

    suspend fun listFees(residentId: String): List<FeeItem>

    suspend fun updateFeeStatus(feeId: String, status: String)

    suspend fun listPayments(residentId: String): List<Payment>

    suspend fun listAllPayments(): List<Payment>

    suspend fun addPayment(payment: Payment): Payment

    suspend fun getKpis(): DashboardKpi
}

/** Partial update payload for a receipt (nulls are omitted by Gson). */
data class ReceiptPatchData(
    val status: String? = null,
    val lateFee: Double? = null,
    val extraCharges: Double? = null,
    val concept: String? = null,
    val amount: Double? = null,
    val dueDate: String? = null,
)
