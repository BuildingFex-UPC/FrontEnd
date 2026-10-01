package com.example.buildingfexfrontend.team.domain.model

/** Admin team / staff directory entry. */
data class TeamWorker(
    val id: String,
    val name: String,
    val phone: String = "",
    val dni: String = "",
    val salary: Double = 0.0,
    val photoUrl: String = "",
)
