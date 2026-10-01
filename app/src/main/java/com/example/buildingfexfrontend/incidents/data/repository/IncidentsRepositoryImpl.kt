package com.example.buildingfexfrontend.incidents.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.incidents.data.remote.IncidentDto
import com.example.buildingfexfrontend.incidents.data.remote.IncidentsApi
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentInput
import com.example.buildingfexfrontend.incidents.domain.repository.IncidentsRepository

class IncidentsRepositoryImpl(
    private val api: IncidentsApi,
    private val session: SessionRepository,
) : IncidentsRepository {

    override suspend fun list(): List<Incident> = apiCall {
        api.list(ownerParams(session))
            .map { it.toDomain() }
            .sortedByDescending { it.createdAt ?: "" }
    }

    override suspend fun add(incident: IncidentInput): Incident = apiCall {
        api.add(IncidentDto.from(incident, session.activeDataOwnerId)).toDomain()
    }

    override suspend fun update(incident: Incident): Incident = apiCall {
        val payload = IncidentDto(
            residentId = incident.residentId,
            residentName = incident.residentName,
            description = incident.description,
            status = incident.status,
            provider = incident.provider,
            ownerAdminId = session.activeDataOwnerId,
        )
        api.update(incident.id, payload).toDomain()
    }

    override suspend fun remove(id: String) = apiCall { api.remove(id) }
}
