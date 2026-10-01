package com.example.buildingfexfrontend.information.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.information.domain.model.NewAnnouncement
import com.example.buildingfexfrontend.information.domain.repository.AnnouncementsRepository

class AnnouncementsUseCases(
    private val repository: AnnouncementsRepository,
    private val session: SessionRepository,
) {
    val list = ListAnnouncementsUseCase(repository)
    val publish = PublishAnnouncementUseCase(repository, session)
    val remove = RemoveAnnouncementUseCase(repository)
}

class ListAnnouncementsUseCase(private val repository: AnnouncementsRepository) {
    suspend operator fun invoke(): List<Announcement> = repository.list()
}

class PublishAnnouncementUseCase(
    private val repository: AnnouncementsRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(title: String, body: String, priority: String, duration: Int): Announcement {
        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        if (cleanTitle.isBlank() || cleanBody.isBlank()) {
            throw AppException("ANNOUNCEMENT_FIELDS_REQUIRED", "Título y contenido requeridos")
        }
        val profile = session.current()?.profile
        return repository.add(
            announcement = NewAnnouncement(
                title = cleanTitle,
                body = cleanBody,
                priority = priority,
                duration = duration,
            ),
            authorId = profile?.id.orEmpty(),
            authorName = profile?.name.orEmpty(),
        )
    }
}

class RemoveAnnouncementUseCase(private val repository: AnnouncementsRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
