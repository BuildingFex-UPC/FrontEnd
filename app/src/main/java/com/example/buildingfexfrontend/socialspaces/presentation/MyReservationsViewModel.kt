package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.socialspaces.application.GuestInviteLink
import com.example.buildingfexfrontend.socialspaces.application.ReservationsUseCases
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.domain.model.GuestInput
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.ReservationRules
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val reservations: List<Reservation> = emptyList(),
    val spacesById: Map<String, Space> = emptyMap(),
    val expandedId: String? = null,
    val guestsDialogId: String? = null,
    val guestsDraft: List<GuestInput> = emptyList(),
    val savingGuests: Boolean = false,
    val guestsError: String? = null,
    val message: String? = null,
    val nowMs: Long = System.currentTimeMillis(),
)

/** Resident "Mi generación": my reservations, guest list editing and invite link. */
class MyReservationsViewModel(
    private val reservations: ReservationsUseCases,
    private val spaces: SpacesUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(MyReservationsUiState())
    val state: StateFlow<MyReservationsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = reservations.myReservations()
                val spaceList = spaces.list()
                _state.update {
                    it.copy(
                        loading = false,
                        reservations = list.sortedWith(
                            compareByDescending<Reservation> { it.date }
                                .thenByDescending { it.startTime },
                        ),
                        spacesById = spaceList.mapNotNull { s -> s.id?.let { id -> id to s } }.toMap(),
                        nowMs = System.currentTimeMillis(),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun toggleExpand(id: String) = _state.update {
        it.copy(expandedId = if (it.expandedId == id) null else id)
    }

    fun openGuests(reservation: Reservation) {
        val id = reservation.id ?: return
        _state.update {
            it.copy(
                guestsDialogId = id,
                guestsError = null,
                guestsDraft = List(ReservationRules.MAX_GUESTS) { index ->
                    val guest = reservation.guests.getOrNull(index)
                    GuestInput(id = guest?.id.orEmpty(), name = guest?.name.orEmpty())
                },
            )
        }
    }

    fun closeGuests() = _state.update {
        it.copy(guestsDialogId = null, guestsError = null, savingGuests = false)
    }

    fun onGuestChange(index: Int, value: String) = _state.update { s ->
        if (index !in s.guestsDraft.indices) return@update s
        s.copy(
            guestsDraft = s.guestsDraft.toMutableList().also {
                it[index] = it[index].copy(name = value)
            },
        )
    }

    fun saveGuests() {
        val s = _state.value
        val id = s.guestsDialogId ?: return
        _state.update { it.copy(savingGuests = true, guestsError = null) }
        viewModelScope.launch {
            try {
                val payload = s.guestsDraft
                    .filter { it.name.isNotBlank() }
                    .take(ReservationRules.MAX_GUESTS)
                val updated = reservations.updateGuests(id, payload)
                _state.update { cur ->
                    cur.copy(
                        savingGuests = false,
                        guestsDialogId = null,
                        reservations = cur.reservations.map { r -> if (r.id == updated.id) updated else r },
                        message = stringOf("resv.guestsUpdated"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(savingGuests = false, guestsError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun shareText(reservation: Reservation): String {
        val token = reservation.guestInviteToken.orEmpty()
        val url = GuestInviteLink.build(token)
        return if (url.isNotEmpty()) {
            stringOf("resv.shareUrl").replace("{url}", url)
        } else {
            stringOf("resv.shareToken").replace("{token}", token.trim())
        }
    }

    /** Absolute invite URL (also encoded in the shareable QR image). */
    fun shareUrl(reservation: Reservation): String =
        GuestInviteLink.build(reservation.guestInviteToken.orEmpty())

    fun inviteExpired(reservation: Reservation): Boolean =
        ReservationRules.isInviteExpired(reservation.date, reservation.startTime, _state.value.nowMs)

    fun refreshNow() = _state.update { it.copy(nowMs = System.currentTimeMillis()) }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
