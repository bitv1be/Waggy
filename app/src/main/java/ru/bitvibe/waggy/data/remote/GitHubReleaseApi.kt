package ru.bitvibe.waggy.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Headers

interface GitHubReleaseApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2026-03-10",
    )
    @GET("releases/latest")
    suspend fun getLatestRelease(): GitHubReleaseResponse
}

@Serializable
data class GitHubReleaseResponse(
    @SerialName("tag_name") val tagName: String,
    val body: String? = null,
    val assets: List<GitHubReleaseAssetResponse> = emptyList(),
)

@Serializable
data class GitHubReleaseAssetResponse(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long = 0,
    val digest: String? = null,
)
