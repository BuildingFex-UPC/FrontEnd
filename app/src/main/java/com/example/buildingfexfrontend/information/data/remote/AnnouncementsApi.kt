package com.example.buildingfexfrontend.information.data.remote

import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.information.domain.model.NewAnnouncement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class AnnouncementDto(
    val id: String? = null,
    val title: String? = null,
    val body: String? = null,
    val priority: String? = null,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: String? = null,
    val duration: Int? = null,
    val expiresAt: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = Announcement(
        id = id.orEmpty(),
        title = title.orEmpty(),
        body = body.orEmpty(),
        priority = priority ?: "normal",
        authorId = authorId,
        authorName = authorName.orEmpty(),
        createdAt = createdAt ?: "",
        duration = duration ?: 7,
        expiresAt = expiresAt ?: createdAt.orEmpty(),
        ownerAdminId = ownerAdminId,
    )
}

interface AnnouncementsApi {

    @GET("announcements")
    suspend fun list(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<AnnouncementDto>

    @POST("announcements")
    suspend fun add(@Body body: AnnouncementDto): AnnouncementDto

    @DELETE("announcements/{id}")
    suspend fun remove(@Path("id") id: String)
}

fun NewAnnouncement.toDto(
    id: String,
    createdAt: String,
    expiresAt: String,
    authorId: String?,
    authorName: String,
    ownerAdminId: String?,
) = AnnouncementDto(
    id = id,
    title = title,
    body = body,
    priority = priority,
    authorId = authorId,
    authorName = authorName,
    createdAt = createdAt,
    duration = duration,
    expiresAt = expiresAt,
    ownerAdminId = ownerAdminId,
)
