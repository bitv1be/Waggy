package ru.bitvibe.waggy.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.bitvibe.waggy.data.remote.GitHubReleaseApi
import ru.bitvibe.waggy.data.remote.GitHubReleaseAssetResponse
import ru.bitvibe.waggy.data.remote.GitHubReleaseResponse

class AppUpdateRepositoryImplTest {
    @Test
    fun getLatestUpdate_returnsVersionedWaggyApk() = runBlocking {
        val digest = "a".repeat(64)
        val repository = AppUpdateRepositoryImpl(
            api = FakeGitHubReleaseApi(
                GitHubReleaseResponse(
                    tagName = "v1.0.12",
                    body = "Bug fixes",
                    assets = listOf(
                        GitHubReleaseAssetResponse(
                            name = "another.apk",
                            browserDownloadUrl = "https://example.com/another.apk",
                        ),
                        GitHubReleaseAssetResponse(
                            name = "Waggy-1.0.12.apk",
                            browserDownloadUrl = "https://example.com/waggy.apk",
                            size = 42,
                            digest = "sha256:$digest",
                        ),
                    ),
                ),
            ),
        )

        val update = repository.getLatestUpdate("1.0.11")

        assertEquals("1.0.12", update?.versionName)
        assertEquals("Waggy-1.0.12.apk", update?.fileName)
        assertEquals("https://example.com/waggy.apk", update?.downloadUrl)
        assertEquals("Bug fixes", update?.releaseNotes)
        assertEquals(42L, update?.sizeBytes)
        assertEquals(digest, update?.sha256)
    }

    @Test
    fun getLatestUpdate_returnsNullWhenInstalledVersionIsCurrent() = runBlocking {
        val repository = AppUpdateRepositoryImpl(
            api = FakeGitHubReleaseApi(
                GitHubReleaseResponse(
                    tagName = "v1.0.12",
                    assets = emptyList(),
                ),
            ),
        )

        assertNull(repository.getLatestUpdate("1.0.12"))
    }

    @Test(expected = AppUpdatePackageNotFoundException::class)
    fun getLatestUpdate_rejectsReleaseWithoutExpectedApk() = runBlocking {
        val repository = AppUpdateRepositoryImpl(
            api = FakeGitHubReleaseApi(
                GitHubReleaseResponse(
                    tagName = "v1.0.13",
                    assets = listOf(
                        GitHubReleaseAssetResponse(
                            name = "another.apk",
                            browserDownloadUrl = "https://example.com/another.apk",
                        ),
                        GitHubReleaseAssetResponse(
                            name = "Waggy-1.0.13.aab",
                            browserDownloadUrl = "https://example.com/waggy.aab",
                        ),
                    ),
                ),
            ),
        )

        repository.getLatestUpdate("1.0.12")
        Unit
    }
}

private class FakeGitHubReleaseApi(
    private val release: GitHubReleaseResponse,
) : GitHubReleaseApi {
    override suspend fun getLatestRelease(): GitHubReleaseResponse = release
}
