package ru.bitvibe.waggy.data.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.bitvibe.waggy.domain.models.AppUpdate
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    suspend fun download(
        update: AppUpdate,
        onProgress: (Int?) -> Unit,
    ): Uri {
        val safeFileName = update.fileName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destination = "updates/${System.currentTimeMillis()}-$safeFileName"
        val request = DownloadManager.Request(update.downloadUrl.toUri())
            .setMimeType(APK_MIME_TYPE)
            .setTitle("Waggy ${update.versionName}")
            .setDescription("Downloading app update")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                destination,
            )
        val downloadId = downloadManager.enqueue(request)

        while (currentCoroutineContext().isActive) {
            DownloadManager.Query().setFilterById(downloadId).let(downloadManager::query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    throw AppUpdateDownloadException("The update download disappeared")
                }

                when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            ?: throw AppUpdateDownloadException(
                                "Android could not open the downloaded APK",
                            )
                        verifyChecksum(uri, update.sha256, downloadId)
                        onProgress(100)
                        return uri
                    }

                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON),
                        )
                        throw AppUpdateDownloadException("DownloadManager failed with reason $reason")
                    }

                    else -> onProgress(cursor.progressPercent())
                }
            }
            delay(PROGRESS_POLL_INTERVAL_MILLIS)
        }

        throw CancellationException("Update download observation was cancelled")
    }

    private fun verifyChecksum(uri: Uri, expectedSha256: String?, downloadId: Long) {
        if (expectedSha256 == null) return

        val digest = MessageDigest.getInstance("SHA-256")
        val actualSha256 = context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            digest.digest().joinToString(separator = "") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
        } ?: throw AppUpdateDownloadException("The downloaded APK could not be read")

        if (!actualSha256.equals(expectedSha256, ignoreCase = true)) {
            downloadManager.remove(downloadId)
            throw AppUpdateDownloadException("The downloaded APK checksum did not match")
        }
    }

    private fun android.database.Cursor.progressPercent(): Int? {
        val downloaded = getLong(
            getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
        )
        val total = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        return if (downloaded >= 0 && total > 0) {
            ((downloaded * 100) / total).toInt().coerceIn(0, 100)
        } else {
            null
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val PROGRESS_POLL_INTERVAL_MILLIS = 500L
    }
}

class AppUpdateDownloadException(message: String) : IllegalStateException(message)
