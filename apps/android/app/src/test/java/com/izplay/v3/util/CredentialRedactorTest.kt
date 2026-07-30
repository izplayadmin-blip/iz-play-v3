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
    fun redactsRootXtreamHlsCredentialsIncludingNumericValues() {
        val redacted = CredentialRedactor.redact(
            "load http://provider.test/123456/789012/345678.m3u8",
        )
        assertEquals(
            "load http://provider.test/<redacted>/<redacted>/345678.m3u8",
            redacted,
        )
    }

    @Test
    fun redactsCredentialsMirroredBySwarmCloudLoopback() {
        val redacted = CredentialRedactor.redact(
            "load http://127.0.0.1:12345/123456/789012/345678.m3u8",
        )
        assertEquals(
            "load http://127.0.0.1:12345/<redacted>/<redacted>/345678.m3u8",
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
