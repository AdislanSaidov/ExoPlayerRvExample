package com.github.video.delegates

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.video.R
import com.github.video.databinding.ItemPhotoBinding
import com.github.video.models.ListItem
import com.github.video.models.PhotoUiModel
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun photoDelegate(
    addToFavClick: (ListItem) -> Unit
) = adapterDelegateViewBinding<PhotoUiModel, ListItem, ItemPhotoBinding>(
    { layoutInflater, root -> ItemPhotoBinding.inflate(layoutInflater, root, false) }
) {

    binding.ivAddToFavs.setOnClickListener { addToFavClick.invoke(item) }
    bind {
        val favRes = if (item.isFavorite) R.drawable.ic_favorite else R.drawable.ic_unfavorite
        binding.ivAddToFavs.setImageResource(favRes)
        Glide
            .with(context)
            .load(item.photoUrl)
            .transition(
                DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION)
            )
            .centerCrop()
            .into(binding.ivPhoto)
    }
}

private const val CROSS_FADE_DURATION = 500
