package com.example.buildingfexfrontend.core.data.network

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

/**
 * Wraps every remote call so repositories only ever throw [AppException].
 * Error payload shape matches the web client: `{ code, message }`.
 */
suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: AppException) {
    throw e
} catch (e: HttpException) {
    throw e.toAppException()
} catch (e: java.io.IOException) {
    throw AppException.network()
} catch (e: Exception) {
    throw AppException.unexpected(e)
}

private fun HttpException.toAppException(): AppException {
    val status = response()?.code()
    val raw = try {
        response()?.errorBody()?.string()
    } catch (e: Exception) {
        null
    }
    val parsed = parseErrorPayload(raw)
    val code = when {
        status == 401 -> "SESSION_EXPIRED"
        parsed?.code != null -> parsed.code
        else -> "HTTP_$status"
    }
    val message = parsed?.message ?: message() ?: "HTTP $status"
    return AppException(code, message, status)
}

private data class ErrorPayload(val code: String?, val message: String?)

private fun parseErrorPayload(raw: String?): ErrorPayload? {
    if (raw.isNullOrBlank()) return null
    return try {
        val json = Gson().fromJson(raw, JsonObject::class.java) ?: return null
        ErrorPayload(
            // Only string codes map to business codes (e.g. "EMAIL_NOT_FOUND").
            // Numeric codes (404) fall back to HTTP_$status so the UI can show
            // a friendly Spanish message instead of the raw server text.
            code = json.get("code")
                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                ?.asString,
            message = json.get("message")?.takeIf { it.isJsonPrimitive }?.asString,
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Multi-tenant scope helper: merges `ownerAdminId` (when a session is active)
 * with context specific query params, mirroring `withOwnerParams`.
 */
fun ownerParams(
    session: SessionRepository,
    extra: Map<String, String> = emptyMap(),
): Map<String, String> {
    val owner = session.activeDataOwnerId
    return if (owner.isNullOrBlank()) extra else extra + ("ownerAdminId" to owner)
}
