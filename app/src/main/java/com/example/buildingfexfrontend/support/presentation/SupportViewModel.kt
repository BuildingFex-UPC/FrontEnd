package com.example.buildingfexfrontend.support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.support.application.SupportUseCases
import com.example.buildingfexfrontend.support.domain.model.Faq
import com.example.buildingfexfrontend.support.domain.model.SupportChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupportUiState(
    val adminMode: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
    val faqs: List<Faq> = emptyList(),
    val expandedFaqId: Int? = null,
    val chats: List<SupportChat> = emptyList(),
    val activeChat: SupportChat? = null,
    val input: String = "",
    val sending: Boolean = false,
    val starting: Boolean = false,
    val topic: String = stringOf("sup.defaultTopic"),
    val message: String? = null,
)

class ResidentSupportViewModel(private val useCases: SupportUseCases) : ViewModel() {

    private val _state = MutableStateFlow(SupportUiState())
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val faqs = useCases.faqs()
                val chats = runCatching { useCases.myChats() }.getOrDefault(emptyList())
                _state.update { it.copy(loading = false, faqs = faqs, chats = chats) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun toggleFaq(id: Int) = _state.update {
        it.copy(expandedFaqId = if (it.expandedFaqId == id) null else id)
    }

    fun onTopicChange(value: String) = _state.update { it.copy(topic = value) }

    fun onInputChanged(value: String) = _state.update { it.copy(input = value) }

    fun startChat() {
        _state.update { it.copy(starting = true, message = null) }
        viewModelScope.launch {
            try {
                val chat = useCases.startChat(_state.value.topic)
                val chats = runCatching { useCases.myChats() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(starting = false, chats = chats, activeChat = chat, input = "")
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(starting = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openChat(chatId: String) {
        _state.update { it.copy(sending = false) }
        viewModelScope.launch {
            try {
                val chat = useCases.openChat(chatId)
                _state.update { it.copy(activeChat = chat, input = "") }
            } catch (e: Throwable) {
                _state.update { it.copy(message = AppException.unexpected(e).userMessage()) }
            }
        }
    }

    fun closeChat() = _state.update { it.copy(activeChat = null) }

    fun send() {
        val chatId = _state.value.activeChat?.id ?: return
        _state.update { it.copy(sending = true, message = null) }
        viewModelScope.launch {
            try {
                val updated = useCases.sendResidentMessage(chatId, _state.value.input)
                val chats = runCatching { useCases.myChats() }.getOrDefault(_state.value.chats)
                _state.update {
                    it.copy(sending = false, activeChat = updated, chats = chats, input = "")
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(sending = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}

class AdminSupportViewModel(private val useCases: SupportUseCases) : ViewModel() {

    private val _state = MutableStateFlow(SupportUiState(adminMode = true))
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val chats = useCases.allChats()
                _state.update { it.copy(loading = false, chats = chats) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun onInputChanged(value: String) = _state.update { it.copy(input = value) }

    fun openChat(chatId: String) {
        viewModelScope.launch {
            try {
                val chat = useCases.openChat(chatId)
                _state.update { it.copy(activeChat = chat, input = "") }
            } catch (e: Throwable) {
                _state.update { it.copy(message = AppException.unexpected(e).userMessage()) }
            }
        }
    }

    fun closeChat() = _state.update { it.copy(activeChat = null) }

    fun send() {
        val chatId = _state.value.activeChat?.id ?: return
        _state.update { it.copy(sending = true, message = null) }
        viewModelScope.launch {
            try {
                val updated = useCases.sendAdminMessage(chatId, _state.value.input)
                val chats = runCatching { useCases.allChats() }.getOrDefault(_state.value.chats)
                _state.update {
                    it.copy(sending = false, activeChat = updated, chats = chats, input = "")
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(sending = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
