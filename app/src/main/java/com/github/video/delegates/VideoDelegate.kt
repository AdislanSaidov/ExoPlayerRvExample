package com.github.video.delegates

import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.video.R
import com.github.video.databinding.ItemVideoBinding
import com.github.video.models.ListItem
import com.github.video.models.VideoUiModel
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun videoDelegate(
    addToFavClick: (ListItem) -> Unit
) = adapterDelegateViewBinding<VideoUiModel, ListItem, ItemVideoBinding>(
    { layoutInflater, root -> ItemVideoBinding.inflate(layoutInflater, root, false) }
) {

    binding.ivAddToFavs.setOnClickListener { addToFavClick.invoke(item) }

    bind {
        val favRes = if (item.isFavorite) R.drawable.ic_favorite else R.drawable.ic_unfavorite
        binding.ivAddToFavs.setImageResource(favRes)
        Glide.with(itemView.context).asBitmap()
            .load(Uri.parse(item.videoUrl))
            .transition(
                BitmapTransitionOptions.withCrossFade(CROSS_FADE_DURATION)
            )
            .apply(RequestOptions().frame(FIRST_FRAME_MICROS))
            .centerCrop()
            .into(binding.ivVideoPreview)
    }
}

private const val FIRST_FRAME_MICROS = 1000L
private const val CROSS_FADE_DURATION = 500
