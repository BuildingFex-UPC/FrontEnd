package com.example.buildingfexfrontend.team.domain.repository

import com.example.buildingfexfrontend.team.domain.model.TeamWorker

interface TeamRepository {
    suspend fun list(): List<TeamWorker>
    suspend fun add(worker: TeamWorker): TeamWorker

    /**
     * The backend exposes GET/POST/DELETE only (no PUT): an update is a
     * delete + re-create that preserves the original id, with a best-effort
     * rollback if the re-create fails.
     */
    suspend fun update(original: TeamWorker, updated: TeamWorker): TeamWorker

    suspend fun remove(id: String)
}
