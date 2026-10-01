package com.example.buildingfexfrontend.information.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.information.application.AnnouncementsUseCases
import com.example.buildingfexfrontend.information.domain.model.Announcement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InformationUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList(),
    val editorVisible: Boolean = false,
    val title: String = "",
    val body: String = "",
    val priority: String = "normal",
    val duration: Int = 7,
    val saving: Boolean = false,
    val formError: String? = null,
    val deleteTarget: Announcement? = null,
    val message: String? = null,
)

class InformationViewModel(private val useCases: AnnouncementsUseCases) : ViewModel() {

    private val _state = MutableStateFlow(InformationUiState())
    val state: StateFlow<InformationUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = useCases.list()
                _state.update { it.copy(loading = false, announcements = items) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openEditor() = _state.update {
        it.copy(
            editorVisible = true,
            title = "",
            body = "",
            priority = "normal",
            duration = 7,
            formError = null,
        )
    }

    fun closeEditor() = _state.update { it.copy(editorVisible = false, formError = null) }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, formError = null) }

    fun onBodyChange(value: String) = _state.update { it.copy(body = value, formError = null) }

    fun onPriorityChange(value: String) = _state.update { it.copy(priority = value) }

    fun onDurationChange(value: Int) = _state.update { it.copy(duration = value) }

    fun publish() {
        val s = _state.value
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                useCases.publish(s.title, s.body, s.priority, s.duration)
                val items = runCatching { useCases.list() }.getOrDefault(s.announcements)
                _state.update {
                    it.copy(
                        saving = false,
                        editorVisible = false,
                        announcements = items,
                        message = stringOf("info.msg.published"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(saving = false, formError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun openDelete(announcement: Announcement) =
        _state.update { it.copy(deleteTarget = announcement) }

    fun closeDelete() = _state.update { it.copy(deleteTarget = null) }

    fun confirmDelete() {
        val target = _state.value.deleteTarget ?: return
        viewModelScope.launch {
            try {
                useCases.remove(target.id)
                val items = runCatching { useCases.list() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        deleteTarget = null,
                        announcements = items,
                        message = stringOf("info.msg.removed"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(deleteTarget = null, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
