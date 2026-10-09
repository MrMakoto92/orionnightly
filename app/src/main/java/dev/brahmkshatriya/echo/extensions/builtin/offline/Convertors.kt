package dev.orionlabs.orionmusic.extensions.builtin.offline

import android.content.Context
import dev.orionlabs.orionmusic.R
import dev.orionlabs.orionmusic.common.helpers.PagedData
import dev.orionlabs.orionmusic.common.models.Album
import dev.orionlabs.orionmusic.common.models.Artist
import dev.orionlabs.orionmusic.common.models.Date
import dev.orionlabs.orionmusic.common.models.Date.Companion.toYearDate
import dev.orionlabs.orionmusic.common.models.Feed
import dev.orionlabs.orionmusic.common.models.Feed.Companion.toFeed
import dev.orionlabs.orionmusic.common.models.ImageHolder.Companion.toResourceImageHolder
import dev.orionlabs.orionmusic.common.models.ImageHolder.Companion.toResourceUriImageHolder
import dev.orionlabs.orionmusic.common.models.Playlist
import dev.orionlabs.orionmusic.common.models.Shelf
import dev.orionlabs.orionmusic.common.models.Track
import dev.orionlabs.orionmusic.extensions.builtin.unified.UnifiedExtension.Companion.EXTENSION_ID

fun MediaStoreUtils.MAlbum.toAlbum() = Album(
    id.toString(),
    title ?: "Unknown",
    null,
    cover.toString().toResourceUriImageHolder(),
    artists.map { it.toArtist() },
    songList.size.toLong(),
    songList.sumOf { it.duration ?: 0 },
    albumYear?.toYearDate(),
    extras = mapOf(EXTENSION_ID to OfflineExtension.metadata.id)
)

fun MediaStoreUtils.MArtist?.toArtist() = Artist(
    this?.id.toString(),
    this?.title ?: "Unknown",
    this?.songList?.firstOrNull()?.cover,
    extras = mapOf(EXTENSION_ID to OfflineExtension.metadata.id)
)

fun MediaStoreUtils.MPlaylist.toPlaylist() = Playlist(
    id.toString(),
    title ?: "Unknown",
    true,
    isPrivate = true,
    cover = songList.firstOrNull()?.cover,
    authors = listOf(),
    trackCount = songList.size.toLong(),
    duration = songList.sumOf { it.duration ?: 0 },
    creationDate = modifiedDate.toDate(),
    description = description,
    extras = mapOf(EXTENSION_ID to OfflineExtension.metadata.id)
)

fun Long.toDate() = Date(this)

private fun MediaStoreUtils.FileNode.toSongList(): List<Track> =
    songList + folderList.values.flatMap { it.toSongList() }

fun MediaStoreUtils.FileNode.toShelf(
    context: Context,
    title: String?
): Shelf.Category = run {
    if (folderList.size == 1 && songList.isEmpty())
        return@run folderList.entries.first()
            .run { value.toShelf(context, "${title ?: folderName}/$key") }
    val itemSize = folderList.size + songList.size
    Shelf.Category(
        folderName,
        title ?: folderName,
        PagedData.Single {
            folderList.map {
                it.value.toShelf(context, it.key)
            } + songList.map { it.toShelf() }
        }.toFeed(
            Feed.Buttons(
                showPlayAndShuffle = true,
                customTrackList = toSongList()
            ),
            songList.firstOrNull()?.cover
        ),
        context.resources.runCatching {
            getQuantityString(R.plurals.number_items, itemSize, itemSize)
        }.getOrNull() ?: context.getString(R.string.n_items, itemSize),
        image = songList.firstOrNull()?.cover ?: R.drawable.ic_offline_files.toResourceImageHolder()
    )
}

fun MediaStoreUtils.Genre.toShelf(): Shelf {
    val id = id.toString()
    return Shelf.Lists.Tracks(
        id,
        title ?: "Unknown",
        songList.take(9),
        more = PagedData.Single<Shelf> { songList.map { it.toShelf() } }.toFeed()
    )
}
