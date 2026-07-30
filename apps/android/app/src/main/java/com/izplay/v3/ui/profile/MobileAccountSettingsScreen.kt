package com.izplay.v3.ui.profile

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.R
import com.izplay.v3.data.ProfileState
import com.izplay.v3.model.Playlist
import com.izplay.v3.ui.LocalPlayerPreferences
import com.izplay.v3.ui.LocalPlaylistContentStore
import com.izplay.v3.ui.LocalPlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private val IzRed = Color(0xFFE30613)
private val IzGreen = Color(0xFF25C65A)
private val IzCard = Color(0xFF111111)
private val IzBorder = Color(0xFF343434)
private val IzMuted = Color(0xFFA7A7A7)

@Composable
fun MobileAccountScreen(
    playlist: Playlist,
    profileState: ProfileState,
    appVersion: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onManageProfiles: () -> Unit,
    onChangeLogin: () -> Unit,
) {
    val active = profileState.activeProfile
    IzPage(title = "Conta e perfis", onBack = onBack) {
        Text("Gerencie sua conta e os perfis deste dispositivo", color = IzMuted)
        IzPanel {
            Text(
                playlist.username.ifBlank { playlist.name },
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Login principal", color = IzMuted)
            HorizontalDivider(color = IzBorder, modifier = Modifier.padding(top = 14.dp))
            AccountRow("Perfil", active?.name ?: "Sem perfil", Icons.Default.Person)
            AccountRow("Status", "Ativa", Icons.Default.VerifiedUser, IzGreen)
            AccountRow("Dispositivo", Build.MODEL ?: "Android", Icons.Default.Devices)
            AccountRow("Versão", appVersion, Icons.Default.Info)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IzAction("Trocar perfil", Icons.Default.SwitchAccount, false, Modifier.weight(1f), onManageProfiles)
            IzAction("Configurações", Icons.Default.Settings, false, Modifier.weight(1f), onOpenSettings)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IzAction("Editar perfis", Icons.Default.Edit, true, Modifier.weight(1f), onManageProfiles)
            IzAction("Trocar login", Icons.Default.Login, false, Modifier.weight(1f), onChangeLogin)
        }
        IzPanel {
            Text("Perfis deste dispositivo", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("Cada perfil mantém suas preferências e sugestões.", color = IzMuted, fontSize = 13.sp)
            profileState.profiles.forEach { profile ->
                Row(
                    Modifier.fillMaxWidth().clickable { onManageProfiles() }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(profile.avatar, profile.id == profileState.activeProfileId, 58)
                    Text(profile.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp).weight(1f))
                    if (profile.id == profileState.activeProfileId) Text("Ativo", color = IzGreen)
                }
            }
            if (profileState.profiles.size < 6) {
                OutlinedButton(
                    onClick = onManageProfiles,
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IzRed)),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Default.Add, null, tint = IzRed)
                    Text("Adicionar perfil", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MobileSettingsScreen(
    playlist: Playlist,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("izplay_mobile_network", Context.MODE_PRIVATE) }
    val playerPrefs = LocalPlayerPreferences.current
    val contentStore = LocalPlaylistContentStore.current
    val playlistRepository = LocalPlaylistRepository.current
    val pip by playerPrefs.pipEnabled.collectAsState()
    val background by playerPrefs.continuePlayingInBackground.collectAsState()
    var dns by remember { mutableStateOf(prefs.getString("dns", "Automático") ?: "Automático") }
    var proxy by remember { mutableStateOf(prefs.getBoolean("proxy", true)) }
    var parental by remember { mutableStateOf(playlist.filterAdultContent) }
    var syncing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ConnectionResult?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun runTest() {
        testing = true
        testResult = null
        scope.launch {
            testResult = testConnection(context)
            testing = false
        }
    }

    if (testing || testResult != null) {
        ConnectionTestScreen(testResult, testing, onBack = {
            testing = false
            testResult = null
        }, onRetest = {
            runTest()
        })
        return
    }

    IzPage(title = "Configurações", onBack = onBack) {
        IzPanel {
            SectionTitle("DNS", "Preferência de resolução usada pelo aplicativo.")
            DropdownSetting("DNS do aplicativo", dns, listOf("Automático", "Cloudflare (1.1.1.1)", "Google (8.8.8.8)", "Quad9 (9.9.9.9)")) {
                dns = it
                prefs.edit().putString("dns", it).apply()
            }
            IzAction("Testar conexão", Icons.Default.MonitorHeart, true, Modifier.fillMaxWidth()) {
                runTest()
            }
        }
        IzPanel {
            SectionTitle("Proxy e rota protegida", "Melhora a estabilidade e a compatibilidade.")
            SwitchSetting("Ativar proxy", "Usar quando a rota local estiver disponível.", proxy) {
                proxy = it; prefs.edit().putBoolean("proxy", it).apply()
            }
        }
        IzPanel {
            SectionTitle("Reprodução / Player", "Ajuste a experiência de reprodução.")
            SwitchSetting("Picture-in-Picture", "Continue assistindo fora do aplicativo.", pip, playerPrefs::setPipEnabled)
            SwitchSetting("Reprodução em segundo plano", "Mantém o áudio ao sair do player.", background, playerPrefs::setContinuePlayingInBackground)
        }
        IzPanel {
            SectionTitle("Conteúdo e cache", "Recarrega canais, filmes e séries.")
            IzAction(if (syncing) "Atualizando..." else "Atualizar conteúdo", Icons.Default.Refresh, true, Modifier.fillMaxWidth()) {
                if (!syncing) scope.launch {
                    syncing = true
                    runCatching {
                        contentStore.syncFromNetworkReplacingLocal(playlist) {}
                        contentStore.reloadFromDatabaseIfActive(playlist.id)
                    }
                    syncing = false
                }
            }
        }
        IzPanel {
            SwitchSetting("Controle parental", "Oculta categorias de conteúdo adulto.", parental) {
                parental = it
                scope.launch {
                    val updated = playlist.copy(filterAdultContent = it)
                    playlistRepository.update(updated)
                    runCatching {
                        contentStore.syncFromNetworkReplacingLocal(updated) {}
                        contentStore.reloadFromDatabaseIfActive(updated.id)
                    }
                }
            }
        }
        IzPanel {
            SectionTitle("Teste de conexão", "Verifique a rota usada por este dispositivo.")
            IzAction("Iniciar teste", Icons.Default.Speed, true, Modifier.fillMaxWidth()) {
                runTest()
            }
        }
    }
}

private data class ConnectionResult(val ok: Boolean, val latency: Long?, val network: String, val testedAt: String)

@Composable
private fun ConnectionTestScreen(result: ConnectionResult?, testing: Boolean, onBack: () -> Unit, onRetest: () -> Unit) {
    IzPage(title = "Teste de conexão", onBack = onBack) {
        if (testing || result == null) {
            IzPanel {
                CircularProgressIndicator(color = IzRed, modifier = Modifier.size(58.dp))
                Text("Testando conexão…", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Medindo uma nova rota. Aguarde alguns segundos.", color = IzMuted)
            }
            return@IzPage
        }
        IzPanel {
            Icon(
                if (result.ok) Icons.Default.CheckCircle else Icons.Default.Error,
                null,
                tint = if (result.ok) IzGreen else IzRed,
                modifier = Modifier.size(64.dp),
            )
            Text(if (result.ok) "Conexão estável" else "Conexão indisponível", color = if (result.ok) IzGreen else IzRed, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(if (result.ok) "O aplicativo conseguiu alcançar a internet." else "Verifique sua rede e tente novamente.", color = IzMuted)
        }
        IzPanel {
            SectionTitle("Resumo", null)
            AccountRow("Latência", result.latency?.let { "$it ms" } ?: "—", Icons.Default.Speed)
            AccountRow("Tipo de rede", result.network, Icons.Default.Wifi)
            AccountRow("DNS", "OK", Icons.Default.Dns, if (result.ok) IzGreen else IzMuted)
            AccountRow("Streaming", if (result.ok) "Compatível" else "Indisponível", Icons.Default.PlayCircle, if (result.ok) IzGreen else IzRed)
        }
        Text("Executado em ${result.testedAt}", color = IzMuted, fontSize = 12.sp)
        Text("O teste não envia login, senha ou URL de reprodução.", color = IzMuted, fontSize = 12.sp)
        IzAction("Testar novamente", Icons.Default.Refresh, true, Modifier.fillMaxWidth(), onRetest)
    }
}

private suspend fun testConnection(context: Context): ConnectionResult = withContext(Dispatchers.IO) {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork)
    val network = when {
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi‑Fi"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Dados móveis"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
        else -> "Desconhecida"
    }
    val samples = listOf(
        "https://www.google.com/generate_204",
        "https://cloudflare.com/cdn-cgi/trace",
        "https://connectivitycheck.gstatic.com/generate_204",
    ).mapNotNull { endpoint ->
        runCatching {
            val start = SystemClock.elapsedRealtime()
            (URL("$endpoint?iz=${System.nanoTime()}").openConnection() as HttpURLConnection).run {
                useCaches = false
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("Cache-Control", "no-cache")
                connect()
                val success = responseCode in 200..399
                disconnect()
                if (success) SystemClock.elapsedRealtime() - start else null
            }
        }.getOrNull()
    }
    val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    ConnectionResult(samples.isNotEmpty(), samples.takeIf { it.isNotEmpty() }?.average()?.toLong(), network, now)
}

@Composable
private fun IzPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White) }
            androidx.compose.foundation.Image(painterResource(R.drawable.izplay_logo), null, Modifier.width(130.dp).height(48.dp))
            Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
        }
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun IzPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(IzCard, RoundedCornerShape(18.dp)).border(1.dp, IzBorder, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String?) {
    Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    subtitle?.let { Text(it, color = IzMuted, fontSize = 13.sp) }
}

@Composable
private fun AccountRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, valueColor: Color = IzMuted) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = IzRed, modifier = Modifier.size(22.dp))
        Text(label, color = Color.White, modifier = Modifier.padding(start = 12.dp).weight(1f))
        Text(value, color = valueColor)
    }
}

@Composable
private fun SwitchSetting(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = IzMuted, fontSize = 12.sp)
        }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = IzRed))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSetting(label: String, value: String, values: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(
            value, {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            values.forEach { item -> DropdownMenuItem({ Text(item) }, { onChange(item); expanded = false }) }
        }
    }
}

@Composable
private fun IzAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick,
        modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (primary) IzRed else Color(0xFF1A1A1A)),
        border = if (primary) null else ButtonDefaults.outlinedButtonBorder,
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(icon, null)
        Text(text, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
    }
}
