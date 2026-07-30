package com.izplay.v3.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.izplay.v3.R
import com.izplay.v3.ui.LocalProfileStore
import com.izplay.v3.ui.profile.ProfileAvatar

/**
 * Cabeçalho único das cinco abas mobile. A caixa da marca é fixa para que
 * o HorizontalPager não provoque salto de posição ou escala entre páginas.
 */
@Composable
fun MobileIzTopBar(
    onSearch: () -> Unit,
    onProfile: () -> Unit = {},
) {
    val profileState by LocalProfileStore.current.state.collectAsState()
    val activeProfile = profileState.activeProfile
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.izplay_logo),
            contentDescription = "IZ Play",
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterStart,
            modifier = Modifier
                .width(154.dp)
                .height(58.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearch, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Search, "Buscar", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onProfile),
            contentAlignment = Alignment.Center,
        ) {
            if (activeProfile != null) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    ProfileAvatar(activeProfile.avatar, selected = false, size = 40)
                }
            } else {
                Icon(Icons.Default.AccountCircle, "Perfil", tint = Color(0xFFE32636), modifier = Modifier.size(38.dp))
            }
        }
    }
}
