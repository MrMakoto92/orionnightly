package dev.orionlabs.orionmusic.ui.download

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.LoadState
import dev.orionlabs.orionmusic.R
import dev.orionlabs.orionmusic.common.models.Feed
import dev.orionlabs.orionmusic.databinding.FragmentDownloadBinding
import dev.orionlabs.orionmusic.extensions.builtin.unified.UnifiedExtension
import dev.orionlabs.orionmusic.extensions.builtin.unified.UnifiedExtension.Companion.getFeed
import dev.orionlabs.orionmusic.ui.common.ExceptionFragment
import dev.orionlabs.orionmusic.ui.common.ExceptionUtils
import dev.orionlabs.orionmusic.ui.common.FragmentUtils.openFragment
import dev.orionlabs.orionmusic.ui.common.GridAdapter
import dev.orionlabs.orionmusic.ui.common.GridAdapter.Companion.configureGridLayout
import dev.orionlabs.orionmusic.ui.common.UiViewModel.Companion.applyBackPressCallback
import dev.orionlabs.orionmusic.ui.common.UiViewModel.Companion.applyContentInsets
import dev.orionlabs.orionmusic.ui.common.UiViewModel.Companion.applyFabInsets
import dev.orionlabs.orionmusic.ui.common.UiViewModel.Companion.applyInsets
import dev.orionlabs.orionmusic.ui.download.DownloadsAdapter.Companion.toItems
import dev.orionlabs.orionmusic.ui.feed.FeedAdapter.Companion.getFeedAdapter
import dev.orionlabs.orionmusic.ui.feed.FeedAdapter.Companion.getTouchHelper
import dev.orionlabs.orionmusic.ui.feed.FeedClickListener.Companion.getFeedListener
import dev.orionlabs.orionmusic.ui.feed.FeedData
import dev.orionlabs.orionmusic.ui.feed.FeedViewModel
import dev.orionlabs.orionmusic.ui.media.LineAdapter
import dev.orionlabs.orionmusic.utils.ContextUtils.observe
import dev.orionlabs.orionmusic.utils.ui.AnimationUtils.setupTransition
import dev.orionlabs.orionmusic.utils.ui.FastScrollerHelper
import dev.orionlabs.orionmusic.utils.ui.UiUtils.configureAppBar
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadFragment : Fragment(R.layout.fragment_download) {

    private val vm by viewModel<DownloadViewModel>()
    private val downloadsAdapter by lazy {
        DownloadsAdapter(object : DownloadsAdapter.Listener {
            override fun onCancel(trackId: Long) = vm.cancel(trackId)
            override fun onRestart(trackId: Long) = vm.restart(trackId)
            override fun onExceptionClicked(data: ExceptionUtils.Data) = requireActivity()
                .openFragment<ExceptionFragment>(null, ExceptionFragment.getBundle(data))
        })
    }

    private val feedViewModel by viewModel<FeedViewModel>()
    private val feedData by lazy {
        val flow = vm.downloader.unified.downloadFeed
        feedViewModel.getFeedData(
            "downloads", Feed.Buttons(), false, flow
        ) {
            val feed = requireContext().getFeed(flow.value)
            FeedData.State(UnifiedExtension.metadata.id, null, feed)
        }
    }

    private val feedListener by lazy { getFeedListener() }
    private val feedAdapter by lazy {
        getFeedAdapter(feedData, feedListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDownloadBinding.bind(view)
        setupTransition(view)
        applyBackPressCallback()
        binding.appBarLayout.configureAppBar { offset ->
            binding.toolbarOutline.alpha = offset
            binding.iconContainer.alpha = 1 - offset
        }
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        applyInsets {
            binding.recyclerView.applyContentInsets(it, 20, 8, 72)
            binding.fabContainer.applyFabInsets(it, systemInsets.value)
        }
        FastScrollerHelper.applyTo(binding.recyclerView)
        val lineAdapter = LineAdapter()
        binding.fabCancel.setOnClickListener {
            vm.cancelAll()
        }
        binding.recyclerView.itemAnimator = null
        getTouchHelper(feedListener).attachToRecyclerView(binding.recyclerView)
        configureGridLayout(
            binding.recyclerView,
            GridAdapter.Concat(
                downloadsAdapter,
                lineAdapter,
                feedAdapter.withLoading(this)
            )
        )
        observe(vm.flow) { infos ->
            binding.fabCancel.isVisible = infos.any { it.download.finalFile == null }
            lineAdapter.loadState = if (infos.isNotEmpty()) LoadState.Loading
            else LoadState.NotLoading(false)
            downloadsAdapter.submitList(infos.toItems(vm.extensions.music.value))
        }
    }
}
