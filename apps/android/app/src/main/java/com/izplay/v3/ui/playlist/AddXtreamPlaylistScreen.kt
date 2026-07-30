package com.izplay.v3.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.izplay.v3.model.Playlist
import com.izplay.v3.model.PlaylistKind
import com.izplay.v3.networking.XtreamApiClient
import com.izplay.v3.ui.LocalPlaylistContentStore
import com.izplay.v3.ui.LocalPlaylistRepository
import com.izplay.v3.ui.components.ModalSlideContainer
import com.izplay.v3.ui.components.SavingOverlay
import com.izplay.v3.ui.components.SectionHeader
import com.izplay.v3.ui.design.IzTheme
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.izplay.v3.ui.design.focus.IzFocusRing
import com.izplay.v3.ui.design.focus.izFocusVisuals
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Form for adding or editing an Xtream Codes playlist — the Android counterpart
 * of the iOS `AddPlaylistView`. Verification is mocked for now (no network call).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddXtreamPlaylistScreen(
    onCancel: () -> Unit,
    onSaved: (String) -> Unit,
    editingPlaylistId: String? = null,
) {
    val repository = LocalPlaylistRepository.current
    val contentStore = LocalPlaylistContentStore.current
    // `editing` is loaded asynchronously from SQLite when an id is provided;
    // the form fields populate as soon as the row arrives.
    var editing by remember(editingPlaylistId) { mutableStateOf<Playlist?>(null) }
    LaunchedEffect(editingPlaylistId) {
        editing = editingPlaylistId?.let { repository.find(it) }
    }

    var name by remember(editing?.id) { mutableStateOf(editing?.name ?: "") }
    var serverUrl by remember(editing?.id) { mutableStateOf(editing?.serverUrl ?: "http://") }
    var username by remember(editing?.id) { mutableStateOf(editing?.username ?: "") }
    var password by remember(editing?.id) { mutableStateOf(editing?.password ?: "") }
    var filterAdultContent by remember(editing?.id) { mutableStateOf(editing?.filterAdultContent ?: false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    // Progress text shown inside the blocking overlay while verify/sync runs.
    // Mirrors iOS `progressMessage` on `AddPlaylistView`.
    var progressMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // DNS fixo (login V2: só usuário/senha). Vazio => campos completos.
    val defaultDns = com.izplay.v3.BuildConfig.DEFAULT_DNS
    val useFixedDns = defaultDns.isNotBlank() && editing == null
    LaunchedEffect(useFixedDns) {
        if (useFixedDns && (serverUrl.isBlank() || serverUrl == "http://")) {
            serverUrl = defaultDns
        }
    }

    val isValid = (name.isNotBlank() || useFixedDns) &&
        serverUrl.isNotBlank() && serverUrl != "http://" &&
        username.isNotBlank() &&
        password.isNotBlank()

    val verifyingMessage = androidx.compose.ui.res.stringResource(
        com.izplay.v3.R.string.add_xtream_verifying,
    )
    val authFailedMessage = androidx.compose.ui.res.stringResource(
        com.izplay.v3.R.string.add_xtream_auth_failed,
    )
    val saveFailedMessage = androidx.compose.ui.res.stringResource(
        com.izplay.v3.R.string.add_xtream_save_failed,
    )
    fun save() {
        if (!isValid || isSaving) return
        focusManager.clearFocus()
        errorMessage = null
        isSaving = true
        progressMessage = verifyingMessage

        scope.launch {
            val current = editing
            val newPlaylist = Playlist.create(
                id = current?.id ?: UUID.randomUUID().toString(),
                name = name.trim().ifBlank { "IZ Play" },
                serverUrl = serverUrl.trim(),
                username = username.trim(),
                password = password.trim(),
                kind = PlaylistKind.XTREAM,
                filterAdultContent = filterAdultContent,
                createdAt = current?.createdAt ?: System.currentTimeMillis(),
            )

            // iOS `detailsChanged` check — if only the display name moved we
            // can skip the round-trip + sync. URL/username/password/filter
            // all affect what gets fetched so any of them flips the bit.
            val detailsChanged = current == null ||
                newPlaylist.serverUrl != current.serverUrl ||
                newPlaylist.username != current.username ||
                newPlaylist.password != current.password ||
                newPlaylist.filterAdultContent != current.filterAdultContent

            if (!detailsChanged) {
                try {
                    repository.update(newPlaylist)
                    isSaving = false
                    progressMessage = null
                    onSaved(newPlaylist.id)
                } catch (e: Throwable) {
                    errorMessage = e.message ?: saveFailedMessage
                    isSaving = false
                    progressMessage = null
                }
                return@launch
            }

            try {
                val client = XtreamApiClient(newPlaylist)
                val response = client.verify()
                if (response.userInfo?.auth != 1) {
                    errorMessage = authFailedMessage
                    isSaving = false
                    progressMessage = null
                    return@launch
                }

                // Persist the playlist BEFORE the sync — iOS does the same so
                // that if sync fails partway, the row is still listed and the
                // user can re-trigger from the dashboard's Re-sync action.
                if (current != null) {
                    repository.update(newPlaylist)
                } else {
                    repository.add(newPlaylist)
                }

                contentStore.syncFromNetworkReplacingLocal(newPlaylist) { msg ->
                    progressMessage = msg
                }

                isSaving = false
                progressMessage = null
                onSaved(newPlaylist.id)
            } catch (e: Throwable) {
                errorMessage = e.message ?: e.javaClass.simpleName
                isSaving = false
                progressMessage = null
            }
        }
    }

    // ===== Apresentação: login fiel ao SetupScreen do IZ Play V2 Android =====
    ModalSlideContainer {
        IzTheme {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(
                    // Pré-mesclado: RedDeep@34% sobre preto ≈ #1E0000. Cores
                    // opacas evitam o banding claro que o alpha em gradiente
                    // produz em GPUs de TV box fracas.
                    Brush.linearGradient(
                        listOf(Color.Black, Color(0xFF1E0000), IzColor.Background, Color.Black),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            val mobile = maxWidth < 600.dp
            IconButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = IzColor.TextSecondary)
            }

            if (mobile) {
                MobileLoginContent(
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    enabled = isValid && !isSaving,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    onLogin = ::save,
                )
            } else Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 72.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(com.izplay.v3.R.drawable.izplay_logo),
                        contentDescription = "IZ Play",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth(0.68f),
                    )
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "ENTRETENIMENTO",
                            color = IzColor.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 6.sp,
                        )
                        Text(
                            "SEM LIMITES",
                            color = IzColor.Primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 6.sp,
                        )
                    }
                }

                Box(
                    Modifier
                        .width(2.dp)
                        .height(520.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, IzColor.Primary, Color.Transparent),
                            ),
                        ),
                )

                Column(
                    Modifier
                        .width(480.dp)
                        .padding(start = 70.dp),
                ) {
                    if (editing != null) {
                        Text(
                            "EDITAR PLAYLIST XTREAM",
                            color = IzColor.TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 3.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                    if (!useFixedDns) {
                        IzLoginField("Servidor (http://...)", serverUrl, Icons.Filled.Storage) { serverUrl = it }
                        Spacer(Modifier.height(18.dp))
                    }
                    IzLoginField("Usuario", username, Icons.Filled.Person) { username = it }
                    Spacer(Modifier.height(18.dp))
                    IzLoginField(
                        "Senha",
                        password,
                        Icons.Filled.Lock,
                        isPassword = true,
                        passVisible = passwordVisible,
                        onTogglePass = { passwordVisible = !passwordVisible },
                    ) { password = it }
                    if (!useFixedDns) {
                        Spacer(Modifier.height(18.dp))
                        IzLoginField("Nome da lista", name, Icons.AutoMirrored.Filled.Label) { name = it }
                    }
                    Spacer(Modifier.height(22.dp))
                    IzEntrarButton(enabled = isValid && !isSaving, onClick = ::save)
                    // O filtro de conteúdo adulto saiu do login (pedido do
                    // responsável). Em EDIÇÃO a opção continua disponível.
                    if (editing != null) {
                        Spacer(Modifier.height(18.dp))
                        AdultContentToggle(checked = filterAdultContent, onCheckedChange = { filterAdultContent = it })
                    }
                }
            }
        }

        if (isSaving) {
            // Tela de espera da marca (logo + barra), igual ao V2 — no lugar
            // da caixinha genérica de progresso.
            com.izplay.v3.ui.design.components.IzLoadingScreen(
                message = progressMessage ?: verifyingMessage,
            )
        }

        errorMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text(androidx.compose.ui.res.stringResource(com.izplay.v3.R.string.add_xtream_connection_failed)) },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text(androidx.compose.ui.res.stringResource(com.izplay.v3.R.string.common_ok))
                    }
                },
            )
        }
        }
    }
}

