package com.izplay.v3.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CredentialRedactorTest {
    @Test
    fun redactsXtreamQueryCredentials() {
        val redacted = CredentialRedactor.redact(
            "https://provider.test/player_api.php?username=alice&password=s3cr%26t&action=x",
        )
        assertEquals(
            "https://provider.test/player_api.php?username=<redacted>&password=<redacted>&action=x",
            redacted,
        )
    }

    @Test
    fun redactsXtreamPlaybackPathCredentials() {
        val redacted = CredentialRedactor.redact(
            "load https://provider.test/live/alice/s3cr3t/123.ts",
        )
        assertEquals(
            "load https://provider.test/live/<redacted>/<redacted>/123.ts",
            redacted,
        )
    }

    @Test
    fun redactsCredentialsInJsonBodies() {
        val redacted = CredentialRedactor.redact(
            "Body: {\"username\": \"alice\", \"password\":\"s3cr3t\", \"status\":\"ok\"}",
        )
        assertFalse(redacted.contains("alice"))
        assertFalse(redacted.contains("s3cr3t"))
    }
}
