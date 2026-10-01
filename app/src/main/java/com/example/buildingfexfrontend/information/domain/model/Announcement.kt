package com.example.buildingfexfrontend.information.domain.model

/**
 * Information bounded-context entity: an official announcement (comunicado).
 * `duration` = days the announcement stays active (7 | 15 | 30).
 */
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val priority: String = "normal",
    val authorId: String? = null,
    val authorName: String = "",
    val createdAt: String,
    val duration: Int = 7,
    val expiresAt: String,
    val ownerAdminId: String? = null,
) {
    val isActive: Boolean
        get() = try {
            java.time.Instant.now() < java.time.Instant.parse(expiresAt)
        } catch (e: Exception) {
            true
        }
}

data class NewAnnouncement(
    val title: String,
    val body: String,
    val priority: String = "normal",
    val duration: Int = 7,
)
