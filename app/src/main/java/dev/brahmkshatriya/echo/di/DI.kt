package dev.orionlabs.orionmusic.di

import dev.orionlabs.orionmusic.download.DownloadWorker
import dev.orionlabs.orionmusic.download.Downloader
import dev.orionlabs.orionmusic.download.db.DownloadDatabase
import dev.orionlabs.orionmusic.extensions.ExtensionLoader
import dev.orionlabs.orionmusic.playback.PlayerService
import dev.orionlabs.orionmusic.playback.PlayerState
import dev.orionlabs.orionmusic.ui.common.SnackBarHandler
import dev.orionlabs.orionmusic.ui.common.UiViewModel
import dev.orionlabs.orionmusic.ui.download.DownloadViewModel
import dev.orionlabs.orionmusic.ui.extensions.ExtensionInfoViewModel
import dev.orionlabs.orionmusic.ui.extensions.ExtensionsViewModel
import dev.orionlabs.orionmusic.ui.extensions.add.AddViewModel
import dev.orionlabs.orionmusic.ui.extensions.login.LoginUserListViewModel
import dev.orionlabs.orionmusic.ui.extensions.login.LoginViewModel
import dev.orionlabs.orionmusic.ui.feed.FeedViewModel
import dev.orionlabs.orionmusic.ui.main.search.SearchViewModel
import dev.orionlabs.orionmusic.ui.media.MediaViewModel
import dev.orionlabs.orionmusic.ui.player.PlayerViewModel
import dev.orionlabs.orionmusic.ui.player.more.info.TrackInfoViewModel
import dev.orionlabs.orionmusic.ui.player.more.lyrics.LyricsViewModel
import dev.orionlabs.orionmusic.ui.playlist.create.CreatePlaylistViewModel
import dev.orionlabs.orionmusic.ui.playlist.delete.DeletePlaylistViewModel
import dev.orionlabs.orionmusic.ui.playlist.edit.EditPlaylistViewModel
import dev.orionlabs.orionmusic.ui.playlist.save.SaveToPlaylistViewModel
import dev.orionlabs.orionmusic.utils.ContextUtils.getSettings
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object DI {

    private val baseModule = module {
        single { androidApplication().getSettings() }
        singleOf(::App)
    }

    private val extensionModule = module {
        includes(baseModule)
        singleOf(::ExtensionLoader)
    }

    private val downloadModule = module {
        includes(extensionModule)
        singleOf(DownloadDatabase::create)
        singleOf(::Downloader)
        workerOf(::DownloadWorker)
    }

    private val playerModule = module {
        includes(extensionModule)
        singleOf(PlayerService::getCache)
        single { PlayerState() }
    }

    private val uiModules = module {
        singleOf(::SnackBarHandler)
        viewModelOf(::UiViewModel)

        viewModelOf(::PlayerViewModel)
        viewModelOf(::LyricsViewModel)
        viewModelOf(::TrackInfoViewModel)

        viewModelOf(::ExtensionsViewModel)
        viewModelOf(::ExtensionInfoViewModel)
        viewModelOf(::LoginUserListViewModel)
        viewModelOf(::AddViewModel)
        viewModelOf(::LoginViewModel)

        viewModelOf(::FeedViewModel)
        viewModelOf(::SearchViewModel)
        viewModelOf(::MediaViewModel)

        viewModelOf(::CreatePlaylistViewModel)
        viewModelOf(::DeletePlaylistViewModel)
        viewModelOf(::SaveToPlaylistViewModel)
        viewModelOf(::EditPlaylistViewModel)

        viewModelOf(::DownloadViewModel)
    }

    val appModule = module {
        includes(baseModule)
        includes(extensionModule)
        includes(playerModule)
        includes(downloadModule)
        includes(uiModules)
    }
}
