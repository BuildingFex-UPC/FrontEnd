package com.example.buildingfexfrontend.imports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.imports.application.ImportsUseCases
import com.example.buildingfexfrontend.imports.domain.model.ImportUpload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val uploads: List<ImportUpload> = emptyList(),
    val uploading: Boolean = false,
    val message: String? = null,
)

class ImportsViewModel(private val useCases: ImportsUseCases) : ViewModel() {

    private val _state = MutableStateFlow(ImportsUiState())
    val state: StateFlow<ImportsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = useCases.list()
                _state.update { it.copy(loading = false, uploads = items) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun upload(fileName: String, mimeType: String, bytes: ByteArray) {
        _state.update { it.copy(uploading = true, message = null) }
        viewModelScope.launch {
            try {
                useCases.upload(fileName, mimeType, bytes)
                val items = runCatching { useCases.list() }.getOrDefault(_state.value.uploads)
                _state.update {
                    it.copy(
                        uploading = false,
                        uploads = items,
                        message = stringOf("imports.msg.uploaded"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(uploading = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            try {
                useCases.remove(id)
                val items = runCatching { useCases.list() }.getOrDefault(_state.value.uploads)
                _state.update { it.copy(uploads = items, message = stringOf("imports.msg.removed")) }
            } catch (e: Throwable) {
                _state.update { it.copy(message = AppException.unexpected(e).userMessage()) }
            }
        }
    }

    fun decode(dataUrl: String): ByteArray? = useCases.decode(dataUrl)

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
