package com.izplay.v3.networking

import com.izplay.v3.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

@Serializable
data class AppUpdate(
    val updateAvailable: Boolean = false,
    val mandatory: Boolean = false,
    val platform: String = "android-mobile",
    val channel: String = BuildConfig.UPDATE_CHANNEL,
    val versionCode: Int = 0,
    val versionName: String = "",
    val minimumVersionCode: Int = 1,
    val downloadUrl: String = "",
    val sha256: String = "",
    val fileSize: Long = 0,
    val releaseNotes: String = "",
)

object AppUpdateClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun check(): AppUpdate? = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = BuildConfig.UPDATE_API_URL.trimEnd('/') +
                "/android-mobile?channel=${BuildConfig.UPDATE_CHANNEL}" +
                "&versionCode=${BuildConfig.VERSION_CODE}"
            val response = client.newCall(Request.Builder().url(endpoint).get().build()).execute()
            response.use {
                if (!it.isSuccessful) return@runCatching null
                val body = it.body?.string().orEmpty()
                json.decodeFromString<AppUpdate>(body).takeIf { update ->
                    update.updateAvailable &&
                        update.platform == "android-mobile" &&
                        update.channel == BuildConfig.UPDATE_CHANNEL &&
                        update.versionCode > BuildConfig.VERSION_CODE &&
                        update.versionName.isNotBlank() &&
                        update.sha256.matches(Regex("[a-fA-F0-9]{64}")) &&
                        runCatching { absoluteDownloadUrl(update).startsWith("https://") }
                            .getOrDefault(false)
                }
            }
        }.getOrNull()
    }

    fun absoluteDownloadUrl(update: AppUpdate): String {
        if (update.downloadUrl.startsWith("https://")) return update.downloadUrl
        val api = URI(BuildConfig.UPDATE_API_URL)
        return URI(api.scheme, api.authority, update.downloadUrl, null, null).toString()
    }
}
