package com.example.buildingfexfrontend.incidents.domain.model

/** Incident statuses (ubiquitous language shared with the web app). */
object IncidentStatus {
    const val OPEN = "open"
    const val IN_PROGRESS = "in-progress"
    const val RESOLVED = "resolved"
}

data class Incident(
    val id: String,
    val residentId: String? = null,
    val residentName: String = "",
    val description: String = "",
    val status: String = IncidentStatus.OPEN,
    val createdAt: String? = null,
    val provider: String = "",
)

data class IncidentInput(
    val description: String,
    val status: String = IncidentStatus.OPEN,
    val provider: String = "",
    val residentId: String? = null,
    val residentName: String = "",
)
