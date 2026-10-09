package dev.orionlabs.orionmusic.download.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.orionlabs.orionmusic.common.models.EchoMediaItem
import dev.orionlabs.orionmusic.utils.Serializer.toData

@Entity
data class ContextEntity(
    @PrimaryKey(true)
    val id: Long,
    val itemId: String,
    val data: String,
) {
    val mediaItem by lazy { data.toData<EchoMediaItem>() }
}
