package com.example.buildingfexfrontend.socialspaces.data.remote

import com.example.buildingfexfrontend.socialspaces.domain.model.Guest
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class GuestDto(
    val id: String? = null,
    val name: String? = null,
    val checkedIn: Boolean? = null,
    val checkedInAt: String? = null,
) {
    fun toDomain() = Guest(
        id = id.orEmpty(),
        name = name.orEmpty(),
        checkedIn = checkedIn == true,
        checkedInAt = checkedInAt,
    )
}

data class ReservationDto(
    val id: String? = null,
    val spaceId: String? = null,
    val residentId: String? = null,
    val residentName: String? = null,
    val residentCode: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val guests: List<GuestDto>? = null,
    val guestInviteToken: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain(includeInviteToken: Boolean = true) = Reservation(
        id = id,
        spaceId = spaceId.orEmpty(),
        residentId = residentId.orEmpty(),
        residentName = residentName.orEmpty(),
        residentCode = residentCode.orEmpty(),
        date = date.orEmpty(),
        startTime = startTime.orEmpty(),
        endTime = endTime.orEmpty(),
        guests = guests.orEmpty()
            .map { it.toDomain() }
            .filter { it.name.isNotBlank() },
        guestInviteToken = if (includeInviteToken) guestInviteToken else null,
        ownerAdminId = ownerAdminId,
    )
}

data class CreateReservationDto(
    val id: String,
    val spaceId: String,
    val residentId: String,
    val residentName: String,
    val residentCode: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val guests: List<GuestDto> = emptyList(),
    val guestInviteToken: String? = null,
    val ownerAdminId: String? = null,
)

data class GuestDtoPatch(
    val id: String,
    val name: String,
    val checkedIn: Boolean = false,
    val checkedInAt: String? = null,
)

data class GuestsPatch(
    val guests: List<GuestDtoPatch>,
    val guestInviteToken: String? = null,
)

/** Social Spaces bounded-context endpoints (reservations side). */
interface ReservationsApi {

    @GET("reservations")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<ReservationDto>

    @GET("reservations/{id}")
    suspend fun byId(
        @Path("id") id: String,
        @QueryMap params: Map<String, @JvmSuppressWildcards String>,
    ): ReservationDto

    @POST("reservations")
    suspend fun create(@Body body: CreateReservationDto): ReservationDto

    @PATCH("reservations/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body body: GuestsPatch,
    ): ReservationDto

    @DELETE("reservations/{id}")
    suspend fun delete(@Path("id") id: String)
}
