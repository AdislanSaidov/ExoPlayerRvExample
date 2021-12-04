package com.github.video.models

import com.github.video.recycler.VideoItem

data class VideoUiModel(
    val id: String,
    override val videoUrl: String,
    val isFavorite: Boolean = false
) : ListItem, VideoItem {
    override fun areItemsTheSame(newItem: ListItem): Boolean =
        newItem is VideoUiModel && this.id == newItem.id
}