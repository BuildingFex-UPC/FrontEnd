package com.example.buildingfexfrontend.incidents.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentInput
import com.example.buildingfexfrontend.incidents.domain.model.IncidentStatus
import com.example.buildingfexfrontend.incidents.domain.repository.IncidentsRepository

class IncidentsUseCases(
    private val repository: IncidentsRepository,
    private val session: SessionRepository,
) {
    val listAll = ListIncidentsUseCase(repository)
    val listMine = ListMyIncidentsUseCase(repository, session)
    val create = CreateIncidentUseCase(repository)
    val report = ReportIncidentUseCase(repository, session)
    val update = UpdateIncidentUseCase(repository)
    val remove = RemoveIncidentUseCase(repository)
}

/** Admin creates incidents directly (no resident attached). */
class CreateIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(description: String, status: String, provider: String): Incident {
        val clean = description.trim()
        if (clean.isBlank()) {
            throw AppException("INCIDENT_DESCRIPTION_REQUIRED", "Descripción requerida")
        }
        return repository.add(
            IncidentInput(description = clean, status = status, provider = provider.trim()),
        )
    }
}

class ListIncidentsUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(): List<Incident> = repository.list()
}

class ListMyIncidentsUseCase(
    private val repository: IncidentsRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): List<Incident> {
        val myId = session.current()?.profile?.id ?: return emptyList()
        return repository.list().filter { it.residentId == myId }
    }
}

class ReportIncidentUseCase(
    private val repository: IncidentsRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(description: String): Incident {
        val clean = description.trim()
        if (clean.isBlank()) {
            throw AppException("INCIDENT_DESCRIPTION_REQUIRED", "Descripción requerida")
        }
        val profile = session.current()?.profile
            ?: throw AppException("RESIDENT_NOT_FOUND", "No resident session")
        val input = IncidentInput(
            description = clean,
            status = IncidentStatus.OPEN,
            provider = "",
            residentId = profile.id,
            residentName = profile.name,
        )
        return repository.add(input)
    }
}

class UpdateIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(incident: Incident): Incident {
        if (incident.description.isBlank()) {
            throw AppException("INCIDENT_DESCRIPTION_REQUIRED", "Descripción requerida")
        }
        return repository.update(incident)
    }
}

class RemoveIncidentUseCase(private val repository: IncidentsRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
