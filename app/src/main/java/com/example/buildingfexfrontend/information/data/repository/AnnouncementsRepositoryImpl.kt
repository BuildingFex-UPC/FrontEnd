package com.example.buildingfexfrontend.information.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.information.data.remote.AnnouncementsApi
import com.example.buildingfexfrontend.information.data.remote.toDto
import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.information.domain.model.NewAnnouncement
import com.example.buildingfexfrontend.information.domain.repository.AnnouncementsRepository
import java.time.Instant
import java.time.ZoneId

class AnnouncementsRepositoryImpl(
    private val api: AnnouncementsApi,
    private val session: SessionRepository,
) : AnnouncementsRepository {

    override suspend fun list(): List<Announcement> = apiCall {
        api.list(
            ownerParams(session, mapOf("_sort" to "createdAt", "_order" to "desc")),
        ).map { it.toDomain() }
    }

    override suspend fun add(
        announcement: NewAnnouncement,
        authorId: String,
        authorName: String,
    ): Announcement = apiCall {
        val createdAt = Dates.nowIso()
        val expiresAt = java.time.OffsetDateTime.now(ZoneId.systemDefault())
            .plusDays(announcement.duration.toLong())
            .toString()
        api.add(
            announcement.toDto(
                id = "ann-${System.currentTimeMillis()}",
                createdAt = createdAt,
                expiresAt = expiresAt,
                authorId = authorId,
                authorName = authorName,
                ownerAdminId = session.activeDataOwnerId,
            ),
        ).toDomain()
    }

    override suspend fun remove(id: String) = apiCall { api.remove(id) }
}
