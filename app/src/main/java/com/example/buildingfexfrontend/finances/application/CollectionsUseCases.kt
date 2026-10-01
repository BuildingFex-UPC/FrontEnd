package com.example.buildingfexfrontend.finances.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.finances.domain.model.AdminExpense
import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.model.SharedUtilityService
import com.example.buildingfexfrontend.finances.domain.repository.CollectionsRepository

/** Recaudación y gastos de gestión use cases. */
class CollectionsUseCases(
    private val repository: CollectionsRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val expenses = ListExpensesUseCase(repository)
    val receipts = ListCollectionsReceiptsUseCase(repository)
    val addExpense = AddExpenseUseCase(repository)
    val sharedServices = ListSharedServicesUseCase(repository)
    val addSharedService = AddSharedServiceUseCase(repository)
    val payouts = ListPayoutsUseCase(repository)
    val addPayout = AddPayoutUseCase(repository)
}

class ListExpensesUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(): List<AdminExpense> = repository.listExpenses()
}

class ListCollectionsReceiptsUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(): List<Receipt> = repository.listReceipts()
}

class AddExpenseUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(
        name: String,
        amount: Double,
        purchaseDate: String,
        invoicePhotoUrl: String = "",
    ): AdminExpense {
        if (name.isBlank() || amount.isNaN() || amount < 0 || purchaseDate.isBlank()) {
            throw AppException(
                "ADMIN_EXPENSE_FIELDS_REQUIRED",
                "Nombre, un monto válido y la fecha de compra son obligatorios.",
            )
        }
        return repository.addExpense(name.trim(), amount, purchaseDate.trim(), invoicePhotoUrl.trim())
    }
}

class ListSharedServicesUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(): List<SharedUtilityService> = repository.listSharedServices()
}

class AddSharedServiceUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(type: String, amount: Double): SharedUtilityService {
        if (type !in setOf("water", "electricity") || amount.isNaN() || amount < 0) {
            throw AppException(
                "SHARED_SERVICE_FIELDS_REQUIRED",
                "Selecciona un tipo de servicio y un monto válidos.",
            )
        }
        return repository.addSharedService(type, amount)
    }
}

class ListPayoutsUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(): List<FixedPayoutRecipient> = repository.listPayouts()
}

class AddPayoutUseCase(private val repository: CollectionsRepository) {
    suspend operator fun invoke(
        name: String,
        dni: String,
        phone: String,
        salary: Double,
        intervalDays: Int,
    ): FixedPayoutRecipient {
        if (name.isBlank() || dni.isBlank() || phone.isBlank() ||
            salary.isNaN() || salary < 0 || intervalDays < 1
        ) {
            throw AppException(
                "FIXED_PAYOUT_FIELDS_REQUIRED",
                "Nombre, DNI, teléfono, salario e intervalo en días son obligatorios.",
            )
        }
        return repository.addPayout(name.trim(), dni.trim(), phone.trim(), salary, intervalDays)
    }
}
