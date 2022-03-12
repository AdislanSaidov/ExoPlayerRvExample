package com.github.video.recycler

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.video.R
import com.github.video.databinding.ItemPlayerViewBinding
import com.github.video.getFirstY
import com.github.video.models.ListItem
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.android.parcel.Parcelize
import java.util.Collections
import kotlin.math.abs

class VideoPlayerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var ivVideoPreview: ImageView? = null
    private var flContainer: FrameLayout? = null
    private var playerView: PlayerView? = null
    private var player: ExoPlayer? = null

    private var playPosition: Int = NO_POSITION
    private var targetPosition: Int = NO_POSITION
    private var isVideoViewAdded: Boolean = false
    private val appContext: Context = context.applicationContext
    private val videoStates: MutableMap<Int, Long> = mutableMapOf()
    private lateinit var dataSourceFactory: DataSource.Factory

    private lateinit var itemsCallback: () -> MutableList<ListItem>

    fun init(
        videoPlayer: ExoPlayer,
        itemsCallback: () -> MutableList<ListItem>,
        dataSourceFactory: DataSource.Factory
    ) {
        this.itemsCallback = itemsCallback
        this.dataSourceFactory = dataSourceFactory

        val inflater = LayoutInflater.from(appContext)
        playerView = ItemPlayerViewBinding.inflate(inflater).root

        player = videoPlayer.also {
            it.repeatMode = Player.REPEAT_MODE_ONE
            it.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            playerView?.visibility = VISIBLE
                        }
                        Player.STATE_BUFFERING -> {
                            ivVideoPreview?.visibility = VISIBLE
                        }
                        Player.STATE_ENDED -> {}
                        Player.STATE_IDLE -> {}
                    }
                }
            })
        }
    }

    override fun onScrollStateChanged(state: Int) {
        if (state == SCROLL_STATE_IDLE) {
            tryToPlayVideo()
        }
    }

    fun update() {
        targetPosition = NO_POSITION
        playPosition = NO_POSITION
        postDelayed({
            tryToPlayVideo()
        }, ITEMS_READY_DELAY)
    }

    private fun tryToPlayVideo() {
        this.parent ?: return
        val gridLayoutManager = layoutManager as? GridLayoutManager
            ?: error("a layoutManager must be a GridLayoutManager")
        // Find visible VideoItems
        val startPosition: Int = gridLayoutManager.findFirstVisibleItemPosition()
        val endPosition: Int = gridLayoutManager.findLastVisibleItemPosition()

        val items = itemsCallback()
        if (startPosition < 0 || endPosition < 0 || startPosition >= items.size || endPosition >= items.size) {
            return
        }

        for (pos in startPosition..endPosition) {
            if (items.getOrNull(pos) is VideoItem && canPlayVideoAtPosition(pos)) {
                if (pos != playPosition) {
                    stopVideo()
                    targetPosition = pos
                    playVideo()
                }
                break
            }
        }
    }

    private fun canPlayVideoAtPosition(position: Int): Boolean {
        val parentFirstY = getFirstY()
        val gridLayoutManager = layoutManager as GridLayoutManager
        val view: View = gridLayoutManager.findViewByPosition(position) ?: return false
        val firstY = view.getFirstY()
        val decoratedTop = gridLayoutManager.getDecoratedTop(view)
        val decoratedBottom = gridLayoutManager.getDecoratedBottom(view)
        val fullHeight = decoratedBottom - decoratedTop
        val start = firstY - parentFirstY
        val neededHeight = -abs(fullHeight * 0.7)
        // If 70% of card's height went over the bound, we need to miss it
        return start > neededHeight
    }

    private fun stopVideo() {
        // Remove PlayerView
        ivVideoPreview?.visibility = VISIBLE
        playerView?.let {
            removePlayerView(it)
            it.visibility = INVISIBLE
        }
        player?.let {
            it.pause()
            // Keep current position of video
            videoStates[playPosition] = it.currentPosition
            targetPosition = NO_POSITION
            playPosition = NO_POSITION
        }
    }

    private fun playVideo() {
        val holder = findViewHolderForAdapterPosition(targetPosition) ?: return

        flContainer = holder.itemView.findViewById(R.id.fl_video_container)
        ivVideoPreview = holder.itemView.findViewById(R.id.iv_video_preview)

        val videoItem = itemsCallback().getOrNull(targetPosition) as? VideoItem
        val videoUrl = videoItem?.videoUrl ?: return
        playerView?.player = player
        val videoSource = MediaItem.Builder().setUri(videoUrl)
            // Video with lower quality
            .setStreamKeys(Collections.singletonList(StreamKey(0, 0, 0)))
            .build()
        ivVideoPreview?.visibility = GONE
        if (!isVideoViewAdded) {
            addPlayerView()
        }

        player?.run {
            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoSource)
            setMediaSource(mediaSource)

            prepare()
            play()
            // Restore video position
            videoStates[targetPosition]?.let {
                seekTo(it)
            }
            playPosition = targetPosition
        }
    }

    private fun removePlayerView(videoView: PlayerView) {
        val parent = videoView.parent as? ViewGroup ?: return
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addPlayerView() {
        flContainer?.let {
            it.addView(playerView)
            isVideoViewAdded = true
            ivVideoPreview?.visibility = GONE
            playerView?.visibility = VISIBLE
        }
    }

    private fun releasePlayer() {
        player?.run {
            release()
            player = null
        }
        playerView = null
        flContainer = null
        ivVideoPreview = null
    }

    fun pausePlayer() {
        if (playPosition != NO_POSITION) {
            player?.pause()
        }
    }

    fun resumePlayer() {
        if (playPosition != NO_POSITION) {
            player?.play()
        }
    }

    override fun onDetachedFromWindow() {
        player?.let {
            it.pause()
            videoStates[playPosition] = it.currentPosition
        }
        releasePlayer()
        super.onDetachedFromWindow()
    }

    @Parcelize
    class State(
        val superSavedState: Parcelable?,
        val videoStates: MutableMap<Int, Long>,
        val targetPosition: Int
    ) : View.BaseSavedState(superSavedState), Parcelable

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return State(
            superSavedState = superState,
            videoStates = videoStates,
            targetPosition = targetPosition
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? State
        super.onRestoreInstanceState(savedState?.superSavedState ?: state)
        savedState?.let {
            this.videoStates.clear()
            this.videoStates.putAll(it.videoStates)
            this.targetPosition = it.targetPosition
        }
    }
}

private const val ITEMS_READY_DELAY = 1000L

interface VideoItem {
    val videoUrl: String
}
