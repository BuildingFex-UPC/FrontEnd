package com.example.buildingfexfrontend.residents.data.remote

import com.example.buildingfexfrontend.residents.domain.model.Resident
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class ResidentUserDto(
    val id: String? = null,
    val name: String? = null,
    val floor: String? = null,
    val code: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: String? = null,
    val admissionDate: String? = null,
    val ownerAdminId: String? = null,
    val hasCredentials: Boolean? = null,
) {
    fun toDomain(): Resident = Resident(
        id = id.orEmpty(),
        name = name.orEmpty(),
        floor = floor.orEmpty(),
        code = code.orEmpty(),
        email = email.orEmpty(),
        hasCredentials = hasCredentials
            ?: (!email.isNullOrBlank() && !password.isNullOrBlank()),
        admissionDate = admissionDate,
    )
}

data class ReservationIdDto(val id: String? = null)

/** Residents bounded-context endpoints (users with `role=resident`). */
interface ResidentsApi {

    @GET("users")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<ResidentUserDto>

    @POST("users")
    suspend fun create(@Body body: ResidentUserDto): ResidentUserDto

    @DELETE("users/{id}")
    suspend fun delete(@Path("id") id: String)

    @GET("reservations")
    suspend fun reservations(
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): List<ReservationIdDto>

    @DELETE("reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: String)
}
