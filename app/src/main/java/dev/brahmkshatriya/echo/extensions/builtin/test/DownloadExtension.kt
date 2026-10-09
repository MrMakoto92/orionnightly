package dev.orionlabs.orionmusic.extensions.builtin.test

import android.content.Context
import dev.orionlabs.orionmusic.common.MusicExtension
import dev.orionlabs.orionmusic.common.clients.AlbumClient
import dev.orionlabs.orionmusic.common.clients.DownloadClient
import dev.orionlabs.orionmusic.common.clients.PlaylistClient
import dev.orionlabs.orionmusic.common.clients.RadioClient
import dev.orionlabs.orionmusic.common.models.Album
import dev.orionlabs.orionmusic.common.models.DownloadContext
import dev.orionlabs.orionmusic.common.models.EchoMediaItem
import dev.orionlabs.orionmusic.common.models.ExtensionType
import dev.orionlabs.orionmusic.common.models.Feed.Companion.loadAll
import dev.orionlabs.orionmusic.common.models.ImportType
import dev.orionlabs.orionmusic.common.models.Metadata
import dev.orionlabs.orionmusic.common.models.Playlist
import dev.orionlabs.orionmusic.common.models.Progress
import dev.orionlabs.orionmusic.common.models.Radio
import dev.orionlabs.orionmusic.common.models.Streamable
import dev.orionlabs.orionmusic.common.models.Track
import dev.orionlabs.orionmusic.common.providers.MusicExtensionsProvider
import dev.orionlabs.orionmusic.common.settings.Setting
import dev.orionlabs.orionmusic.common.settings.Settings
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.getAs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class DownloadExtension(
    val context: Context
) : DownloadClient, MusicExtensionsProvider {

    companion object {
        val metadata = Metadata(
            "DownloadExtension",
            "",
            ImportType.BuiltIn,
            ExtensionType.MISC,
            "test_download",
            "Test Download Extension",
            "1.0.0",
            "Test extension for download testing",
            "Test",
        )
    }

    override val concurrentDownloads = 2

    override suspend fun selectServer(context: DownloadContext): Streamable {
        return context.track.servers.first()
    }

    override suspend fun selectSources(
        context: DownloadContext, server: Streamable.Media.Server
    ): List<Streamable.Source> {
        return server.sources
    }

    override suspend fun download(
        progressFlow: MutableStateFlow<Progress>,
        context: DownloadContext,
        source: Streamable.Source
    ) = test(progressFlow, "Downloading", 10000)

    override suspend fun merge(
        progressFlow: MutableStateFlow<Progress>,
        context: DownloadContext,
        files: List<File>
    ) = test(progressFlow, "Merging", 5000)

    override suspend fun tag(
        progressFlow: MutableStateFlow<Progress>,
        context: DownloadContext,
        file: File
    ) = test(progressFlow, "Tagging", 2000)

    override suspend fun getDownloadTracks(
        extensionId: String,
        item: EchoMediaItem,
        context: EchoMediaItem?
    ): List<DownloadContext> {
        return when (item) {
            is Track -> listOf(DownloadContext(extensionId, item))
            is EchoMediaItem.Lists -> {
                val ext = exts.first { it.id == extensionId }
                val tracks = when (item) {
                    is Album -> ext.getAs<AlbumClient, List<Track>> {
                        val album = loadAlbum(item)
                        val tracks = loadTracks(album)!!.loadAll()
                        tracks
                    }

                    is Playlist -> ext.getAs<PlaylistClient, List<Track>> {
                        val album = loadPlaylist(item)
                        val tracks = loadTracks(album).loadAll()
                        tracks
                    }

                    is Radio -> ext.getAs<RadioClient, List<Track>> {
                        val radio = loadRadio(item)
                        loadTracks(radio).loadAll()
                    }

                }.getOrThrow()
                tracks.mapIndexed { index, track ->
                    DownloadContext(extensionId, track, index, item)
                }
            }

            else -> listOf()
        }
    }

    private suspend fun test(
        progressFlow: MutableSharedFlow<Progress>,
        type: String,
        crash: Long
    ): File {
        progressFlow.emit(Progress(crash, 0))
        var it = 0L
        while (it < crash) {
            delay(1)
            progressFlow.emit(Progress(crash, it))
            it++
        }
        if (type == "Tagging") throw Exception("Test exception in $type")
        return this.context.cacheDir
    }

    override suspend fun getSettingItems() = listOf<Setting>()
    override fun setSettings(settings: Settings) {}
    override val requiredMusicExtensions = listOf<String>()

    private lateinit var exts: List<MusicExtension>
    override fun setMusicExtensions(extensions: List<MusicExtension>) {
        exts = extensions
    }
}
