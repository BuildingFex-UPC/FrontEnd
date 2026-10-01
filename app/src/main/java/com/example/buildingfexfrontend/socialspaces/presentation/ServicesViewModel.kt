package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.socialspaces.application.ReservationsUseCases
import com.example.buildingfexfrontend.socialspaces.application.SpacesUseCases
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServicesUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val spaces: List<Space> = emptyList(),
    val selectedSpace: Space? = null,
    val spaceReservations: List<Reservation> = emptyList(),
    val loadingReservations: Boolean = false,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val formError: String? = null,
    val submitting: Boolean = false,
    val message: String? = null,
)

/** Resident "Espacios comunes": browse spaces and reserve a slot (port of AppResidentServicesView). */
class ServicesViewModel(
    private val spaces: SpacesUseCases,
    private val reservations: ReservationsUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ServicesUiState())
    val state: StateFlow<ServicesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = spaces.list()
                _state.update { it.copy(loading = false, spaces = list) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun selectSpace(space: Space) {
        _state.update {
            it.copy(
                selectedSpace = space,
                date = "",
                startTime = "",
                endTime = "",
                formError = null,
                spaceReservations = emptyList(),
            )
        }
        loadSpaceReservations(space.id.orEmpty())
    }

    fun closeReserve() {
        if (_state.value.submitting) return
        _state.update { it.copy(selectedSpace = null, formError = null) }
    }

    fun onFieldChange(field: String, value: String) = _state.update {
        when (field) {
            "date" -> it.copy(date = value, formError = null)
            "startTime" -> it.copy(startTime = value, formError = null)
            "endTime" -> it.copy(endTime = value, formError = null)
            else -> it
        }
    }

    fun submit() {
        val s = _state.value
        val space = s.selectedSpace ?: return
        _state.update { it.copy(submitting = true, formError = null) }
        viewModelScope.launch {
            try {
                reservations.reserve(
                    spaceId = space.id.orEmpty(),
                    date = s.date,
                    startTime = s.startTime,
                    endTime = s.endTime,
                )
                _state.update {
                    it.copy(
                        submitting = false,
                        selectedSpace = null,
                        message = stringOf("services.reservedMessage")
                            .replace("{name}", space.name),
                    )
                }
            } catch (e: Throwable) {
                val appError = AppException.unexpected(e)
                _state.update {
                    it.copy(submitting = false, formError = appError.userMessage())
                }
                if (appError.code == "RESERVATION_OVERLAP") {
                    loadSpaceReservations(space.id.orEmpty())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private fun loadSpaceReservations(spaceId: String) {
        if (spaceId.isBlank()) return
        _state.update { it.copy(loadingReservations = true) }
        viewModelScope.launch {
            try {
                val list = reservations.spaceReservations(spaceId)
                _state.update { it.copy(loadingReservations = false, spaceReservations = list) }
            } catch (e: Throwable) {
                _state.update { it.copy(loadingReservations = false, spaceReservations = emptyList()) }
            }
        }
    }
}
