package ru.bitvibe.waggy.data.repository

import ru.bitvibe.waggy.data.remote.GitHubReleaseApi
import ru.bitvibe.waggy.domain.models.AppUpdate
import ru.bitvibe.waggy.domain.repository.AppUpdateRepository
import javax.inject.Inject

class AppUpdateRepositoryImpl @Inject constructor(
    private val api: GitHubReleaseApi,
) : AppUpdateRepository {
    override suspend fun getLatestUpdate(currentVersionName: String): AppUpdate? {
        val release = api.getLatestRelease()
        val latestVersionName = release.tagName.removePrefix("v").removePrefix("V")

        if (!VersionComparator.isNewer(latestVersionName, currentVersionName)) {
            return null
        }

        val expectedAssetName = "Waggy-$latestVersionName.apk"
        val apkAsset = release.assets.firstOrNull {
            it.name.equals(expectedAssetName, ignoreCase = true)
        } ?: throw AppUpdatePackageNotFoundException(latestVersionName)

        return AppUpdate(
            versionName = latestVersionName,
            downloadUrl = apkAsset.browserDownloadUrl,
            fileName = apkAsset.name,
            releaseNotes = release.body?.takeIf { it.isNotBlank() },
            sizeBytes = apkAsset.size,
            sha256 = apkAsset.digest.toSha256OrNull(),
        )
    }
}

class AppUpdatePackageNotFoundException(versionName: String) :
    IllegalStateException("GitHub release $versionName does not contain an APK asset")

private fun String?.toSha256OrNull(): String? {
    val value = this?.substringAfter("sha256:", missingDelimiterValue = "")
        ?.lowercase()
        ?: return null
    return value.takeIf { it.matches(Regex("[0-9a-f]{64}")) }
}
