package com.example.buildingfexfrontend.finances.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.finances.application.ResidentAccount
import com.example.buildingfexfrontend.finances.application.ResidentFinanceUseCases
import com.example.buildingfexfrontend.finances.domain.repository.PaymentCheckout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResidentFinanceUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val account: ResidentAccount? = null,
    val startingCheckout: Boolean = false,
    val confirming: Boolean = false,
    val openCheckoutUrl: String? = null,
    val message: String? = null,
)

class ResidentFinanceViewModel(
    private val useCases: ResidentFinanceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ResidentFinanceUiState())
    val state: StateFlow<ResidentFinanceUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                runCatching { useCases.autoApplyLateFees() }
                val account = useCases.account()
                _state.update { it.copy(loading = false, account = account) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    /** Starts Mercado Pago Checkout Pro and hands the URL to the screen. */
    fun startCheckout() {
        _state.update { it.copy(startingCheckout = true, message = null) }
        viewModelScope.launch {
            try {
                val checkout = useCases.checkout()
                if (checkout.demo) {
                    val confirmation = useCases.confirm(paymentId = null, demo = true)
                    useCases.recordPayment(
                        reference = "demo-${System.currentTimeMillis()}",
                        paidAt = confirmation.paidAt,
                    )
                    refresh(stringOf("rfin.msg.demoConfirmed"))
                } else if (!checkout.initPoint.isNullOrBlank()) {
                    _state.update {
                        it.copy(
                            startingCheckout = false,
                            openCheckoutUrl = checkout.initPoint,
                            message = stringOf("rfin.msg.checkoutOpened"),
                        )
                    }
                } else {
                    _state.update {
                        it.copy(startingCheckout = false, message = stringOf("rfin.msg.checkoutFailed"))
                    }
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(startingCheckout = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    /**
     * Called when Mercado Pago redirects back into the in-app checkout WebView.
     * Extracts `payment_id` (or legacy `collection_id`) from the back_url query
     * and confirms the payment, mirroring the web SPA behavior.
     */
    fun onCheckoutRedirect(uri: Uri) {
        _state.update { it.copy(openCheckoutUrl = null) }
        val payment = uri.getQueryParameter("payment")
        val status = uri.getQueryParameter("status")
        val paymentId = uri.getQueryParameter("payment_id")
            ?: uri.getQueryParameter("collection_id")
        when {
            payment == "failure" -> _state.update {
                it.copy(message = stringOf("rfin.msg.paymentFailed"))
            }

            payment == "pending" || status == "pending" || status == "in_process" -> _state.update {
                it.copy(message = stringOf("rfin.msg.paymentUnderReview"))
            }

            !paymentId.isNullOrBlank() -> confirmWithPayment(paymentId)

            payment == "success" -> refresh(stringOf("rfin.msg.paymentUnderReview"))

            else -> refresh(stringOf("rfin.msg.reconcileSoon"))
        }
    }

    private fun confirmWithPayment(paymentId: String?) {
        _state.update { it.copy(confirming = true, message = null) }
        viewModelScope.launch {
            try {
                val confirmation = useCases.confirm(paymentId = paymentId, demo = false)
                useCases.recordPayment(
                    reference = confirmation.paidAt ?: "mp-${System.currentTimeMillis()}",
                    paidAt = confirmation.paidAt,
                )
                val suffix = if (confirmation.reconciled) {
                    stringOf("rfin.msg.itemsReconciled").replace("{n}", confirmation.itemsPaid.toString())
                } else {
                    stringOf("rfin.msg.reconcileSoon")
                }
                refresh(stringOf("rfin.msg.paymentConfirmed").replace("{suffix}", suffix))
            } catch (e: Throwable) {
                _state.update {
                    it.copy(confirming = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun consumeCheckoutUrl() = _state.update { it.copy(openCheckoutUrl = null) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private fun refresh(message: String) {
        viewModelScope.launch {
            val account = runCatching { useCases.account() }.getOrNull()
            _state.update {
                it.copy(confirming = false, startingCheckout = false, account = account, message = message)
            }
        }
    }
}
