package com.example.buildingfexfrontend.imports.application

import android.util.Base64
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.imports.domain.model.ImportUpload
import com.example.buildingfexfrontend.imports.domain.repository.ImportsRepository

class ImportsUseCases(
    private val repository: ImportsRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val list = ListUploadsUseCase(repository)
    val upload = UploadFileUseCase(repository)
    val remove = RemoveUploadUseCase(repository)
    val decode = DecodeDataUrlUseCase()
}

class ListUploadsUseCase(private val repository: ImportsRepository) {
    suspend operator fun invoke(): List<ImportUpload> = repository.list()
}

class UploadFileUseCase(private val repository: ImportsRepository) {
    suspend operator fun invoke(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): ImportUpload {
        if (bytes.isEmpty()) {
            throw AppException("IMPORT_FILE_EMPTY", "El archivo está vacío.")
        }
        if (bytes.size > MAX_BYTES) {
            throw AppException(
                "IMPORT_FILE_TOO_LARGE",
                "El archivo supera el máximo de ${MAX_BYTES / 1024 / 1024} MB.",
            )
        }
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return repository.add(
            ImportUpload(
                id = "",
                fileName = fileName.ifBlank { "archivo" },
                mimeType = mimeType.ifBlank { "application/octet-stream" },
                size = bytes.size.toLong(),
                dataUrl = "data:$mimeType;base64,$encoded",
            ),
        )
    }

    private companion object {
        const val MAX_BYTES = 2 * 1024 * 1024
    }
}

class RemoveUploadUseCase(private val repository: ImportsRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}

class DecodeDataUrlUseCase {
    operator fun invoke(dataUrl: String): ByteArray? {
        val marker = "base64,"
        val index = dataUrl.indexOf(marker)
        if (index < 0) return null
        return try {
            Base64.decode(dataUrl.substring(index + marker.length), Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}
