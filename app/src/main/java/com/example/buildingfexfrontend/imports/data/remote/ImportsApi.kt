package com.example.buildingfexfrontend.imports.data.remote

import com.example.buildingfexfrontend.imports.domain.model.ImportUpload
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class ImportUploadDto(
    val id: String? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val size: Double? = null,
    val uploadedAt: String? = null,
    val dataUrl: String? = null,
    val ownerAdminId: String? = null,
) {
    fun toDomain() = ImportUpload(
        id = id.orEmpty(),
        fileName = fileName.orEmpty(),
        mimeType = mimeType ?: "application/octet-stream",
        size = size?.toLong() ?: 0L,
        uploadedAt = uploadedAt.orEmpty(),
        dataUrl = dataUrl.orEmpty(),
    )
}

interface ImportsApi {

    @GET("importUploads")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<ImportUploadDto>

    @POST("importUploads")
    suspend fun add(@Body body: ImportUploadDto): ImportUploadDto

    @DELETE("importUploads/{id}")
    suspend fun remove(@Path("id") id: String)
}
