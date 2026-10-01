package com.example.buildingfexfrontend.team.data.remote

import com.example.buildingfexfrontend.team.domain.model.TeamWorker
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class TeamWorkerDto(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val dni: String? = null,
    val salary: Double? = null,
    val photoUrl: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = TeamWorker(
        id = id.orEmpty(),
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        dni = dni.orEmpty(),
        salary = salary ?: 0.0,
        photoUrl = photoUrl.orEmpty(),
    )
}

interface TeamApi {

    @GET("teamWorkers")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<TeamWorkerDto>

    @POST("teamWorkers")
    suspend fun add(@Body body: TeamWorkerDto): TeamWorkerDto

    @DELETE("teamWorkers/{id}")
    suspend fun remove(@Path("id") id: String)
}