@Composable
private fun MobileLoginContent(
    username: String,
    password: String,
    passwordVisible: Boolean,
    enabled: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(76.dp))
        Image(
            painter = painterResource(com.izplay.v3.R.drawable.izplay_logo),
            contentDescription = "IZ Play",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
        )
        Spacer(Modifier.height(62.dp))
        Text("Bem-vindo", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text("Entre para assistir!", color = IzColor.TextSecondary, fontSize = 22.sp)
        Spacer(Modifier.height(44.dp))
        MobileUnderlineField(
            value = username,
            placeholder = "Login",
            onValueChange = onUsernameChange,
        )
        Spacer(Modifier.height(24.dp))
        MobileUnderlineField(
            value = password,
            placeholder = "Senha",
            password = true,
            passwordVisible = passwordVisible,
            onTogglePassword = onTogglePassword,
            onValueChange = onPasswordChange,
        )
        Spacer(Modifier.height(34.dp))
        IzEntrarButton(enabled = enabled, onClick = onLogin)
        Spacer(Modifier.height(110.dp))
        Text(
            "IZ Play Mobile  •  ${com.izplay.v3.BuildConfig.VERSION_NAME}",
            color = IzColor.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun MobileUnderlineField(
    value: String,
    placeholder: String,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    onValueChange: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                cursorBrush = SolidColor(IzColor.Primary),
                visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, color = IzColor.TextSecondary, fontSize = 20.sp)
                    inner()
                },
                modifier = Modifier.weight(1f),
            )
            if (password) {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Mostrar senha",
                        tint = Color.White,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.75f)))
    }
}

