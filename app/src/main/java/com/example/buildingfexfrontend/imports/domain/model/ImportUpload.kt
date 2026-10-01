package com.example.buildingfexfrontend.imports.domain.model

/** File uploaded from the Importación screen (stored as a data URL). */
data class ImportUpload(
    val id: String,
    val fileName: String,
    val mimeType: String = "application/octet-stream",
    val size: Long = 0L,
    val uploadedAt: String = "",
    val dataUrl: String = "",
)
