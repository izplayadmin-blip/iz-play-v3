package com.izplay.v3.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.data.AppProfile
import com.izplay.v3.data.ProfileState

private val genres = listOf("Ação", "Comédia", "Drama", "Romance", "Suspense", "Terror", "Ficção", "Animação", "Documentários", "Novelas", "Esportes", "Nacional")
private val avatarColors = listOf(0xFF9B1725,0xFF37A4C7,0xFF9A66B2,0xFF96705F,0xFFD99A39,0xFF53B9B5,0xFFE99B84,0xFF2EA7D7,0xFFE06E49,0xFF50639B,0xFF8F54A8,0xFF678A7B).map(::Color)

@Composable
fun MobileProfileScreen(
    state: ProfileState,
    onSelect: (AppProfile) -> Unit,
    onCreate: (String, Int, List<String>) -> Unit,
    onClose: () -> Unit,
) {
    var creating by remember(state.profiles.size) { mutableStateOf(state.profiles.isEmpty()) }
    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        if (creating) CreateProfile(
            canCancel = state.profiles.isNotEmpty(),
            onCancel = { creating = false },
            onCreate = onCreate,
        ) else Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Quem vai assistir?", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Escolha um perfil para continuar", color = Color(0xFF999999), fontSize = 14.sp)
            Spacer(Modifier.height(34.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(state.profiles, key = { it.id }) { profile ->
                    Column(
                        Modifier.clickable { onSelect(profile) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProfileAvatar(profile.avatar, profile.id == state.activeProfileId, 132)
                        Spacer(Modifier.height(8.dp))
                        Text(profile.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (state.profiles.size < 6) item {
                    Column(
                        Modifier.clickable { creating = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(132.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFF171717)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Add, null, tint = Color(0xFFE32636), modifier = Modifier.size(48.dp)) }
                        Spacer(Modifier.height(8.dp))
                        Text("Adicionar perfil", color = Color.White)
                    }
                }
            }
            if (state.activeProfile != null) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClose) { Text("Voltar", color = Color.White) }
            }
        }
    }
}

@Composable
private fun CreateProfile(canCancel: Boolean, onCancel: () -> Unit, onCreate: (String, Int, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Crie seu perfil", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Favoritos, histórico e sugestões para você.", color = Color(0xFF999999), textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        ProfileAvatar(avatar, true, 112)
        OutlinedTextField(
            value = name, onValueChange = { name = it.take(24) },
            label = { Text("Como devemos chamar você?") },
            singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        )
        Text("Escolha seu avatar", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.height(112.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { items((0..11).toList()) { index ->
            Box(Modifier.clickable { avatar = index }) { ProfileAvatar(index, avatar == index, 50) }
        } }
        Text("O que você gosta de assistir?", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 18.dp))
        Text("Escolha pelo menos 3 opções", color = Color(0xFF888888), modifier = Modifier.fillMaxWidth())
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { items(genres) { genre ->
            val active = genre in selected
            Text(
                genre, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.clip(RoundedCornerShape(18.dp))
                    .background(if (active) Color(0xFFE30613) else Color(0xFF191919))
                    .clickable { selected = if (active) selected - genre else if (selected.size < 6) selected + genre else selected }
                    .padding(vertical = 10.dp),
            )
        } }
        Button(
            onClick = { onCreate(name, avatar, selected.toList()) },
            enabled = name.isNotBlank() && selected.size >= 3,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE30613)),
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) { Text("Salvar perfil", fontWeight = FontWeight.Bold) }
        if (canCancel) TextButton(onClick = onCancel) { Text("Cancelar", color = Color.White) }
    }
}

@Composable
fun ProfileAvatar(index: Int, selected: Boolean, size: Int) {
    val i = index.coerceIn(0, 11)
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size / 6).dp))
            .background(avatarColors[i])
            .then(if (selected) Modifier.border(3.dp, Color.White, RoundedCornerShape((size / 6).dp)) else Modifier),
    ) {
        Canvas(Modifier.fillMaxSize().padding((size * .09f).dp)) {
            val w = this.size.width; val h = this.size.height
            val skin = listOf(0xFFF1B38E,0xFFD9976D,0xFFFFC294,0xFFB97655)[i % 4].let(::Color)
            val hair = listOf(0xFF20252B,0xFF713A20,0xFFF0A000,0xFF15313D)[i % 4].let(::Color)
            val shirt = listOf(0xFF17364A,0xFFB33575,0xFF743A94,0xFF174B67)[i % 4].let(::Color)
            drawCircle(hair, w*.25f, androidx.compose.ui.geometry.Offset(w*.5f,h*.36f))
            drawOval(skin, androidx.compose.ui.geometry.Offset(w*.31f,h*.22f), androidx.compose.ui.geometry.Size(w*.38f,h*.43f))
            val torso=Path().apply { moveTo(w*.25f,h); lineTo(w*.35f,h*.68f); lineTo(w*.65f,h*.68f); lineTo(w*.75f,h); close() }
            drawPath(torso, shirt)
            drawCircle(Color(0xFF202124),w*.018f,androidx.compose.ui.geometry.Offset(w*.43f,h*.42f))
            drawCircle(Color(0xFF202124),w*.018f,androidx.compose.ui.geometry.Offset(w*.57f,h*.42f))
        }
    }
}
