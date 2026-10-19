package dev.orionlabs.orionmusic.ui.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.orionlabs.orionmusic.common.Extension
import dev.orionlabs.orionmusic.common.clients.LoginClient
import dev.orionlabs.orionmusic.common.clients.SettingsChangeListenerClient
import dev.orionlabs.orionmusic.common.models.ExtensionType
import dev.orionlabs.orionmusic.common.providers.SettingsProvider
import dev.orionlabs.orionmusic.common.settings.Setting
import dev.orionlabs.orionmusic.common.settings.Settings
import dev.orionlabs.orionmusic.di.App
import dev.orionlabs.orionmusic.extensions.ExtensionLoader
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.get
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.getIf
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.getOrThrow
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.isClient
import dev.orionlabs.orionmusic.extensions.ExtensionUtils.runIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

class ExtensionInfoViewModel(
    val app: App,
    val extensionLoader: ExtensionLoader,
    val type: ExtensionType,
    val id: String,
) : ViewModel() {

    private val reload = MutableSharedFlow<Unit>(1).also {
        viewModelScope.launch { it.emit(Unit) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val stateFlow = extensionLoader.getFlow(type).combine(reload) { a, _ -> a }
        .transformLatest { list ->
            emit(null)
            val ext = list.find { it.id == id } ?: return@transformLatest
            emit(
                State(
                    ext,
                    ext.isClient<LoginClient>(),
                    ext.isClient<SettingsChangeListenerClient>(),
                    ext.getIf<SettingsProvider, List<Setting>>(app.throwFlow) {
                        getSettingItems()
                    }.orEmpty()
                )
            )
        }.stateIn(viewModelScope, Eagerly, null)

    data class State(
        val extension: Extension<*>?,
        val isLoginClient: Boolean,
        val isPlaylistEditClient: Boolean,
        val settings: List<Setting>,
    )

    fun onSettingsChanged(settings: Settings, key: String?) = viewModelScope.launch {
        stateFlow.value?.extension?.runIf<SettingsChangeListenerClient>(app.throwFlow) {
            onSettingsChanged(settings, key)
        }
    }

    fun onSettingsClick(onClick: suspend () -> Unit) = viewModelScope.launch {
        stateFlow.value?.extension?.get { onClick() }?.getOrThrow(app.throwFlow)
    }
}
