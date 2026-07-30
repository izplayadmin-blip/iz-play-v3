package com.izplay.v3

import android.content.res.Configuration
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izplay.v3.player.LocalPlayerActivityState
import com.izplay.v3.player.PlayerActivityState
import com.izplay.v3.networking.AppUpdateClient
import com.izplay.v3.networking.AppUpdateInstaller
import com.izplay.v3.networking.UpdateDownloadState
import com.izplay.v3.ui.AppNavigation
import com.izplay.v3.ui.LocalFavoriteRepository
import com.izplay.v3.ui.LocalHiddenCategoryStore
import com.izplay.v3.ui.LocalLastPlaylistStore
import com.izplay.v3.ui.LocalPlayerPreferences
import com.izplay.v3.ui.LocalProfileStore
import com.izplay.v3.ui.LocalPlaylistContentStore
import com.izplay.v3.ui.LocalPlaylistRepository
import com.izplay.v3.ui.LocalSeriesRepository
import com.izplay.v3.ui.LocalVodRepository
import com.izplay.v3.ui.theme.IZPlayTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** Shared between Activity lifecycle hooks and PlayerScreen. */
    private val playerState = PlayerActivityState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        val app = application as IZPlayApp
        setContent {
            IZPlayTheme {
                var availableUpdate by remember { mutableStateOf<com.izplay.v3.networking.AppUpdate?>(null) }
                var updateState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
                val updateScope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    availableUpdate = AppUpdateClient.check()
                }
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
                    LocalProfileStore provides app.profileStore,
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                    }
                }
                availableUpdate?.let { update ->
                    AlertDialog(
                        onDismissRequest = {
                            if (!update.mandatory) availableUpdate = null
                        },
                        title = { Text("Nova versão disponível") },
                        text = {
                            Column {
                                Text(
                                    buildString {
                                        append("IZ Play Mobile ")
                                        append(update.versionName)
                                        if (update.releaseNotes.isNotBlank()) {
                                            append("\n\n")
                                            append(update.releaseNotes)
                                        }
                                    }
                                )
                                when (val state = updateState) {
                                    is UpdateDownloadState.Downloading -> {
                                        val progress = if (state.total > 0) {
                                            (state.downloaded.toFloat() / state.total).coerceIn(0f, 1f)
                                        } else null
                                        Spacer(Modifier.height(16.dp))
                                        if (progress != null) {
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(5.dp),
                                            )
                                            Text("${(progress * 100).toInt()}% baixado")
                                        } else {
                                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(5.dp))
                                            Text("Baixando atualização…")
                                        }
                                    }
                                    UpdateDownloadState.Verifying -> Text("\nVerificando integridade e assinatura…")
                                    is UpdateDownloadState.Failed -> Text(
                                        "\n${state.message}",
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                    )
                                    is UpdateDownloadState.Ready -> Text("\nAtualização verificada e pronta para instalar.")
                                    UpdateDownloadState.Idle -> Unit
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                when (val state = updateState) {
                                    is UpdateDownloadState.Ready -> {
                                        AppUpdateInstaller.requestInstall(this@MainActivity, state.file)
                                    }
                                    is UpdateDownloadState.Downloading,
                                    UpdateDownloadState.Verifying -> Unit
                                    else -> updateScope.launch {
                                        updateState = AppUpdateInstaller.downloadAndVerify(
                                            activity = this@MainActivity,
                                            update = update,
                                            onProgress = { updateState = it },
                                        )
                                        val ready = updateState as? UpdateDownloadState.Ready
                                        if (ready != null) {
                                            AppUpdateInstaller.requestInstall(this@MainActivity, ready.file)
                                        }
                                    }
                                }
                            }) {
                                Text(
                                    when (updateState) {
                                        is UpdateDownloadState.Downloading -> "Baixando…"
                                        UpdateDownloadState.Verifying -> "Verificando…"
                                        is UpdateDownloadState.Ready -> "Instalar atualização"
                                        is UpdateDownloadState.Failed -> "Tentar novamente"
                                        UpdateDownloadState.Idle -> "Atualizar agora"
                                    }
                                )
                            }
                        },
                        dismissButton = if (update.mandatory) null else {
                            {
                                TextButton(onClick = { availableUpdate = null }) {
                                    Text("Depois")
                                }
                            }
                        },
                        properties = DialogProperties(
                            dismissOnBackPress = !update.mandatory,
                            dismissOnClickOutside = !update.mandatory,
                        ),
                    )
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
