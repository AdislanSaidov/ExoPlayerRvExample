package com.github.video.models

interface ListItem {
    fun areItemsTheSame(newItem: ListItem): Boolean

    fun areContentsTheSame(newItem: ListItem): Boolean = this == newItem
}