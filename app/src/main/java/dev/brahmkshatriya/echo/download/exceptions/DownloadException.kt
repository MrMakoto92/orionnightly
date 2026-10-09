package dev.orionlabs.orionmusic.download.exceptions

import dev.orionlabs.orionmusic.download.db.models.DownloadEntity
import dev.orionlabs.orionmusic.download.db.models.TaskType

data class DownloadException(
    val type: TaskType,
    val downloadEntity: DownloadEntity,
    override val cause: Throwable
) : Exception()
