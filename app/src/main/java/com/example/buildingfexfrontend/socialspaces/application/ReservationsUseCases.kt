package com.example.buildingfexfrontend.socialspaces.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.socialspaces.domain.model.GuestInput
import com.example.buildingfexfrontend.socialspaces.domain.model.NewReservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.repository.ReservationsRepository

/** Reservation use cases for both roles. */
class ReservationsUseCases(
    private val repository: ReservationsRepository,
    private val session: SessionRepository,
) {
    val adminList = AdminReservationsUseCase(repository)
    val spaceReservations = SpaceReservationsUseCase(repository)
    val myReservations = MyReservationsUseCase(repository, session)
    val reserve = ReserveSpaceUseCase(repository, session)
    val updateGuests = UpdateGuestsUseCase(repository)
    val checkIn = CheckInGuestUseCase(repository)
}

class AdminReservationsUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(): List<Reservation> = repository.listAllForAdmin()
}

class SpaceReservationsUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(spaceId: String): List<Reservation> = repository.listBySpace(spaceId)
}

class MyReservationsUseCase(
    private val repository: ReservationsRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): List<Reservation> {
        val residentId = session.current()?.profile?.id.orEmpty()
        if (residentId.isBlank()) return emptyList()
        return repository.listByResident(residentId)
    }
}

class ReserveSpaceUseCase(
    private val repository: ReservationsRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(
        spaceId: String,
        date: String,
        startTime: String,
        endTime: String,
    ): Reservation {
        val profile = session.current()?.profile
            ?: throw AppException("RESIDENT_NOT_FOUND", "No resident session")
        return repository.add(
            NewReservation(
                spaceId = spaceId,
                residentId = profile.id,
                residentName = profile.name,
                residentCode = profile.code.orEmpty(),
                date = date,
                startTime = startTime,
                endTime = endTime,
            ),
        )
    }
}

class UpdateGuestsUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(reservationId: String, guests: List<GuestInput>): Reservation =
        repository.updateGuests(reservationId, guests)
}

class CheckInGuestUseCase(private val repository: ReservationsRepository) {
    suspend operator fun invoke(
        reservationId: String,
        guestId: String,
        checkedIn: Boolean,
    ): Reservation = repository.setGuestCheckedIn(reservationId, guestId, checkedIn)
}
