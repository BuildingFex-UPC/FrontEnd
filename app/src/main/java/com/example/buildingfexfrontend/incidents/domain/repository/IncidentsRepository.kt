package com.example.buildingfexfrontend.incidents.domain.repository

import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentInput

interface IncidentsRepository {
    suspend fun list(): List<Incident>
    suspend fun add(incident: IncidentInput): Incident
    suspend fun update(incident: Incident): Incident
    suspend fun remove(id: String)
}
