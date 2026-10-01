package com.example.buildingfexfrontend.core.domain.model

/**
 * IAM ubiquitous language: the two roles a session can carry.
 */
enum class SessionRole { ADMIN, RESIDENT }

/**
 * Public projection of a user row (mirrors `publicUser` in the web frontend).
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val role: String? = null,
    val floor: String? = null,
    val code: String? = null,
    val ownerAdminId: String? = null,
    val dni: String? = null,
    val address: String? = null,
    val company: String? = null,
    val ruc: String? = null,
    val admissionDate: String? = null,
) {
    val isAdmin: Boolean get() = role == "admin"
    val isResident: Boolean get() = role == "resident"
}

/**
 * Authenticated session persisted across app restarts.
 */
data class Session(
    val role: SessionRole,
    val profile: UserProfile,
    val token: String,
) {
    /**
     * Active data owner for multi-tenant API queries (admin account id).
     * Admins scope by their own id, residents by the admin that owns the building.
     */
    val activeDataOwnerId: String?
        get() = when (role) {
            SessionRole.ADMIN -> profile.id.takeIf { it.isNotBlank() }
            SessionRole.RESIDENT -> profile.ownerAdminId?.takeIf { it.isNotBlank() }
        }
}
