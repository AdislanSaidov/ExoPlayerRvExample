package com.github.video

import android.app.Application
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import timber.log.Timber
import java.io.File

class App : Application() {

    lateinit var simpleCache: Cache

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        simpleCache = createCache()
    }

    private fun createCache(): Cache {
        val downloadContentDirectory = File(getExternalFilesDir(null) ?: filesDir, "downloads")
        return SimpleCache(
            downloadContentDirectory,
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(this)
        )
    }
}