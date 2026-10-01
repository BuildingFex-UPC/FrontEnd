package com.example.buildingfexfrontend.core.domain.repository

import com.example.buildingfexfrontend.core.domain.model.Session
import com.example.buildingfexfrontend.core.domain.model.SessionRole
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract of the IAM session store: single source of truth for the JWT,
 * the current role and the active multi-tenant data owner.
 */
interface SessionRepository {

    /** Emits on every login / logout so the shell can react. */
    val session: StateFlow<Session?>

    fun current(): Session?

    val accessToken: String?

    val activeDataOwnerId: String?

    fun role(): SessionRole?

    /** Returns false when the JWT is missing, malformed or expired. */
    fun isTokenValid(): Boolean

    suspend fun signIn(role: SessionRole, profile: UserProfile, token: String)

    suspend fun updateProfile(profile: UserProfile)

    fun clear()
}
