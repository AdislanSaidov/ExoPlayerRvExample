package com.github.video.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.video.getData
import com.github.video.models.ListItem
import com.github.video.models.PhotoUiModel
import com.github.video.models.VideoUiModel

class MainViewModel : ViewModel() {
    val mediaListState: MutableLiveData<List<ListItem>> = MutableLiveData()
    var lastId: Int = 0

    init {
        mediaListState.value = generateData()
    }

    fun onAddToFavClicked(listItem: ListItem) {
        mediaListState.value = mediaListState.value?.map { item ->
            if (item == listItem) {
                when (item) {
                    is VideoUiModel -> item.copy(isFavorite = !item.isFavorite)
                    is PhotoUiModel -> item.copy(isFavorite = !item.isFavorite)
                    else -> item
                }
            } else {
                item
            }
        }
    }

    fun onAddMediaClicked() {
        mediaListState.value = mediaListState.value.orEmpty() + generateData()
    }

    private fun generateData(): List<ListItem> {
        return getData().map { url ->
            lastId++
            if (url.endsWith("mp4")) {
                VideoUiModel(
                    id = lastId.toString(),
                    videoUrl = url
                )
            } else {
                PhotoUiModel(
                    id = lastId.toString(),
                    photoUrl = url
                )
            }
        }
    }
}