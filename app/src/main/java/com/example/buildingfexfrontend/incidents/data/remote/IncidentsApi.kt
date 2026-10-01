package com.example.buildingfexfrontend.incidents.data.remote

import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentInput
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class IncidentDto(
    val id: String? = null,
    val residentId: String? = null,
    val residentName: String? = null,
    val description: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val provider: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = Incident(
        id = id.orEmpty(),
        residentId = residentId,
        residentName = residentName.orEmpty(),
        description = description.orEmpty(),
        status = status ?: "open",
        createdAt = createdAt,
        provider = provider.orEmpty(),
    )

    companion object {
        fun from(input: IncidentInput, ownerAdminId: String?) = IncidentDto(
            id = "incident-${System.currentTimeMillis()}",
            residentId = input.residentId,
            residentName = input.residentName,
            description = input.description,
            status = input.status,
            createdAt = com.example.buildingfexfrontend.core.util.Dates.nowIso(),
            provider = input.provider,
        ).let { if (ownerAdminId != null) it.copy(ownerAdminId = ownerAdminId) else it }
    }
}

/** Incidents bounded-context endpoints. */
interface IncidentsApi {

    @GET("incidents")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<IncidentDto>

    @POST("incidents")
    suspend fun add(@Body body: IncidentDto): IncidentDto

    @PUT("incidents/{id}")
    suspend fun update(@Path("id") id: String, @Body body: IncidentDto): IncidentDto

    @DELETE("incidents/{id}")
    suspend fun remove(@Path("id") id: String)
}
