package com.example.buildingfexfrontend.iam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.iam.application.AuthUseCases
import com.example.buildingfexfrontend.core.util.InviteCodes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthTab { LOGIN, REGISTER }

data class AuthUiState(
    val tab: AuthTab = AuthTab.LOGIN,
    val loading: Boolean = false,
    val error: String? = null,
    // login
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    // register
    val regName: String = "",
    val regEmail: String = "",
    val regPassword: String = "",
    val regDni: String = "",
    val regAddress: String = "",
    val regCompany: String = "",
    val regRuc: String = "",
    // resident invite (credentials activation)
    val inviteMode: Boolean = false,
    val inviteCode: String = "",
    val inviteLoading: Boolean = false,
    val inviteError: String? = null,
    val inviteResident: UserProfile? = null,
    val inviteEmail: String = "",
    val invitePassword: String = "",
)

class AuthViewModel(private val auth: AuthUseCases) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var lookupJob: Job? = null

    init {
        auth.ensureValidSession()
    }

    fun onTabChange(tab: AuthTab) = _state.update { it.copy(tab = tab, error = null) }

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, emailError = null, error = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, passwordError = null, error = null) }

    fun onRegFieldChange(field: String, value: String) = _state.update { state ->
        when (field) {
            "name" -> state.copy(regName = value)
            "email" -> state.copy(regEmail = value)
            "password" -> state.copy(regPassword = value)
            "dni" -> state.copy(regDni = value)
            "address" -> state.copy(regAddress = value)
            "company" -> state.copy(regCompany = value)
            "ruc" -> state.copy(regRuc = value)
            else -> state
        }.copy(error = null)
    }

    fun openInviteMode() {
        lookupJob?.cancel()
        _state.update {
            it.copy(inviteMode = true, inviteError = null, error = null, inviteResident = null)
        }
    }

    fun closeInviteMode() {
        lookupJob?.cancel()
        _state.update {
            it.copy(inviteMode = false, inviteError = null, inviteResident = null, inviteCode = "")
        }
    }

    fun onInviteCodeChange(value: String) {
        val trimmed = value.trim()
        if (InviteCodes.looksLikeMessage(trimmed)) {
            // Pasted the whole shared message: keep only the code and search right away.
            val code = InviteCodes.extract(trimmed)
            if (code.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        inviteCode = trimmed,
                        inviteError = stringOf("auth.invite.no_code"),
                    )
                }
                return
            }
            _state.update { it.copy(inviteCode = code, inviteError = null) }
            scheduleLookup(code, immediate = true)
            return
        }
        _state.update { it.copy(inviteCode = trimmed, inviteError = null) }
        scheduleLookup(trimmed, immediate = false)
    }

    fun onInviteEmailChange(value: String) =
        _state.update { it.copy(inviteEmail = value, inviteError = null) }

    fun onInvitePasswordChange(value: String) =
        _state.update { it.copy(invitePassword = value, inviteError = null) }

    fun login() {
        val current = _state.value
        val emailError = if (!isValidEmail(current.email)) stringOf("auth.error.invalid_email") else null
        val passwordError = if (current.password.isBlank()) stringOf("auth.error.password_required") else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        launch {
            auth.login(current.email, current.password)
        }
    }

    fun registerAdmin() {
        val s = _state.value
        val emailError = if (!isValidEmail(s.regEmail)) stringOf("auth.error.invalid_email") else null
        val passwordError = if (s.regPassword.length < 6) stringOf("auth.error.min_6") else null
        val nameError = if (s.regName.isBlank()) stringOf("auth.error.name_required") else null
        if (emailError != null || passwordError != null || nameError != null) {
            _state.update { it.copy(error = null) }
            _state.update {
                it.copy(
                    emailError = null,
                    passwordError = null,
                    error = listOfNotNull(nameError, emailError, passwordError).joinToString(" "),
                )
            }
            return
        }
        launch {
            auth.registerAdmin(
                name = s.regName,
                email = s.regEmail,
                password = s.regPassword,
                dni = s.regDni,
                address = s.regAddress,
                company = s.regCompany,
                ruc = s.regRuc,
            )
        }
    }

    fun lookupInvite() {
        val raw = _state.value.inviteCode
        val code = InviteCodes.extract(raw) ?: raw.trim()
        if (code.isBlank()) {
            _state.update { it.copy(inviteError = stringOf("auth.error.invite_code_required")) }
            return
        }
        _state.update { it.copy(inviteCode = code) }
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch { runLookup(code) }
    }

    /** Debounced search while typing; pasted messages search immediately. */
    private fun scheduleLookup(code: String, immediate: Boolean) {
        lookupJob?.cancel()
        if (!immediate && !InviteCodes.canAutoSearch(code)) return
        lookupJob = viewModelScope.launch {
            if (!immediate) delay(450)
            runLookup(code)
        }
    }

    private suspend fun runLookup(code: String) {
        _state.update { it.copy(inviteLoading = true, inviteError = null) }
        try {
            val resident = auth.findInvite(code)
            _state.update { it.copy(inviteLoading = false, inviteResident = resident) }
        } catch (e: Throwable) {
            val appError = AppException.unexpected(e)
            val message = if (appError.code == "RESIDENT_NOT_FOUND") {
                stringOf("auth.error.resident_not_found").replace("{code}", code)
            } else {
                appError.userMessage()
            }
            _state.update { it.copy(inviteLoading = false, inviteError = message) }
        }
    }

    fun acceptInvite() {
        val s = _state.value
        val resident = s.inviteResident ?: return
        val emailError = if (!isValidEmail(s.inviteEmail)) stringOf("auth.error.invalid_email") else null
        val passwordError = if (s.invitePassword.length < 6) stringOf("auth.error.min_6") else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(inviteError = listOfNotNull(emailError, passwordError).joinToString(" ")) }
            return
        }
        _state.update { it.copy(loading = true, inviteError = null) }
        viewModelScope.launch {
            try {
                auth.setResidentCredentials(resident.code ?: s.inviteCode, s.inviteEmail, s.invitePassword)
                _state.update { it.copy(loading = false) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, inviteError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _state.update { it.copy(loading = false) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    companion object {
        fun isValidEmail(email: String): Boolean =
            Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email.trim())
    }
}
