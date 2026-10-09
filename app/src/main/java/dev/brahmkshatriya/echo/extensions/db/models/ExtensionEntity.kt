package dev.orionlabs.orionmusic.extensions.db.models

import androidx.room.Entity
import dev.orionlabs.orionmusic.common.models.ExtensionType

@Entity(primaryKeys = ["id", "type"])
data class ExtensionEntity(
    val id: String,
    val type : ExtensionType,
    val enabled : Boolean
)
