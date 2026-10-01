package com.example.buildingfexfrontend.information.domain.repository

import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.information.domain.model.NewAnnouncement

interface AnnouncementsRepository {
    suspend fun list(): List<Announcement>
    suspend fun add(announcement: NewAnnouncement, authorId: String, authorName: String): Announcement
    suspend fun remove(id: String)
}
