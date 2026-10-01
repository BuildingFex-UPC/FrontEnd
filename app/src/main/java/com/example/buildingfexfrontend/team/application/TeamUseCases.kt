package com.example.buildingfexfrontend.team.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.team.domain.model.TeamWorker
import com.example.buildingfexfrontend.team.domain.repository.TeamRepository

class TeamUseCases(
    private val repository: TeamRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val list = ListTeamUseCase(repository)
    val add = AddTeamWorkerUseCase(repository)
    val update = UpdateTeamWorkerUseCase(repository)
    val remove = RemoveTeamWorkerUseCase(repository)
}

class ListTeamUseCase(private val repository: TeamRepository) {
    suspend operator fun invoke(): List<TeamWorker> = repository.list()
}

class AddTeamWorkerUseCase(private val repository: TeamRepository) {
    suspend operator fun invoke(
        name: String,
        phone: String,
        dni: String,
        salary: Double,
        photoUrl: String = "",
    ): TeamWorker = repository.add(
        validatedWorker(name = name, phone = phone, dni = dni, salary = salary, photoUrl = photoUrl),
    )
}

class UpdateTeamWorkerUseCase(private val repository: TeamRepository) {
    suspend operator fun invoke(
        original: TeamWorker,
        name: String,
        phone: String,
        dni: String,
        salary: Double,
        photoUrl: String,
    ): TeamWorker = repository.update(
        original = original,
        updated = validatedWorker(
            name = name,
            phone = phone,
            dni = dni,
            salary = salary,
            photoUrl = photoUrl,
        ).copy(id = original.id),
    )
}

class RemoveTeamWorkerUseCase(private val repository: TeamRepository) {
    suspend operator fun invoke(id: String) {
        if (id.isBlank()) {
            throw AppException("TEAM_WORKER_NOT_FOUND", "No se encontró al miembro del equipo.")
        }
        repository.remove(id)
    }
}

private fun validatedWorker(
    name: String,
    phone: String,
    dni: String,
    salary: Double,
    photoUrl: String,
): TeamWorker {
    val cleanName = name.trim()
    val cleanPhone = phone.trim()
    val cleanDni = dni.trim()
    if (cleanName.isBlank() || cleanPhone.isBlank() || cleanDni.isBlank() || salary.isNaN() || salary < 0) {
        throw AppException(
            "TEAM_WORKER_FIELDS_REQUIRED",
            "Nombre, teléfono, DNI y un salario válidos son obligatorios.",
        )
    }
    return TeamWorker(
        id = "",
        name = cleanName,
        phone = cleanPhone,
        dni = cleanDni,
        salary = salary,
        photoUrl = photoUrl.trim(),
    )
}
