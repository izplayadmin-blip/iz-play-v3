package com.izplay.v3.util

/** Removes Xtream credentials from text before it is written to logs. */
internal object CredentialRedactor {
    private const val REDACTED = "<redacted>"

    private val queryCredential = Regex("(?i)([?&](?:username|password)=)[^&#\\s]*")
    private val jsonCredential = Regex(
        "(?i)(\\\"(?:username|password)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
    )
    private val xtreamPath = Regex("(?i)(/(?:live|movie|series)/)[^/\\s]+/[^/\\s]+(/)")
    // Some Xtream servers expose live HLS directly as /user/password/id.m3u8,
    // without the conventional /live prefix. SwarmCloud loopback URLs mirror
    // that path, so both the origin and localhost form must be sanitized.
    private val rootXtreamPath = Regex(
        "(?i)(https?://[^/\\s]+/)(?!(?:live|movie|series)/)[^/\\s]+/[^/\\s]+(/[^?\\s]+)",
    )

    fun redact(input: String): String = input
        .replace(queryCredential) { match -> "${match.groupValues[1]}$REDACTED" }
        .replace(jsonCredential) { match ->
            "${match.groupValues[1]}$REDACTED${match.groupValues[2]}"
        }
        .replace(xtreamPath) { match ->
            "${match.groupValues[1]}$REDACTED/$REDACTED${match.groupValues[2]}"
        }
        .replace(rootXtreamPath) { match ->
            "${match.groupValues[1]}$REDACTED/$REDACTED${match.groupValues[2]}"
        }
}
