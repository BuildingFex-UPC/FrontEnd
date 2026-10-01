package com.example.buildingfexfrontend.core.domain.model

/**
 * UI-facing async state wrapper shared by every presentation layer.
 */
sealed interface Resource<out T> {
    data object Idle : Resource<Nothing>
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Failure(val error: AppException) : Resource<Nothing>
}

inline fun <T> runCatchingApp(block: () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (e: Throwable) {
    Resource.Failure(AppException.unexpected(e))
}
