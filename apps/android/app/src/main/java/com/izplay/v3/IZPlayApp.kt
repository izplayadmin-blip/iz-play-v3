package com.izplay.v3

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.izplay.v3.BuildConfig
import com.izplay.v3.data.DownloadManager
import com.izplay.v3.data.DownloadStorage
import com.izplay.v3.data.FavoriteRepository
import com.izplay.v3.data.HiddenCategoryStore
import com.izplay.v3.data.LastPlaylistStore
import com.izplay.v3.data.M3uContentStore
import com.izplay.v3.data.M3uFavoriteStore
import com.izplay.v3.data.M3uImporter
import com.izplay.v3.data.PlayerPreferences
import com.izplay.v3.data.ProfileStore
import com.izplay.v3.data.PlaylistContentStore
import com.izplay.v3.data.PlaylistRepository
import com.izplay.v3.data.RatingManager
import com.izplay.v3.data.SeriesRepository
import com.izplay.v3.data.VodRepository
import com.izplay.v3.data.local.AppDatabase
import com.izplay.v3.data.local.LiveStreamEntity
import com.p2pengine.core.p2p.P2pConfig
import com.p2pengine.core.tracking.TrackerZone
import com.p2pengine.sdk.P2pEngine

/**
 * App-wide singleton holder.
 *
 * Mirrors the iOS `AppDatabase.shared` pattern: a single SQLite instance and
 * the repositories that wrap it live here, lazily created on first use.
 * The Composition tree reaches them via [com.izplay.v3.ui.LocalPlaylistRepository]
 * and [com.izplay.v3.ui.LocalPlaylistContentStore].
 */
class IZPlayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // O SDK nativo pode levar vários segundos para carregar em celulares
        // mais antigos. Nunca bloqueie a primeira frame/splash por causa do
        // P2P: enquanto inicializa, a reprodução continua com fallback direto.
        Thread(
            { initializeSwarmCloud() },
            "izplay-swarmcloud-init",
        ).start()
    }

    private fun initializeSwarmCloud() {
        val setTopBox = isTelevisionDevice()
        val token = BuildConfig.SWARMCLOUD_TOKEN
        if (token.isBlank()) {
            Log.i(TAG, "SwarmCloud disabled: no local token configured; setTopBox=$setTopBox")
            return
        }

        val config = P2pConfig.Builder()
            .trackerZone(TrackerZone.USA)
            .insertTimeOffsetTag(0.0)
            .isSetTopBox(setTopBox)
            .build()

        runCatching {
            P2pEngine.init(this, token, config)
            Log.i(TAG, "SwarmCloud initialized; setTopBox=$setTopBox")
        }.onFailure {
            // Never include the token or a provider URL in this message.
            Log.w(TAG, "SwarmCloud initialization failed; direct playback remains available", it)
        }
    }

    private fun isTelevisionDevice(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
            packageManager.hasSystemFeature("android.software.leanback")
    }

    private val database: AppDatabase by lazy { AppDatabase.get(this) }

    /**
     * Direct access for [com.izplay.v3.data.DownloadWorker]
     * which can't reach into the composition layer. Treat as package-private
     * even though Kotlin can't express that; mark with the
     * `ForDownloads` suffix so its single caller is obvious.
     */
    val appDatabaseForDownloads: AppDatabase get() = database

    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(database.playlistDao())
    }

    /**
     * Process-wide active-playlist catalog. iOS uses a `@MainActor`
     * singleton (`PlaylistContentStore.shared`); we hold one instance here
     * so the catalog survives back-stack pops.
     */
    val playlistContentStore: PlaylistContentStore by lazy {
        PlaylistContentStore(database, this)
    }

    /** Per-movie reads / writes for the detail screen. */
    val vodRepository: VodRepository by lazy {
        VodRepository(database.vodStreamDao())
    }

    /** Per-series reads / writes — drives `SeriesDetailScreen`. */
    val seriesRepository: SeriesRepository by lazy {
        SeriesRepository(database)
    }

    /** Star toggle + favorites list grids. */
    val favoriteRepository: FavoriteRepository by lazy {
        FavoriteRepository(database.favoriteDao())
    }

    /** Tracks which catalog categories the user has hidden per playlist. */
    val hiddenCategoryStore: HiddenCategoryStore by lazy {
        HiddenCategoryStore(this)
    }

    /**
     * Persists the playlist the user last opened. Mirrors iOS
     * `UserDefaults.lastPlaylistId` — read once at app launch to auto-open
     * the previous dashboard, cleared when the user navigates back to the
     * playlist list.
     */
    val lastPlaylistStore: LastPlaylistStore by lazy {
        LastPlaylistStore(this)
    }

    /** Active M3U playlist channel cache (counterpart of iOS `M3UContentStore.shared`). */
    val m3uContentStore: M3uContentStore by lazy { M3uContentStore(database) }

    /** Reactive M3U favourites set, driven by Room observations. */
    val m3uFavoriteStore: M3uFavoriteStore by lazy { M3uFavoriteStore(database) }

    /** "Replace whole playlist" import path used by the AddM3UPlaylistScreen. */
    val m3uImporter: M3uImporter by lazy { M3uImporter(database) }

    /** Filesystem layout helper for downloads — exposed for the WorkManager Worker. */
    val downloadStorageForWorker: DownloadStorage by lazy { DownloadStorage(this) }

    /** Orchestrator for VOD/episode downloads. iOS counterpart: `DownloadManager.shared`. */
    val downloadManager: DownloadManager by lazy {
        DownloadManager(this, database, downloadStorageForWorker)
    }

    /** App Store review prompt eligibility tracker. Holds *only* the eligibility decision. */
    val ratingManager: RatingManager by lazy { RatingManager(this) }

    /**
     * Player UX preferences (PiP, background playback, long-press 2× speed).
     * iOS counterpart: the `UserDefaults.register(defaults:)` block in the
     * app delegate.
     */
    val playerPreferences: PlayerPreferences by lazy { PlayerPreferences(this) }
    val profileStore: ProfileStore by lazy { ProfileStore(this) }

    /**
     * Single-row live-stream lookup. Used by [ui.player.PlayerViewModel] to
     * resolve a channel id → name + (optional) extension before handing the
     * URL to libmpv. Skipping the dedicated `LiveStreamRepository` wrapper —
     * everything else for live streams flows through `PlaylistContentStore`'s
     * bulk catalogue feeds.
     */
    suspend fun findLiveStream(streamId: Int, playlistId: String): LiveStreamEntity? =
        database.liveStreamDao().findById(streamId, playlistId)

    private companion object {
        const val TAG = "IZPlayApp"
    }
}
