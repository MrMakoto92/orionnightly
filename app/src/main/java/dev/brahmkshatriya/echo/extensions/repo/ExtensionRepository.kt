package dev.orionlabs.orionmusic.extensions.repo

import dev.orionlabs.orionmusic.common.clients.ExtensionClient
import dev.orionlabs.orionmusic.common.models.Metadata
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    val flow: Flow<List<Result<Pair<Metadata, Lazy<ExtensionClient>>>>?>
    suspend fun loadExtensions(): List<Result<Pair<Metadata, Lazy<ExtensionClient>>>>
}
