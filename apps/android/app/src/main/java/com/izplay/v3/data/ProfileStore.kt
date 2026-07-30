package com.izplay.v3.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class AppProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatar: Int,
    val genres: List<String>,
)

data class ProfileState(
    val playlistId: String? = null,
    val profiles: List<AppProfile> = emptyList(),
    val activeProfileId: String? = null,
    val pickerVisible: Boolean = false,
) {
    val activeProfile get() = profiles.firstOrNull { it.id == activeProfileId }
}

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("izplay_mobile_profiles", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()
    private val presented = mutableSetOf<String>()

    fun enterPlaylist(playlistId: String) {
        val profiles = loadProfiles(playlistId)
        val active = prefs.getString(activeKey(playlistId), null)
            ?.takeIf { id -> profiles.any { it.id == id } }
        _state.value = ProfileState(
            playlistId = playlistId,
            profiles = profiles,
            activeProfileId = active,
            pickerVisible = playlistId !in presented || profiles.isEmpty() || active == null,
        )
        presented += playlistId
    }

    fun openPicker(playlistId: String) {
        if (_state.value.playlistId != playlistId) enterPlaylist(playlistId)
        _state.value = _state.value.copy(pickerVisible = true)
    }

    fun closePicker() {
        if (_state.value.activeProfile != null) _state.value = _state.value.copy(pickerVisible = false)
    }

    fun create(playlistId: String, name: String, avatar: Int, genres: List<String>) {
        val current = if (_state.value.playlistId == playlistId) _state.value.profiles else loadProfiles(playlistId)
        if (current.size >= 6) return
        val profile = AppProfile(name = name.trim().take(24), avatar = avatar.coerceIn(0, 11), genres = genres.take(6))
        val updated = current + profile
        saveProfiles(playlistId, updated)
        saveActive(playlistId, profile.id)
        _state.value = ProfileState(playlistId, updated, profile.id, pickerVisible = false)
    }

    fun select(playlistId: String, profile: AppProfile) {
        saveActive(playlistId, profile.id)
        _state.value = ProfileState(playlistId, loadProfiles(playlistId), profile.id, pickerVisible = false)
    }

    private fun loadProfiles(playlistId: String): List<AppProfile> =
        runCatching {
            json.decodeFromString(
                ListSerializer(AppProfile.serializer()),
                prefs.getString(profilesKey(playlistId), "[]") ?: "[]",
            )
        }.getOrDefault(emptyList())

    private fun saveProfiles(playlistId: String, profiles: List<AppProfile>) {
        prefs.edit().putString(
            profilesKey(playlistId),
            json.encodeToString(ListSerializer(AppProfile.serializer()), profiles),
        ).apply()
    }

    private fun saveActive(playlistId: String, id: String) {
        prefs.edit().putString(activeKey(playlistId), id).apply()
    }

    private fun profilesKey(id: String) = "profiles_$id"
    private fun activeKey(id: String) = "active_$id"
}
