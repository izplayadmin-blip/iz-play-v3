package com.izplay.v3.player

import android.net.Uri
import android.util.Log
import com.p2pengine.sdk.P2pEngine

/**
 * Converts compatible HLS/DASH sources to the SwarmCloud loopback URL.
 *
 * The original URL is retained only when a local proxy URL was actually
 * produced, allowing [MPVPlayer] to retry direct playback once.
 */
object SwarmCloudManager {

    data class ResolvedUrl(
        val playbackUrl: String,
        val directFallbackUrl: String? = null,
    )

    fun resolvePlaybackUrl(originalUrl: String): ResolvedUrl {
        if (!originalUrl.isP2pCompatibleStream()) {
            Log.i(TAG, "Stream bypassed SwarmCloud: unsupported manifest type")
            return ResolvedUrl(originalUrl)
        }

        val parsedUrl = runCatching {
            P2pEngine.instance?.parseStreamUrl(originalUrl)
        }.onFailure {
            Log.w(TAG, "SwarmCloud URL resolution failed; using direct playback")
        }.getOrNull()

        return if (!parsedUrl.isNullOrBlank() && parsedUrl != originalUrl) {
            val host = runCatching { Uri.parse(parsedUrl).host }.getOrNull()
            val isLoopback = host == "127.0.0.1" || host == "localhost" || host == "::1"
            Log.i(TAG, "SwarmCloud playback URL resolved; loopback=$isLoopback")
            ResolvedUrl(
                playbackUrl = parsedUrl,
                directFallbackUrl = originalUrl,
            )
        } else {
            Log.i(TAG, "SwarmCloud unavailable; using direct playback")
            ResolvedUrl(originalUrl)
        }
    }

    private const val TAG = "SwarmCloud"
}

internal fun String.isP2pCompatibleStream(): Boolean {
    val cleanUrl = substringBefore('#').substringBefore('?').lowercase()
    return cleanUrl.endsWith(".m3u8") || cleanUrl.endsWith(".mpd")
}
