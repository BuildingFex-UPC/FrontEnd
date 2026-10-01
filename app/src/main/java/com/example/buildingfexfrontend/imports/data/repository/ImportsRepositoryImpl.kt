package com.example.buildingfexfrontend.imports.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.imports.data.remote.ImportsApi
import com.example.buildingfexfrontend.imports.data.remote.ImportUploadDto
import com.example.buildingfexfrontend.imports.domain.model.ImportUpload
import com.example.buildingfexfrontend.imports.domain.repository.ImportsRepository

class ImportsRepositoryImpl(
    private val api: ImportsApi,
    private val session: SessionRepository,
) : ImportsRepository {

    override suspend fun list(): List<ImportUpload> = apiCall {
        api.list(
            ownerParams(session, mapOf("_sort" to "uploadedAt", "_order" to "desc")),
        ).map { it.toDomain() }
    }

    override suspend fun add(upload: ImportUpload): ImportUpload = apiCall {
        api.add(
            ImportUploadDto(
                id = "import-${System.currentTimeMillis()}",
                fileName = upload.fileName,
                mimeType = upload.mimeType,
                size = upload.size.toDouble(),
                uploadedAt = Dates.nowIso(),
                dataUrl = upload.dataUrl,
                ownerAdminId = session.activeDataOwnerId,
            ),
        ).toDomain()
    }

    override suspend fun remove(id: String) = apiCall { api.remove(id) }
}
