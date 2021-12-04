package com.github.video.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.video.App
import com.github.video.adapter.MediaAdapter
import com.github.video.databinding.ActivityMainBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaAdapter = MediaAdapter(
            addToFavClick = viewModel::onAddToFavClicked
        )

        binding.rvMedia.apply {
            adapter = mediaAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            init(
                ExoPlayer.Builder(this@MainActivity).build(),
                mediaAdapter::getItems,
                createDataSource()
            )
            update()
        }

        binding.fabAdd.setOnClickListener {
            viewModel.onAddMediaClicked()
        }

        viewModel.mediaListState.observe(this) {
            mediaAdapter.items = it
        }
    }

    private fun createDataSource(): CacheDataSource.Factory {
        val cache = (application as App).simpleCache
        val upstreamFactory = DefaultDataSource.Factory(this)

        val cacheWriteDataSinkFactory =
            CacheDataSink.Factory().setCache(cache).setFragmentSize(C.LENGTH_UNSET.toLong())
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(cacheWriteDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    override fun onResume() {
        super.onResume()
        binding.rvMedia.resumePlayer()
    }

    override fun onPause() {
        binding.rvMedia.pausePlayer()
        super.onPause()
    }

    override fun onDestroy() {
        binding.rvMedia.releasePlayer()
        super.onDestroy()
    }
}