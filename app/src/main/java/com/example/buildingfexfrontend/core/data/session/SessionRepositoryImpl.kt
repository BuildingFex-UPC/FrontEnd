package com.example.buildingfexfrontend.core.data.session

import android.content.Context
import com.example.buildingfexfrontend.core.domain.model.Session
import com.example.buildingfexfrontend.core.domain.model.SessionRole
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Base64
import org.json.JSONObject

/**
 * SharedPreferences-backed session store (mobile equivalent of the web
 * `localStorage` session). Keeps the JWT, the role and the profile in sync
 * with an observable [StateFlow].
 */
class SessionRepositoryImpl(context: Context) : SessionRepository {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _session = MutableStateFlow(loadStored())
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override fun current(): Session? = _session.value

    override val accessToken: String?
        get() = _session.value?.token?.takeIf { it.isNotBlank() }

    override val activeDataOwnerId: String?
        get() = _session.value?.activeDataOwnerId

    override fun role(): SessionRole? = _session.value?.role

    override fun isTokenValid(): Boolean {
        val token = accessToken ?: return false
        return try {
            val segment = token.split(".")[1]
            val json = String(Base64.decode(segment, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val exp = JSONObject(json).optLong("exp", -1L)
            if (exp <= 0) true else System.currentTimeMillis() < exp * 1000 - 30_000
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signIn(role: SessionRole, profile: UserProfile, token: String) {
        store(Session(role, profile, token))
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val current = _session.value ?: return
        store(current.copy(profile = profile))
    }

    override fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
        _session.value = null
    }

    private fun store(session: Session) {
        prefs.edit().putString(KEY_SESSION, gson.toJson(session)).apply()
        _session.value = session
    }

    private fun loadStored(): Session? = try {
        prefs.getString(KEY_SESSION, null)
            ?.takeIf { it.isNotBlank() }
            ?.let { gson.fromJson(it, Session::class.java) }
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val FILE_NAME = "buildingfex.session"
        const val KEY_SESSION = "session"
    }
}
