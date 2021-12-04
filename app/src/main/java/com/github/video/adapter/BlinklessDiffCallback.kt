package com.github.video.adapter

import androidx.recyclerview.widget.DiffUtil
import com.github.video.models.ListItem

class BlinklessDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        oldItem.areItemsTheSame(newItem)

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        oldItem.areContentsTheSame(newItem)

    override fun getChangePayload(oldItem: ListItem, newItem: ListItem) = Any()
}
