package com.example.buildingfexfrontend.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.application.ResidentAccount
import com.example.buildingfexfrontend.finances.application.ResidentFinanceUseCases
import com.example.buildingfexfrontend.information.application.AnnouncementsUseCases
import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.incidents.application.IncidentsUseCases
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.socialspaces.application.ReservationsUseCases
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ResidentFinanceSummary(
    val paid: Double = 0.0,
    val pending: Double = 0.0,
    val overdue: Double = 0.0,
) {
    val total: Double get() = paid + pending
}

data class ResidentDashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val account: ResidentAccount? = null,
    val announcements: List<Announcement> = emptyList(),
    val incidents: List<Incident> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val spacesById: Map<String, Space> = emptyMap(),
) {
    val financeSummary: ResidentFinanceSummary
        get() {
            val fees = account?.fees.orEmpty()
            val paid = fees.filter { it.isPaid }.sumOf { it.amount }
            val pendingFees = fees.filter { !it.isPaid }
            val pending = pendingFees.sumOf { it.amount }
            val today = LocalDate.now()
            val overdue = pendingFees
                .filter { fee -> Dates.ymdOrNull(fee.dueDate)?.isBefore(today) == true }
                .sumOf { it.amount }
            return ResidentFinanceSummary(paid = paid, pending = pending, overdue = overdue)
        }

    val activeAnnouncements: List<Announcement>
        get() = announcements.filter { it.isActive }

    val todayReservations: List<Reservation>
        get() = reservations.filter { it.date == Dates.todayYmd() }

    val upcomingReservations: List<Reservation>
        get() = reservations
            .filter { it.date > Dates.todayYmd() }
            .sortedWith(compareBy({ it.date }, { it.startTime }))

    val openIncidents: Int get() = incidents.count { it.status == "open" }
    val inProgressIncidents: Int get() = incidents.count { it.status == "in-progress" }
    val resolvedIncidents: Int get() = incidents.count { it.status == "resolved" }
}

/** Resident dashboard: finance KPIs, announcements, incidents and reservations. */
class ResidentDashboardViewModel(
    private val finance: ResidentFinanceUseCases,
    private val announcements: AnnouncementsUseCases,
    private val incidents: IncidentsUseCases,
    private val reservations: ReservationsUseCases,
    private val spaces: SpacesUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ResidentDashboardUiState())
    val state: StateFlow<ResidentDashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val (account, ann, inc, res, spaceList) = coroutineScope {
                    val accountDeferred = async { finance.account() }
                    val announcementsDeferred = async { runCatching { announcements.list() }.getOrDefault(emptyList()) }
                    val incidentsDeferred = async { runCatching { incidents.listMine() }.getOrDefault(emptyList()) }
                    val reservationsDeferred = async { runCatching { reservations.myReservations() }.getOrDefault(emptyList()) }
                    val spacesDeferred = async { runCatching { spaces.list() }.getOrDefault(emptyList()) }
                    Quint(
                        accountDeferred.await(),
                        announcementsDeferred.await(),
                        incidentsDeferred.await(),
                        reservationsDeferred.await(),
                        spacesDeferred.await(),
                    )
                }
                _state.update {
                    it.copy(
                        loading = false,
                        account = account,
                        announcements = ann,
                        incidents = inc,
                        reservations = res.sortedWith(
                            compareBy({ it.date }, { it.startTime }),
                        ),
                        spacesById = spaceList.mapNotNull { s -> s.id?.let { id -> id to s } }.toMap(),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)
