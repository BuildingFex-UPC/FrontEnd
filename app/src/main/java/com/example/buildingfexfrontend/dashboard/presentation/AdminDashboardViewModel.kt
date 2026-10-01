package com.example.buildingfexfrontend.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.finances.application.CollectionsUseCases
import com.example.buildingfexfrontend.finances.application.FinanceUseCases
import com.example.buildingfexfrontend.finances.domain.model.DashboardKpi
import com.example.buildingfexfrontend.incidents.application.IncidentsUseCases
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ChartData(
    val labels: List<String> = emptyList(),
    val income: List<Double> = emptyList(),
    val expenses: List<Double> = emptyList(),
) {
    val hasData: Boolean
        get() = income.any { it > 0 } || expenses.any { it > 0 }
}

data class AdminDashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val kpi: DashboardKpi = DashboardKpi(),
    val chart: ChartData = ChartData(),
    val recentIncidents: List<Incident> = emptyList(),
)

/** Admin dashboard: KPIs, 6-month income/expense chart and recent incidents. */
class AdminDashboardViewModel(
    private val finances: FinanceUseCases,
    private val collections: CollectionsUseCases,
    private val incidents: IncidentsUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val (kpi, chart, recent) = coroutineScope {
                    val kpiDeferred = async { finances.kpis() }
                    val incidentsDeferred = async {
                        incidents.listAll()
                            .sortedByDescending { it.createdAt.orEmpty() }
                            .take(5)
                    }
                    val kpiValue = kpiDeferred.await()
                    val chartValue = buildChart(kpiValue)
                    Triple(kpiValue, chartValue, incidentsDeferred.await())
                }
                _state.update {
                    it.copy(loading = false, kpi = kpi, chart = chart, recentIncidents = recent)
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    private suspend fun buildChart(kpi: DashboardKpi): ChartData {
        val fromApi = chartFromMonthly(kpi.monthlyChart)
        if (fromApi.hasData) return fromApi
        return try {
            val payments = finances.allPayments()
            val expenses = collections.expenses()
            val receipts = finances.receipts()
            chartFromTransactions(payments, expenses, receipts)
        } catch (e: Exception) {
            ChartData()
        }
    }
}

private val MonthShortEs: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale("es", "PE"))
private val MonthShortEn: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)

private fun monthShortFormatter(): DateTimeFormatter =
    if (com.example.buildingfexfrontend.core.i18n.AppLanguage.current ==
        com.example.buildingfexfrontend.core.i18n.Language.EN
    ) {
        MonthShortEn
    } else {
        MonthShortEs
    }

private fun labelFromMonthKey(monthKey: String): String {
    val parsed = runCatching { YearMonth.parse(monthKey) }.getOrNull() ?: return monthKey
    return parsed.atDay(1).format(monthShortFormatter()).trimEnd('.')
}

private fun chartFromMonthly(rows: List<com.example.buildingfexfrontend.finances.domain.model.MonthPoint>): ChartData =
    ChartData(
        labels = rows.map { labelFromMonthKey(it.monthKey) },
        income = rows.map { it.income },
        expenses = rows.map { it.expenses },
    )

private fun chartFromTransactions(
    payments: List<com.example.buildingfexfrontend.finances.domain.model.Payment>,
    expenses: List<com.example.buildingfexfrontend.finances.domain.model.AdminExpense>,
    receipts: List<com.example.buildingfexfrontend.finances.domain.model.Receipt>,
): ChartData {
    val now = YearMonth.now()
    val months = (5 downTo 0).map { now.minusMonths(it.toLong()) }
    val labels = months.map { it.atDay(1).format(monthShortFormatter()).trimEnd('.') }
    val income = months.map { key ->
        val fromPayments = payments
            .filter { it.paidAt.take(7) == key.toString() }
            .sumOf { it.amount }
        if (fromPayments > 0) {
            fromPayments
        } else {
            receipts
                .filter { it.status == "Paid" && it.issueDate.take(7) == key.toString() }
                .sumOf { it.amount + it.lateFee + it.extraCharges }
        }
    }
    val expenseData = months.map { key ->
        expenses
            .filter { it.purchaseDate.take(7) == key.toString() }
            .sumOf { it.amount }
    }
    return ChartData(labels = labels, income = income, expenses = expenseData)
}
