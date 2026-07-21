package com.izplay.v3

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.izplay.v3.player.LocalPlayerActivityState
import com.izplay.v3.player.PlayerActivityState
import com.izplay.v3.ui.AppNavigation
import com.izplay.v3.ui.LocalFavoriteRepository
import com.izplay.v3.ui.LocalHiddenCategoryStore
import com.izplay.v3.ui.LocalLastPlaylistStore
import com.izplay.v3.ui.LocalPlayerPreferences
import com.izplay.v3.ui.LocalPlaylistContentStore
import com.izplay.v3.ui.LocalPlaylistRepository
import com.izplay.v3.ui.LocalSeriesRepository
import com.izplay.v3.ui.LocalVodRepository
import com.izplay.v3.ui.theme.IZPlayTheme

class MainActivity : ComponentActivity() {

    /** Shared between Activity lifecycle hooks and PlayerScreen. */
    private val playerState = PlayerActivityState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as IZPlayApp
        setContent {
            IZPlayTheme {
                // Push brightness-override changes onto the window. Setting
                // `screenBrightness = -1f` restores the system value (iOS
                // `UIScreen.main.brightness` doesn't need this gymnastics).
                LaunchedEffect(Unit) {
                    snapshotFlow { playerState.brightnessOverride.value }
                        .collect { override ->
                            val lp = window.attributes
                            lp.screenBrightness = override ?: -1f
                            window.attributes = lp
                        }
                }
                CompositionLocalProvider(
                    LocalPlaylistRepository provides app.playlistRepository,
                    LocalPlaylistContentStore provides app.playlistContentStore,
                    LocalVodRepository provides app.vodRepository,
                    LocalSeriesRepository provides app.seriesRepository,
                    LocalFavoriteRepository provides app.favoriteRepository,
                    LocalHiddenCategoryStore provides app.hiddenCategoryStore,
                    LocalLastPlaylistStore provides app.lastPlaylistStore,
                    LocalPlayerActivityState provides playerState,
                    LocalPlayerPreferences provides app.playerPreferences,
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                    }
                }
            }
        }
    }

    /**
     * `onUserLeaveHint` is the Android equivalent of the iOS
     * `willResignActive` notification — fires when the user hits home /
     * recents but NOT when an incoming activity covers us (call, dialog).
     * That's exactly the moment we want to auto-enter PiP for active playback.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Bump the trigger; PlayerScreen observes via snapshotFlow and calls
        // `enterPip(aspect)` once it's confirmed playback is established.
        playerState.pipTrigger.value++
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        playerState.isInPip.value = isInPictureInPictureMode
    }
}
