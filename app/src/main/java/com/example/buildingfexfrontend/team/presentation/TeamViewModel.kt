package com.example.buildingfexfrontend.team.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.team.application.TeamUseCases
import com.example.buildingfexfrontend.team.domain.model.TeamWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val workers: List<TeamWorker> = emptyList(),
    // search
    val query: String = "",
    // editor
    val editorVisible: Boolean = false,
    val editingOriginal: TeamWorker? = null,
    val name: String = "",
    val phone: String = "",
    val dni: String = "",
    val salary: String = "",
    val photoUrl: String = "",
    val processingPhoto: Boolean = false,
    val saving: Boolean = false,
    val formError: String? = null,
    val message: String? = null,
    // delete
    val deleteTarget: TeamWorker? = null,
    val deleting: Boolean = false,
) {
    val filteredWorkers: List<TeamWorker>
        get() {
            val q = query.trim()
            if (q.isBlank()) return workers
            return workers.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.phone.contains(q, ignoreCase = true) ||
                    it.dni.contains(q, ignoreCase = true)
            }
        }
}

class TeamViewModel(private val useCases: TeamUseCases) : ViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val workers = useCases.list()
                _state.update { it.copy(loading = false, workers = workers) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun clearQuery() = _state.update { it.copy(query = "") }

    fun openEditor() = _state.update {
        it.copy(
            editorVisible = true,
            editingOriginal = null,
            name = "",
            phone = "",
            dni = "",
            salary = "",
            photoUrl = "",
            formError = null,
        )
    }

    fun openEditorFor(worker: TeamWorker) = _state.update {
        it.copy(
            editorVisible = true,
            editingOriginal = worker,
            name = worker.name,
            phone = worker.phone,
            dni = worker.dni,
            salary = if (worker.salary == 0.0) "" else worker.salary.toString(),
            photoUrl = worker.photoUrl,
            formError = null,
        )
    }

    fun closeEditor() = _state.update { it.copy(editorVisible = false, formError = null) }

    fun onFieldChange(field: String, value: String) = _state.update {
        when (field) {
            "name" -> it.copy(name = value)
            "phone" -> it.copy(phone = value)
            "dni" -> it.copy(dni = value)
            "salary" -> it.copy(salary = value)
            else -> it
        }.copy(formError = null)
    }

    fun onPhotoProcessing(value: Boolean) = _state.update { it.copy(processingPhoto = value) }

    fun setPhoto(dataUrl: String?) = _state.update {
        it.copy(photoUrl = dataUrl.orEmpty(), processingPhoto = false, formError = null)
    }

    fun photoProcessingFailed() = _state.update {
        it.copy(
            processingPhoto = false,
            formError = stringOf("team.msg.photoFailed"),
        )
    }

    fun clearPhoto() = _state.update { it.copy(photoUrl = "") }

    fun requestDelete(worker: TeamWorker) = _state.update { it.copy(deleteTarget = worker) }

    fun cancelDelete() = _state.update { it.copy(deleteTarget = null) }

    fun confirmDelete() {
        val target = _state.value.deleteTarget ?: return
        if (_state.value.deleting) return
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            try {
                useCases.remove(target.id)
                val workers = runCatching { useCases.list() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        deleting = false,
                        deleteTarget = null,
                        workers = workers,
                        message = stringOf("team.msg.removed"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        deleting = false,
                        deleteTarget = null,
                        error = AppException.unexpected(e).userMessage(),
                    )
                }
            }
        }
    }

    fun save() {
        val s = _state.value
        val salaryValue = s.salary.trim().replace(',', '.').toDoubleOrNull()
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                val original = s.editingOriginal
                if (original == null) {
                    useCases.add(
                        name = s.name,
                        phone = s.phone,
                        dni = s.dni,
                        salary = salaryValue ?: Double.NaN,
                        photoUrl = s.photoUrl,
                    )
                } else {
                    useCases.update(
                        original = original,
                        name = s.name,
                        phone = s.phone,
                        dni = s.dni,
                        salary = salaryValue ?: Double.NaN,
                        photoUrl = s.photoUrl,
                    )
                }
                val workers = runCatching { useCases.list() }.getOrDefault(s.workers)
                _state.update {
                    it.copy(
                        saving = false,
                        editorVisible = false,
                        editingOriginal = null,
                        workers = workers,
                        message = if (original == null) {
                            stringOf("team.msg.added")
                        } else {
                            stringOf("team.msg.updated")
                        },
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(saving = false, formError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
