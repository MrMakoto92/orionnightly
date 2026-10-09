package dev.orionlabs.orionmusic.download.tasks

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Drawable
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.NotificationUtil.createNotificationChannel
import androidx.media3.common.util.UnstableApi
import dev.orionlabs.orionmusic.echo.R
import dev.orionlabs.orionmusic.common.clients.DownloadClient
import dev.orionlabs.orionmusic.common.models.DownloadContext
import dev.orionlabs.orionmusic.common.models.Progress
import dev.orionlabs.orionmusic.download.DownloadWorker.Companion.getMainIntent
import dev.orionlabs.orionmusic.download.Downloader
import dev.orionlabs.orionmusic.download.db.models.TaskType
import dev.orionlabs.orionmusic.download.exceptions.DownloadException
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.getAs
import dev.orionlabs.orionmusic.ui.common.ExceptionUtils.toData
import dev.orionlabs.orionmusic.utils.CoroutineUtils.throttleLatest
import dev.orionlabs.orionmusic.utils.Serializer.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File

abstract class BaseTask(
    private val context: Context,
    val downloader: Downloader,
    open val trackId: Long,
) {
    abstract val type: TaskType
    val progressFlow = MutableStateFlow(Progress())
    val throttledProgressFlow = progressFlow.throttleLatest(500L)
    val running = MutableStateFlow(false)
    suspend fun <T> withDownloadExtension(block: suspend DownloadClient.() -> T) =
        downloader.downloadExtension().getAs<DownloadClient, T> { block() }.getOrThrow()

    val dao = downloader.dao

    suspend fun doWork() = withContext(Dispatchers.IO) {
        running.value = true
        val result = runCatching { work(trackId) }
        val throwable = result.exceptionOrNull()
        val download = dao.getDownloadEntity(trackId)
        if (throwable != null && download != null) {
            val exception = DownloadException(type, download, throwable)
            val exceptionFile = context.exceptionDir().resolve("$trackId.json")
            exceptionFile.writeText(exception.toData(context).toJson())
            dao.insertDownloadEntity(download.copy(exceptionFile = exceptionFile.absolutePath))
        }
        running.value = false
        result
    }

    suspend fun getDownload() = dao.getDownloadEntity(trackId)!!

    suspend fun getDownloadContext() = run {
        val download = getDownload()
        val contextEntity = download.contextId?.let { dao.getContextEntity(it) }
        DownloadContext(
            download.extensionId,
            download.track.getOrThrow(),
            download.sortOrder,
            contextEntity?.mediaItem?.getOrThrow()
        )
    }

    abstract suspend fun work(trackId: Long)

    companion object {
        fun Context.getTitle(type: TaskType, title: String) = when (type) {
            TaskType.Loading -> getString(R.string.loading_x, title)
            TaskType.Downloading -> getString(R.string.downloading_x, title)
            TaskType.Merging -> getString(R.string.merging_x, title)
            TaskType.Tagging -> getString(R.string.tagging_x, title)
            TaskType.Saving -> getString(R.string.saving_x, title)
        }

        private const val DOWNLOAD_CHANNEL_ID = "download_channel"

        fun Context.exceptionDir() = File(filesDir, "download_exceptions").apply { mkdirs() }
    }


    @OptIn(UnstableApi::class)
    fun createCompleteNotification(
        context: Context,
        title: String,
        drawable: Drawable?,
    ) {
        createNotificationChannel(
            context, DOWNLOAD_CHANNEL_ID, R.string.download_complete, 0,
            NotificationUtil.IMPORTANCE_DEFAULT
        )
        val notificationBuilder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_downloading)
            .setContentTitle(context.getString(R.string.download_complete))
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigPictureStyle().bigLargeIcon(drawable?.toBitmap())
            )
            .setContentIntent(getMainIntent(context))
            .setAutoCancel(true)

        if (checkSelfPermission(context, POST_NOTIFICATIONS) != PERMISSION_GRANTED) return
        NotificationManagerCompat.from(context).notify(
            title.hashCode(),
            notificationBuilder.build()
        )
    }
}
