package com.example.buildingfexfrontend.residents.domain.repository

import com.example.buildingfexfrontend.residents.domain.model.CascadeDeleteResult
import com.example.buildingfexfrontend.residents.domain.model.LinkedDataPreview
import com.example.buildingfexfrontend.residents.domain.model.NewResident
import com.example.buildingfexfrontend.residents.domain.model.Resident

interface ResidentsRepository {

    suspend fun list(): List<Resident>

    suspend fun create(resident: NewResident): Resident

    suspend fun existsWithCode(code: String): Boolean

    suspend fun previewLinkedData(id: String): LinkedDataPreview

    suspend fun removeCascade(id: String): CascadeDeleteResult
}
