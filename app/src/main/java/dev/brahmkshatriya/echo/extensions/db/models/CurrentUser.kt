package dev.orionlabs.orionmusic.extensions.db.models

import androidx.room.Entity
import dev.orionlabs.orionmusic.common.models.ExtensionType

@Entity(primaryKeys = ["type", "extId"])
data class CurrentUser(
    val type : ExtensionType,
    val extId: String,
    val userId: String?
)
