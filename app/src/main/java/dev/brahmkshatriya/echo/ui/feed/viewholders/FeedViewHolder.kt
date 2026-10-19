package dev.orionlabs.orionmusic.ui.feed.viewholders

import android.view.View
import dev.orionlabs.orionmusic.playback.PlayerState
import dev.orionlabs.orionmusic.ui.feed.FeedType
import dev.orionlabs.orionmusic.utils.ui.scrolling.ScrollAnimViewHolder

sealed class FeedViewHolder<T : FeedType>(view: View) : ScrollAnimViewHolder(view) {
    abstract fun bind(feed: T)
    open fun canBeSwiped(): Boolean = false
    open fun onSwipe(): T? = null
    open fun onCurrentChanged(current: PlayerState.Current?) {}
}
