package com.example.buildingfexfrontend.residents.domain.model

/**
 * Resident account aggregate of the residents bounded context.
 * `code` doubles as the department number (ubiquitous language of the web app).
 */
data class Resident(
    val id: String,
    val name: String,
    val floor: String = "",
    val code: String = "",
    val email: String = "",
    val hasCredentials: Boolean = false,
    val admissionDate: String? = null,
)

data class NewResident(
    val name: String,
    val floor: String,
    val code: String,
    val admissionDate: String,
)

data class LinkedDataPreview(val reservations: Int = 0)

data class CascadeDeleteResult(
    val id: String,
    val reservationsRemoved: Int = 0,
    val errors: List<String> = emptyList(),
)
