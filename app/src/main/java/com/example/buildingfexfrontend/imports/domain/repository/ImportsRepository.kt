package com.example.buildingfexfrontend.imports.domain.repository

import com.example.buildingfexfrontend.imports.domain.model.ImportUpload

interface ImportsRepository {
    suspend fun list(): List<ImportUpload>
    suspend fun add(upload: ImportUpload): ImportUpload
    suspend fun remove(id: String)
}
