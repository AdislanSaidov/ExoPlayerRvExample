package com.github.video.adapter

import com.github.video.delegates.photoDelegate
import com.github.video.delegates.videoDelegate
import com.github.video.models.ListItem
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

class MediaAdapter(
    addToFavClick: (ListItem) -> Unit
) : AsyncListDifferDelegationAdapter<ListItem>(BlinklessDiffCallback()) {

    init {
        with(delegatesManager) {
            addDelegate(photoDelegate(addToFavClick))
            addDelegate(videoDelegate(addToFavClick))
        }
    }
}
