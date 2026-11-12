package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.DeviceTelemetry
import com.example.services.PingResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NEXORA X", appName)
    }

    @Test
    fun `formatBytes converts sizes accurately`() {
        assertEquals("0 B", DeviceTelemetry.formatBytes(0L))
        assertEquals("500 MB", DeviceTelemetry.formatBytes(500L * 1024L * 1024L))
        val gbFormatted = DeviceTelemetry.formatBytes(4L * 1024L * 1024L * 1024L)
        assertTrue(gbFormatted.contains("4.0 GB"))
    }

    @Test
    fun `ping result stores metrics correctly`() {
        val ping = PingResult(
            host = "1.1.1.1",
            latencies = listOf(22L, 26L, 24L, 28L),
            minLatency = 22L,
            maxLatency = 28L,
            avgLatency = 25L,
            jitter = 3L,
            packetLossPercent = 0,
            isSuccessful = true
        )
        assertEquals(25L, ping.avgLatency)
        assertEquals(0, ping.packetLossPercent)
        assertTrue(ping.isSuccessful)
    }

    @Test
    fun `navigation screen items are non-null and valid`() {
        val items = com.example.navigation.Screen.items
        assertEquals(5, items.size)
        items.forEach { screen ->
            org.junit.Assert.assertNotNull(screen)
            org.junit.Assert.assertNotNull(screen.route)
            org.junit.Assert.assertNotNull(screen.title)
            org.junit.Assert.assertNotNull(screen.selectedIcon)
            org.junit.Assert.assertNotNull(screen.unselectedIcon)
        }
    }
}
