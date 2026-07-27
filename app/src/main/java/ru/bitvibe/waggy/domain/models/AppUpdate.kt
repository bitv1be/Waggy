package ru.bitvibe.waggy.domain.models

data class AppUpdate(
    val versionName: String,
    val downloadUrl: String,
    val fileName: String,
    val releaseNotes: String?,
    val sizeBytes: Long,
    val sha256: String?,
)
