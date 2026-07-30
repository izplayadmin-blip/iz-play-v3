package com.izplay.v3.networking

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.izplay.v3.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data class Downloading(val downloaded: Long, val total: Long) : UpdateDownloadState
    data object Verifying : UpdateDownloadState
    data class Ready(val file: File) : UpdateDownloadState
    data class Failed(val message: String) : UpdateDownloadState
}

object AppUpdateInstaller {
    private const val MAX_APK_BYTES = 500L * 1024L * 1024L
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun downloadAndVerify(
        activity: Activity,
        update: AppUpdate,
        onProgress: (UpdateDownloadState) -> Unit,
    ): UpdateDownloadState = withContext(Dispatchers.IO) {
        runCatching {
            val url = AppUpdateClient.absoluteDownloadUrl(update)
            require(url.startsWith("https://")) { "A atualização precisa usar HTTPS." }
            require(update.sha256.matches(Regex("[a-fA-F0-9]{64}"))) {
                "O manifesto não possui um SHA-256 válido."
            }

            val directory = File(activity.cacheDir, "updates").apply { mkdirs() }
            directory.listFiles()?.forEach { if (it.isFile) it.delete() }
            val partial = File(directory, "izplay-${update.versionCode}.apk.part")
            val target = File(directory, "izplay-${update.versionCode}.apk")
            val request = Request.Builder()
                .url(url)
                .header("Cache-Control", "no-cache")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Falha no download: HTTP ${response.code}." }
                val body = checkNotNull(response.body) { "O servidor retornou um arquivo vazio." }
                val total = body.contentLength().takeIf { it > 0 } ?: update.fileSize
                require(total <= 0 || total <= MAX_APK_BYTES) { "O arquivo excede o limite permitido." }
                body.byteStream().use { input ->
                    partial.outputStream().buffered().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        var lastProgressAt = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            downloaded += read
                            require(downloaded <= MAX_APK_BYTES) { "O arquivo excede o limite permitido." }
                            output.write(buffer, 0, read)
                            val now = android.os.SystemClock.elapsedRealtime()
                            if (now - lastProgressAt >= 250L) {
                                withContext(Dispatchers.Main) {
                                    onProgress(UpdateDownloadState.Downloading(downloaded, total))
                                }
                                lastProgressAt = now
                            }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onProgress(UpdateDownloadState.Verifying)
            }
            check(sha256(partial).equals(update.sha256, ignoreCase = true)) {
                "A integridade da atualização não confere."
            }
            verifyArchive(activity, partial, update)
            check(partial.renameTo(target)) { "Não foi possível preparar o APK." }
            UpdateDownloadState.Ready(target)
        }.getOrElse {
            UpdateDownloadState.Failed(it.message ?: "Não foi possível preparar a atualização.")
        }
    }

    fun requestInstall(activity: Activity, apk: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            activity.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${activity.packageName}"),
                ),
            )
            return false
        }
        val uri = FileProvider.getUriForFile(
            activity,
            "${BuildConfig.APPLICATION_ID}.updates",
            apk,
        )
        activity.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        return true
    }

    @Suppress("DEPRECATION")
    private fun verifyArchive(activity: Activity, apk: File, update: AppUpdate) {
        val pm = activity.packageManager
        val archive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageArchiveInfo(apk.path, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            pm.getPackageArchiveInfo(apk.path, PackageManager.GET_SIGNATURES)
        } ?: error("O arquivo baixado não é um APK válido.")
        check(archive.packageName == BuildConfig.APPLICATION_ID) {
            "O APK pertence a outro aplicativo."
        }
        val archiveVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archive.longVersionCode
        } else {
            archive.versionCode.toLong()
        }
        check(archiveVersion == update.versionCode.toLong() && archiveVersion > BuildConfig.VERSION_CODE) {
            "A versão do APK não corresponde ao manifesto."
        }

        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(activity.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            pm.getPackageInfo(activity.packageName, PackageManager.GET_SIGNATURES)
        }
        val expected = signatures(current)
        val received = signatures(archive)
        check(expected.isNotEmpty() && expected == received) {
            "A assinatura do APK não pertence ao IZ Play."
        }
    }

    @Suppress("DEPRECATION")
    private fun signatures(info: android.content.pm.PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signing = info.signingInfo ?: return emptySet()
            if (signing.hasMultipleSigners()) signing.apkContentsSigners.toList()
            else signing.signingCertificateHistory.toList()
        } else {
            info.signatures?.toList().orEmpty()
        }
        return signatures.map { bytesSha256(it.toByteArray()) }.toSet()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun bytesSha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
