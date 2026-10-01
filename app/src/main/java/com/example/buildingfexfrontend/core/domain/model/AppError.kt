package com.example.buildingfexfrontend.core.domain.model

import com.example.buildingfexfrontend.core.i18n.AppLanguage
import com.example.buildingfexfrontend.core.i18n.translateOrNull

/**
 * Unified error model for every bounded context.
 * The backend reports failures as `{ code, message }`; the data layer maps
 * transport/HTTP failures into this type so the UI only branches on [code].
 */
class AppException(
    val code: String,
    override val message: String,
    val status: Int? = null,
    val payload: Map<String, Any?>? = null,
) : Exception(message) {

    /**
     * Human readable message in the current app language.
     * Known codes resolve from the i18n catalog (`err.<code>`); unknown codes fall
     * back to the raw message (usually provided by the backend).
     */
    fun userMessage(): String =
        translateOrNull("err.$code", AppLanguage.current)
            ?: message.ifBlank { null }
            ?: translateOrNull("err.generic", AppLanguage.current).orEmpty()

    companion object {
        fun network(): AppException =
            AppException("NETWORK_ERROR", "Error de conexión.")

        fun timeout(): AppException =
            AppException("TIMEOUT", "La solicitud tardó demasiado.")

        fun unexpected(throwable: Throwable): AppException = when (throwable) {
            is AppException -> throwable
            is java.io.IOException -> network()
            else -> AppException("UNEXPECTED_ERROR", throwable.message ?: "Error inesperado.")
        }
    }
}
