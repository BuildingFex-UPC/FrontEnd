package com.example.buildingfexfrontend.socialspaces.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.socialspaces.data.remote.CreateReservationDto
import com.example.buildingfexfrontend.socialspaces.data.remote.GuestDtoPatch
import com.example.buildingfexfrontend.socialspaces.data.remote.GuestsPatch
import com.example.buildingfexfrontend.socialspaces.data.remote.ReservationsApi
import com.example.buildingfexfrontend.socialspaces.domain.model.GuestInput
import com.example.buildingfexfrontend.socialspaces.domain.model.NewReservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.ReservationRules
import com.example.buildingfexfrontend.socialspaces.domain.repository.ReservationsRepository

/**
 * Client-side port of the web `reservationsApi` (json-server backend):
 * field/time/overlap validation happens before POST, exactly like the web client.
 */
class ReservationsRepositoryImpl(
    private val api: ReservationsApi,
    private val session: SessionRepository,
) : ReservationsRepository {

    override suspend fun listBySpace(spaceId: String): List<Reservation> = apiCall {
        api.list(ownerParams(session, mapOf("spaceId" to spaceId)))
            .map { it.toDomain(includeInviteToken = false) }
    }

    override suspend fun listByResident(residentId: String): List<Reservation> = apiCall {
        api.list(ownerParams(session, mapOf("residentId" to residentId)))
            .map { it.toDomain() }
    }

    override suspend fun listAllForAdmin(): List<Reservation> = apiCall {
        api.list(ownerParams(session)).map { it.toDomain(includeInviteToken = false) }
    }

    override suspend fun removeBySpace(spaceId: String): Int {
        val clean = spaceId.trim()
        if (clean.isEmpty()) return 0
        return deleteWhere(mapOf("spaceId" to clean))
    }

    override suspend fun removeByResident(residentId: String): Int {
        val clean = residentId.trim()
        if (clean.isEmpty()) return 0
        return deleteWhere(mapOf("residentId" to clean))
    }

    override suspend fun add(request: NewReservation): Reservation = apiCall {
        val spaceId = request.spaceId.trim()
        val residentId = request.residentId.trim()
        val date = request.date.trim()
        val start = request.startTime.trim()
        val end = request.endTime.trim()

        if (spaceId.isEmpty() || residentId.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw AppException(
                "RESERVATION_FIELDS_REQUIRED",
                "Completa espacio, fecha y horario para reservar.",
            )
        }
        if (!ReservationRules.timeRangeValid(start, end)) {
            throw AppException(
                "RESERVATION_TIME_INVALID",
                "La hora de fin debe ser posterior a la de inicio.",
            )
        }

        val existing = api.list(ownerParams(session, mapOf("spaceId" to spaceId, "date" to date)))
            .map { it.toDomain(includeInviteToken = false) }
        val candidate = Reservation(
            spaceId = spaceId,
            date = date,
            startTime = start,
            endTime = end,
        )
        if (existing.any { ReservationRules.overlaps(it, candidate) }) {
            throw AppException(
                "RESERVATION_OVERLAP",
                "Ya existe una reserva que se cruza con ese horario.",
            )
        }

        api.create(
            CreateReservationDto(
                id = "reservation-${System.currentTimeMillis()}",
                spaceId = spaceId,
                residentId = residentId,
                residentName = request.residentName.trim(),
                residentCode = request.residentCode.trim(),
                date = date,
                startTime = start,
                endTime = end,
                guests = emptyList(),
                guestInviteToken = null,
                ownerAdminId = session.activeDataOwnerId,
            ),
        ).toDomain()
    }

    override suspend fun updateGuests(
        reservationId: String,
        guests: List<GuestInput>,
    ): Reservation = apiCall {
        val cleanId = reservationId.trim()
        if (cleanId.isEmpty()) {
            throw AppException("RESERVATION_NOT_FOUND", "Reserva no encontrada.")
        }
        val current = api.byId(cleanId, ownerParams(session)).toDomain()

        val normalized = guests
            .mapIndexed { index, input ->
                val name = input.name.trim()
                if (name.isEmpty()) return@mapIndexed null
                val incomingId = input.id.trim()
                val previous = current.guests.firstOrNull { it.id == incomingId }
                GuestDtoPatch(
                    id = incomingId.ifEmpty { "guest-${System.currentTimeMillis()}-$index" },
                    name = name,
                    checkedIn = previous?.checkedIn == true,
                    checkedInAt = previous?.checkedInAt,
                )
            }
            .filterNotNull()
            .take(ReservationRules.MAX_GUESTS)

        val token = if (normalized.isNotEmpty()) {
            current.guestInviteToken?.takeIf { it.isNotBlank() }
                ?: "invite-${java.util.UUID.randomUUID()}"
        } else {
            null
        }

        api.patch(cleanId, GuestsPatch(guests = normalized, guestInviteToken = token))
            .toDomain()
    }

    override suspend fun setGuestCheckedIn(
        reservationId: String,
        guestId: String,
        checkedIn: Boolean,
    ): Reservation = apiCall {
        val rid = reservationId.trim()
        val gid = guestId.trim()
        if (rid.isEmpty() || gid.isEmpty()) {
            throw AppException("RESERVATION_NOT_FOUND", "Reserva no encontrada.")
        }
        val current = api.byId(rid, ownerParams(session)).toDomain()
        val found = current.guests.any { it.id == gid }
        if (!found) {
            throw AppException("GUEST_NOT_FOUND", "Invitado no encontrado.")
        }
        val nextGuests = current.guests.map { guest ->
            val on = guest.id == gid && checkedIn
            GuestDtoPatch(
                id = guest.id,
                name = guest.name,
                checkedIn = if (guest.id == gid) checkedIn else guest.checkedIn,
                checkedInAt = when {
                    guest.id != gid -> guest.checkedInAt
                    on -> guest.checkedInAt ?: java.time.OffsetDateTime.now().toString()
                    else -> null
                },
            )
        }
        api.patch(rid, GuestsPatch(guests = nextGuests)).toDomain(includeInviteToken = false)
    }

    private suspend fun deleteWhere(params: Map<String, String>): Int = apiCall {
        val items = api.list(ownerParams(session, params))
        items.forEach { item ->
            item.id?.let { id -> api.delete(id) }
        }
        items.size
    }
}
