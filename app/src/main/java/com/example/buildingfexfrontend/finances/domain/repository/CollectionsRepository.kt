package com.example.buildingfexfrontend.finances.domain.repository

import com.example.buildingfexfrontend.finances.domain.model.AdminExpense
import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.model.SharedUtilityService

interface CollectionsRepository {

    suspend fun listExpenses(): List<AdminExpense>

    /** Receipts are needed to compute the collections summary (recaudado bruto). */
    suspend fun listReceipts(): List<Receipt>

    suspend fun addExpense(
        name: String,
        amount: Double,
        purchaseDate: String,
        invoicePhotoUrl: String,
    ): AdminExpense

    suspend fun listSharedServices(): List<SharedUtilityService>

    suspend fun addSharedService(type: String, amount: Double): SharedUtilityService

    /** Applies due payouts (persisting the new schedule) before returning. */
    suspend fun listPayouts(): List<FixedPayoutRecipient>

    suspend fun addPayout(
        name: String,
        dni: String,
        phone: String,
        salary: Double,
        intervalDays: Int,
    ): FixedPayoutRecipient
}
