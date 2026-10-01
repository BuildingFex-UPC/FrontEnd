package com.example.buildingfexfrontend.iam.application

import com.example.buildingfexfrontend.core.domain.model.SessionRole
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.iam.domain.repository.AuthRepository

/**
 * IAM use cases. They own the "who is signed in" decision: the repository
 * returns credentials, the use case persists the session with the right role.
 */
class AuthUseCases(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    val login = LoginUseCase(repository, session)
    val registerAdmin = RegisterAdminUseCase(repository, session)
    val isEmailRegistered = IsEmailRegisteredUseCase(repository)
    val findInvite = FindResidentInviteUseCase(repository)
    val setResidentCredentials = SetResidentCredentialsUseCase(repository, session)
    val refreshProfile = RefreshProfileUseCase(repository, session)
    val logout = LogoutUseCase(session)
    val ensureValidSession = EnsureValidSessionUseCase(session)
}

class LoginUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(email: String, password: String): UserProfile {
        val (profile, token) = repository.login(email.trim().lowercase(), password)
        session.signIn(profile.toSessionRole(), profile, token)
        return profile
    }
}

class RegisterAdminUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        dni: String,
        address: String,
        company: String,
        ruc: String,
    ): UserProfile {
        val (profile, token) = repository.registerAdmin(
            name = name.trim(),
            email = email.trim().lowercase(),
            password = password,
            dni = dni.trim(),
            address = address.trim(),
            company = company.trim(),
            ruc = ruc.trim(),
        )
        session.signIn(SessionRole.ADMIN, profile.copy(role = "admin"), token)
        return profile
    }
}

class IsEmailRegisteredUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Boolean =
        repository.isEmailRegistered(email.trim().lowercase())
}

class FindResidentInviteUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(code: String): UserProfile =
        repository.findResidentByInviteCode(code.trim())
}

class SetResidentCredentialsUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(code: String, email: String, password: String): UserProfile {
        val (profile, token) = repository.setResidentCredentials(
            code = code.trim(),
            email = email.trim().lowercase(),
            password = password,
        )
        session.signIn(SessionRole.RESIDENT, profile.copy(role = "resident"), token)
        return profile
    }
}

class RefreshProfileUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): UserProfile? {
        val current = session.current() ?: return null
        val fresh = repository.getProfileById(current.profile.id) ?: return current.profile
        session.updateProfile(fresh)
        return fresh
    }
}

class LogoutUseCase(private val session: SessionRepository) {
    operator fun invoke() = session.clear()
}

class EnsureValidSessionUseCase(private val session: SessionRepository) {
    operator fun invoke(): Boolean {
        val valid = session.current() != null && session.isTokenValid()
        if (!valid) session.clear()
        return valid
    }
}

private fun UserProfile.toSessionRole(): SessionRole =
    if (role == "resident") SessionRole.RESIDENT else SessionRole.ADMIN
