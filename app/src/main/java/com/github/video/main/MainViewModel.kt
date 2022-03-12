package com.github.video.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.video.getData
import com.github.video.models.ListItem
import com.github.video.models.PhotoUiModel
import com.github.video.models.VideoUiModel

class MainViewModel : ViewModel() {
    private val _mediaListState: MutableLiveData<List<ListItem>> = MutableLiveData()
    val mediaListState: LiveData<List<ListItem>> get() = _mediaListState
    private var lastId: Int = 0

    init {
        _mediaListState.value = generateData()
    }

    fun onAddToFavClicked(listItem: ListItem) {
        _mediaListState.value = _mediaListState.value?.map { item ->
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
        _mediaListState.value = _mediaListState.value.orEmpty() + generateData()
    }

    private fun generateData(): List<ListItem> {
        return getData().map { url ->
            lastId++
            if (url.endsWith(MP4_EXTENSION)) {
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

private const val MP4_EXTENSION = "mp4"
