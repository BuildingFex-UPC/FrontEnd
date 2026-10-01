package com.example.buildingfexfrontend.finances.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.data.remote.AdminExpenseDto
import com.example.buildingfexfrontend.finances.data.remote.CollectionsApi
import com.example.buildingfexfrontend.finances.data.remote.FixedPayoutDto
import com.example.buildingfexfrontend.finances.data.remote.SharedServiceDto
import com.example.buildingfexfrontend.finances.domain.model.AdminExpense
import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.PayoutSchedule
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.model.SharedUtilityService
import com.example.buildingfexfrontend.finances.domain.repository.CollectionsRepository
import kotlin.math.roundToInt

class CollectionsRepositoryImpl(
    private val api: CollectionsApi,
    private val session: SessionRepository,
) : CollectionsRepository {

    override suspend fun listReceipts(): List<Receipt> = apiCall {
        api.receipts(ownerParams(session)).map { it.toDomain() }
    }

    override suspend fun listExpenses(): List<AdminExpense> = apiCall {
        api.expenses(ownerParams(session)).map { it.toDomain() }
            .sortedByDescending { it.purchaseDate }
    }

    override suspend fun addExpense(
        name: String,
        amount: Double,
        purchaseDate: String,
        invoicePhotoUrl: String,
    ): AdminExpense = apiCall {
        api.createExpense(
            AdminExpenseDto(
                id = "admin-exp-${System.currentTimeMillis()}",
                name = name,
                amount = amount,
                purchaseDate = purchaseDate,
                invoicePhotoUrl = invoicePhotoUrl,
                ownerAdminId = session.activeDataOwnerId,
            ),
        ).toDomain()
    }

    override suspend fun listSharedServices(): List<SharedUtilityService> = apiCall {
        api.sharedServices(ownerParams(session)).map { it.toDomain() }
    }

    /**
     * Records the service like the web `sharedUtilityServicesApi.add` (month +
     * per-resident split kept for reference). The share is intentionally NOT
     * attached to any receipt: residents are only charged the base quota.
     */
    override suspend fun addSharedService(type: String, amount: Double): SharedUtilityService =
        apiCall {
            val residents = api.residents(ownerParams(session, mapOf("role" to "resident")))
            val residentCount = residents.size
            val share = if (residentCount > 0) round2(amount / residentCount) else 0.0

            api.createSharedService(
                SharedServiceDto(
                    id = "shared-svc-${System.currentTimeMillis()}",
                    type = type,
                    amount = amount,
                    month = currentMonthPrefix(),
                    residentCount = residentCount,
                    perResidentShare = share,
                    ownerAdminId = session.activeDataOwnerId,
                ),
            ).toDomain()
        }

    override suspend fun listPayouts(): List<FixedPayoutRecipient> = apiCall {
        val rows = api.payouts(ownerParams(session)).map { it.toDomain() }
        val today = Dates.todayYmd()
        val patched = rows.mapNotNull { row -> PayoutSchedule.flushDue(row, today) }
        patched.forEach { updated ->
            api.patchPayout(updated.id, FixedPayoutDto().from(updated))
        }
        if (patched.isNotEmpty()) {
            api.payouts(ownerParams(session)).map { it.toDomain() }
        } else {
            rows
        }
    }

    override suspend fun addPayout(
        name: String,
        dni: String,
        phone: String,
        salary: Double,
        intervalDays: Int,
    ): FixedPayoutRecipient = apiCall {
        api.createPayout(
            FixedPayoutDto().from(
                FixedPayoutRecipient(
                    id = "fp-${System.currentTimeMillis()}",
                    name = name,
                    dni = dni,
                    phone = phone,
                    salary = salary,
                    intervalDays = intervalDays,
                    nextPaymentDate = PayoutSchedule.addDays(Dates.todayYmd(), intervalDays),
                    createdAt = Dates.nowIso(),
                ),
            ).copy(ownerAdminId = session.activeDataOwnerId),
        ).toDomain()
    }

    private fun currentMonthPrefix(): String = Dates.todayYmd().take(7)

    private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
}
