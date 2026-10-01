package com.example.buildingfexfrontend.finances.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.application.CollectionsUseCases
import com.example.buildingfexfrontend.finances.domain.model.AdminExpense
import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.PayoutSchedule
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.model.SharedUtilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CollectionsDialog { NONE, EXPENSE, SHARED_SERVICE, PAYOUT }

data class CollectionsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val expenses: List<AdminExpense> = emptyList(),
    val receipts: List<Receipt> = emptyList(),
    val sharedServices: List<SharedUtilityService> = emptyList(),
    val payouts: List<FixedPayoutRecipient> = emptyList(),
    val dialog: CollectionsDialog = CollectionsDialog.NONE,
    val busy: Boolean = false,
    // expense form
    val expenseName: String = "",
    val expenseAmount: String = "",
    val expenseDate: String = "",
    // shared service form
    val sharedType: String = "water",
    val sharedAmount: String = "",
    // payout form
    val payoutName: String = "",
    val payoutDni: String = "",
    val payoutPhone: String = "",
    val payoutSalary: String = "",
    val payoutInterval: String = "30",
    val message: String? = null,
) {
    /** Same totals as the web collections hero. */
    val grossCollected: Double
        get() = receipts.filter { it.status == Receipt.STATUS_PAID }.sumOf { it.amount + it.lateFee }

    val totalCosts: Double
        get() = expenses.sumOf { it.amount }

    val totalFixedPaid: Double
        get() = payouts.sumOf { row ->
            val fromHistory = row.paymentHistory.sumOf { it.amount }
            val extraPeriods = (PayoutSchedule.periodsSinceCreation(row, Dates.todayYmd()) -
                row.paymentHistory.size).coerceAtLeast(0)
            fromHistory + extraPeriods * row.salary
        }

    val totalSharedServices: Double
        get() = sharedServices.sumOf { it.amount }

    val balanceAfterAll: Double
        get() = grossCollected - totalCosts - totalSharedServices - totalFixedPaid
}

class CollectionsViewModel(private val useCases: CollectionsUseCases) : ViewModel() {

    private val _state = MutableStateFlow(CollectionsUiState())
    val state: StateFlow<CollectionsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val expenses = useCases.expenses()
                val receipts = useCases.receipts()
                val shared = useCases.sharedServices()
                val payouts = useCases.payouts()
                _state.update {
                    it.copy(
                        loading = false,
                        expenses = expenses,
                        receipts = receipts,
                        sharedServices = shared,
                        payouts = payouts,
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openDialog(dialog: CollectionsDialog) = _state.update {
        it.copy(
            dialog = dialog,
            expenseName = "",
            expenseAmount = "",
            expenseDate = Dates.todayYmd(),
            sharedType = "water",
            sharedAmount = "",
            payoutName = "",
            payoutDni = "",
            payoutPhone = "",
            payoutSalary = "",
            payoutInterval = "30",
        )
    }

    fun closeDialog() = _state.update { it.copy(dialog = CollectionsDialog.NONE) }

    fun onFieldChange(field: String, value: String) = _state.update {
        when (field) {
            "expenseName" -> it.copy(expenseName = value)
            "expenseAmount" -> it.copy(expenseAmount = value)
            "expenseDate" -> it.copy(expenseDate = value)
            "sharedType" -> it.copy(sharedType = value)
            "sharedAmount" -> it.copy(sharedAmount = value)
            "payoutName" -> it.copy(payoutName = value)
            "payoutDni" -> it.copy(payoutDni = value)
            "payoutPhone" -> it.copy(payoutPhone = value)
            "payoutSalary" -> it.copy(payoutSalary = value)
            "payoutInterval" -> it.copy(payoutInterval = value)
            else -> it
        }
    }

    fun submit() {
        val s = _state.value
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                when (s.dialog) {
                    CollectionsDialog.EXPENSE -> useCases.addExpense(
                        name = s.expenseName,
                        amount = s.expenseAmount.toDoubleOrNull() ?: Double.NaN,
                        purchaseDate = s.expenseDate,
                    )

                    CollectionsDialog.SHARED_SERVICE -> useCases.addSharedService(
                        type = s.sharedType,
                        amount = s.sharedAmount.toDoubleOrNull() ?: Double.NaN,
                    )

                    CollectionsDialog.PAYOUT -> useCases.addPayout(
                        name = s.payoutName,
                        dni = s.payoutDni,
                        phone = s.payoutPhone,
                        salary = s.payoutSalary.toDoubleOrNull() ?: Double.NaN,
                        intervalDays = s.payoutInterval.toIntOrNull() ?: 0,
                    )

                    CollectionsDialog.NONE -> Unit
                }
                reloadAfterSave()
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busy = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private suspend fun reloadAfterSave() {
        val expenses = runCatching { useCases.expenses() }.getOrDefault(emptyList())
        val receipts = runCatching { useCases.receipts() }.getOrDefault(emptyList())
        val shared = runCatching { useCases.sharedServices() }.getOrDefault(emptyList())
        val payouts = runCatching { useCases.payouts() }.getOrDefault(emptyList())
        _state.update {
            it.copy(
                busy = false,
                dialog = CollectionsDialog.NONE,
                expenses = expenses,
                receipts = receipts,
                sharedServices = shared,
                payouts = payouts,
                message = stringOf("coll.saved"),
            )
        }
    }
}
