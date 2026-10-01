package com.example.buildingfexfrontend.residents.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.residents.data.remote.ResidentUserDto
import com.example.buildingfexfrontend.residents.data.remote.ResidentsApi
import com.example.buildingfexfrontend.residents.domain.model.CascadeDeleteResult
import com.example.buildingfexfrontend.residents.domain.model.LinkedDataPreview
import com.example.buildingfexfrontend.residents.domain.model.NewResident
import com.example.buildingfexfrontend.residents.domain.model.Resident
import com.example.buildingfexfrontend.residents.domain.repository.ResidentsRepository

class ResidentsRepositoryImpl(
    private val api: ResidentsApi,
    private val session: SessionRepository,
) : ResidentsRepository {

    override suspend fun list(): List<Resident> = apiCall {
        api.list(ownerParams(session, mapOf("role" to "resident")))
            .map { it.toDomain() }
            .sortedBy { it.code }
    }

    override suspend fun create(resident: NewResident): Resident = apiCall {
        val ownerId = session.activeDataOwnerId
            ?: throw AppException("RESIDENT_OWNER_REQUIRED", "Owner admin required")
        api.create(
            ResidentUserDto(
                id = "resident-${System.currentTimeMillis()}",
                name = resident.name,
                floor = resident.floor,
                code = resident.code,
                email = "",
                password = "",
                role = "resident",
                admissionDate = resident.admissionDate,
                ownerAdminId = ownerId,
            ),
        ).toDomain()
    }

    override suspend fun existsWithCode(code: String): Boolean = apiCall {
        api.list(
            ownerParams(session, mapOf("role" to "resident", "code" to code)),
        ).isNotEmpty()
    }

    override suspend fun previewLinkedData(id: String): LinkedDataPreview = apiCall {
        val reservations = api.reservations(
            ownerParams(session, mapOf("residentId" to id)),
        )
        LinkedDataPreview(reservations = reservations.size)
    }

    override suspend fun removeCascade(id: String): CascadeDeleteResult = apiCall {
        val reservations = api.reservations(ownerParams(session, mapOf("residentId" to id)))
        val errors = mutableListOf<String>()
        reservations.forEach { reservation ->
            reservation.id?.let { reservationId ->
                try {
                    api.deleteReservation(reservationId)
                } catch (e: Exception) {
                    errors += "reservation:$reservationId"
                }
            }
        }
        try {
            api.delete(id)
        } catch (e: Exception) {
            throw AppException("RESIDENT_DELETE_FAILED", "No se pudo eliminar al residente.")
        }
        CascadeDeleteResult(
            id = id,
            reservationsRemoved = reservations.size,
            errors = errors,
        )
    }
}
