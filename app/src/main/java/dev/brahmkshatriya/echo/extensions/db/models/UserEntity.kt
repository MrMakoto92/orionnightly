package dev.orionlabs.orionmusic.extensions.db.models

import androidx.room.Entity
import dev.orionlabs.orionmusic.common.models.ExtensionType
import dev.orionlabs.orionmusic.common.models.User
import dev.orionlabs.orionmusic.utils.Serializer.toData
import dev.orionlabs.orionmusic.utils.Serializer.toJson

@Entity(primaryKeys = ["id", "type", "extId"])
data class UserEntity(
    val type: ExtensionType,
    val extId: String,
    val id: String,
    val data: String
) {
    val user by lazy { data.toData<User>() }

    companion object {
        fun User.toEntity(type: ExtensionType, clientId: String) =
            UserEntity(type, clientId, id, toJson())

        fun UserEntity.toCurrentUser() =
            CurrentUser(type, extId, id)
    }
}
