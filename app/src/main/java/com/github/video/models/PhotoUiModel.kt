package com.github.video.models

data class PhotoUiModel(
    val id: String,
    val photoUrl: String,
    val isFavorite: Boolean = false
) : ListItem {
    override fun areItemsTheSame(newItem: ListItem): Boolean =
        newItem is PhotoUiModel && this.id == newItem.id
}