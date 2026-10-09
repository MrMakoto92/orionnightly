package dev.orionlabs.orionmusic.download.tasks

import android.content.Context
import dev.orionlabs.orionmusic.download.Downloader
import dev.orionlabs.orionmusic.download.db.models.TaskType
import dev.orionlabs.orionmusic.utils.Serializer.toJson

class DownloadingTask(
    context: Context,
    downloader: Downloader,
    override val trackId: Long,
    val index: Int,
) : BaseTask(context, downloader, trackId) {

    override val type = TaskType.Downloading
    override suspend fun work(trackId: Long) {
        var download = getDownload()
        val server = downloader.getServer(trackId, download)
        val source = server.sources[index]
        val downloadContext = getDownloadContext()
        val file = withDownloadExtension { download(progressFlow, downloadContext, source) }
        download = getDownload()
        download =
            download.copy(toMergeFilesData = (download.toMergeFiles + file.toString()).toJson())
        dao.insertDownloadEntity(download)
    }
}