/** Campo de login no estilo do SetupScreen do V2 (66dp, raio 12, fundo translúcido). */
@Composable
private fun IzLoginField(
    placeholder: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passVisible: Boolean = false,
    onTogglePass: (() -> Unit)? = null,
    onChange: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC181A1E))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = IzColor.TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(
                color = IzColor.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            cursorBrush = SolidColor(IzColor.Primary),
            visualTransformation = if (isPassword && !passVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = IzColor.TextSecondary, fontSize = 16.sp)
                inner()
            },
            modifier = Modifier.weight(1f),
        )
        if (isPassword && onTogglePass != null) {
            val eye = rememberIzInteractionSource()
            val eyeFocused by eye.collectIsFocusedAsState()
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (eyeFocused) IzColor.Primary else Color.Transparent)
                    .clickable(interactionSource = eye, indication = null) { onTogglePass() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (passVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Mostrar senha",
                    tint = if (eyeFocused) Color.White else IzColor.TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/** Botão ENTRAR do V2: 64dp, gradiente vermelho, foco = anel branco + zoom leve. */
@Composable
private fun IzEntrarButton(enabled: Boolean, onClick: () -> Unit) {
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .izFocusVisuals(interaction, shape = shape, focusedScale = 1.02f, ring = IzFocusRing.OnPrimary)
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.verticalGradient(listOf(Color(0xFFE8222C), Color(0xFFC8121B)))
                } else {
                    Brush.verticalGradient(listOf(IzColor.RedDark, IzColor.RedDark))
                },
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "ENTRAR",
            color = IzColor.TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.width(14.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = IzColor.TextPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AdultContentToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Filter adult content",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Hide channels and categories flagged as adult.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// `@Preview` is intentionally omitted: the save path requires
// `LocalPlaylistContentStore`, which can't be faked without an in-memory
// Room database. Verify the screen by running the app.
