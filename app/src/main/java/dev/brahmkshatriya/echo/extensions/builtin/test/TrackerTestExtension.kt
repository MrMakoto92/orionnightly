package dev.orionlabs.orionmusic.extensions.builtin.test

import dev.orionlabs.orionmusic.common.clients.TrackerMarkClient
import dev.orionlabs.orionmusic.common.models.ExtensionType
import dev.orionlabs.orionmusic.common.models.ImportType
import dev.orionlabs.orionmusic.common.models.Metadata
import dev.orionlabs.orionmusic.common.models.TrackDetails
import dev.orionlabs.orionmusic.common.settings.Setting
import dev.orionlabs.orionmusic.common.settings.Settings

class TrackerTestExtension : TrackerMarkClient {
    companion object {
        val metadata = Metadata(
            "TrackerTestExtension",
            "",
            ImportType.BuiltIn,
            ExtensionType.TRACKER,
            "test",
            "Tracker Test Extension",
            "1.0.0",
            "Test extension for offline testing",
            "Test",
        )
    }

    override suspend fun getSettingItems() = listOf<Setting>()
    override fun setSettings(settings: Settings) {}

    override suspend fun onTrackChanged(details: TrackDetails?) {
        println("onTrackChanged ${details?.track?.id}")
    }

    override suspend fun getMarkAsPlayedDuration(details: TrackDetails): Long? {
        return details.totalDuration?.div(3)
    }

    override suspend fun onMarkAsPlayed(details: TrackDetails) {
        println("onMarkAsPlayed: ${details.track.id}")
    }

    override suspend fun onPlayingStateChanged(details: TrackDetails?, isPlaying: Boolean) {
        println("onPlayingStateChanged $isPlaying: ${details?.track?.id}")
    }
}
