package com.example.buildingfexfrontend.team.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.team.data.remote.TeamApi
import com.example.buildingfexfrontend.team.data.remote.TeamWorkerDto
import com.example.buildingfexfrontend.team.domain.model.TeamWorker
import com.example.buildingfexfrontend.team.domain.repository.TeamRepository

class TeamRepositoryImpl(
    private val api: TeamApi,
    private val session: SessionRepository,
) : TeamRepository {

    override suspend fun list(): List<TeamWorker> = apiCall {
        api.list(ownerParams(session)).map { it.toDomain() }
    }

    override suspend fun add(worker: TeamWorker): TeamWorker = apiCall {
        create(id = "worker-${System.currentTimeMillis()}", worker = worker)
    }

    override suspend fun update(original: TeamWorker, updated: TeamWorker): TeamWorker = apiCall {
        val id = original.id
        api.remove(id)
        try {
            create(id = id, worker = updated)
        } catch (e: Throwable) {
            // Best effort: restore the previous row so a failed update never loses data.
            runCatching { create(id = id, worker = original) }
            throw e
        }
    }

    override suspend fun remove(id: String): Unit = apiCall {
        api.remove(id)
    }

    private suspend fun create(id: String, worker: TeamWorker): TeamWorker = api.add(
        TeamWorkerDto(
            id = id,
            name = worker.name,
            phone = worker.phone,
            dni = worker.dni,
            salary = worker.salary,
            photoUrl = worker.photoUrl,
            ownerAdminId = session.activeDataOwnerId,
        ),
    ).toDomain()
}
