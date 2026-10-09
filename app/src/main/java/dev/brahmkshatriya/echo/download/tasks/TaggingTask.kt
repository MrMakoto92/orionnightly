package dev.orionlabs.orionmusic.download.tasks

import android.content.Context
import android.media.MediaScannerConnection
import dev.orionlabs.orionmusic.download.Downloader
import dev.orionlabs.orionmusic.download.db.models.TaskType
import java.io.File

class TaggingTask(
    private val app: Context,
    downloader: Downloader,
    override val trackId: Long,
) : BaseTask(app, downloader, trackId) {

    override val type = TaskType.Tagging

    override suspend fun work(trackId: Long) {
        val downloadContext = getDownloadContext()
        val download = getDownload()
        val file = withDownloadExtension {
            tag(progressFlow, downloadContext, File(download.toTagFile!!))
        }
        dao.insertDownloadEntity(download.copy(finalFile = file.toString()))
        MediaScannerConnection.scanFile(
            app, arrayOf(file.toString()), null, null
        )
    }

}
