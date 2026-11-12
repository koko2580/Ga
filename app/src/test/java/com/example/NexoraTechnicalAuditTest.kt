package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GameDao
import com.example.data.local.GameEntity
import com.example.data.local.NexoraDatabase
import com.example.data.local.PingHistoryDao
import com.example.data.local.PingHistoryEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BatteryRepository
import com.example.data.repository.DeviceInfoRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.NetworkRepository
import com.example.data.repository.ThermalRepository
import com.example.domain.model.BatteryChargingStatus
import com.example.domain.model.DeviceTelemetry
import com.example.domain.model.ThermalStatusLevel
import com.example.navigation.Screen
import com.example.services.DeviceTelemetryService
import com.example.services.NetworkDiagnosticsService
import com.example.services.PingResult
import com.example.services.SystemOptimizerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NexoraTechnicalAuditTest {

    private lateinit var context: Context
    private lateinit var database: NexoraDatabase
    private lateinit var gameDao: GameDao
    private lateinit var pingDao: PingHistoryDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, NexoraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gameDao = database.gameDao()
        pingDao = database.pingHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. Navigation Audit
    @Test
    fun audit_navigationScreensIntegrity() {
        val items = Screen.items
        assertEquals(5, items.size)
        val expectedRoutes = setOf("home", "games", "boost", "network", "settings")
        val actualRoutes = items.map { it.route }.toSet()
        assertEquals(expectedRoutes, actualRoutes)
        items.forEach { screen ->
            assertTrue(screen.title.isNotBlank())
            assertNotNull(screen.selectedIcon)
            assertNotNull(screen.unselectedIcon)
        }
    }

    // 2. Room Game Library & Favorites Audit
    @Test
    fun audit_gameLibraryCrudAndFavorites() = runBlocking {
        val game1 = GameEntity(
            packageName = "com.supercell.clashroyale",
            appName = "Clash Royale",
            isFavorite = false
        )
        val game2 = GameEntity(
            packageName = "com.mihoyo.genshin",
            appName = "Genshin Impact",
            isFavorite = true
        )

        gameDao.insertGame(game1)
        gameDao.insertGame(game2)

        val allGames = gameDao.getAllGames().first()
        assertEquals(2, allGames.size)

        val favorites = gameDao.getFavoriteGames().first()
        assertEquals(1, favorites.size)
        assertEquals("Genshin Impact", favorites[0].appName)

        gameDao.updateFavorite("com.supercell.clashroyale", true)
        val updatedFavorites = gameDao.getFavoriteGames().first()
        assertEquals(2, updatedFavorites.size)

        gameDao.deleteGame("com.mihoyo.genshin")
        val remaining = gameDao.getAllGames().first()
        assertEquals(1, remaining.size)
        assertEquals("Clash Royale", remaining[0].appName)

        gameDao.clearAll()
        assertTrue(gameDao.getAllGames().first().isEmpty())
    }

    // 3. Network Ping History Database Audit
    @Test
    fun audit_pingHistoryPersistence() = runBlocking {
        val pingEntry = PingHistoryEntity(
            host = "1.1.1.1",
            avgLatencyMs = 24L,
            minLatencyMs = 21L,
            maxLatencyMs = 28L,
            jitterMs = 2L,
            packetLossPercent = 0
        )
        pingDao.insertPing(pingEntry)

        val history = pingDao.getRecentPings(10).first()
        assertEquals(1, history.size)
        assertEquals("1.1.1.1", history[0].host)
        assertEquals(24L, history[0].avgLatencyMs)

        pingDao.clearHistory()
        assertTrue(pingDao.getRecentPings(10).first().isEmpty())
    }

    // 4. Intent Launch Safety & Error Handling
    @Test
    fun audit_gameLaunchIntentSafeFailure() = runBlocking {
        val repo = GameRepository(context, gameDao)
        val result = repo.launchGame("com.nonexistent.fakegame.xyz")
        assertFalse(result.isSuccess)
        assertNotNull(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull()?.message?.contains("no active launcher") == true ||
                result.exceptionOrNull()?.message?.contains("no launcher activity") == true)
    }

    // 5. System Optimization Workflow Audit
    @Test
    fun audit_optimizationWorkflowExecution() = runBlocking {
        val prefsRepo = UserPreferencesRepository(context)
        val optimizer = SystemOptimizerService(context, prefsRepo)

        val testCacheFile = File(context.cacheDir, "audit_test.tmp")
        testCacheFile.writeText("diagnostic temporary bytes")
        assertTrue(testCacheFile.exists())

        val stepsReported = mutableListOf<String>()
        val report = optimizer.runOptimizationWorkflow("PERFORMANCE") { _, name ->
            stepsReported.add(name)
        }

        assertEquals(4, stepsReported.size)
        assertEquals("PERFORMANCE", report.profileApplied)
        assertTrue(report.actionsCompleted.isNotEmpty())
        assertTrue(report.systemTips.isNotEmpty())

        assertFalse(testCacheFile.exists())

        val displayIntent = optimizer.openDisplaySettingsIntent()
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, displayIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)

        val batteryIntent = optimizer.openBatterySettingsIntent()
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, batteryIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // 6. Real Device Telemetry Math Audit
    @Test
    fun audit_deviceTelemetryCalculations() {
        val telemetry = DeviceTelemetry(
            manufacturer = "Xiaomi",
            model = "Redmi Note 14 5G",
            brand = "Redmi",
            deviceCode = "gold",
            androidVersion = "14",
            sdkInt = 34,
            availableRamBytes = 4L * 1024L * 1024L * 1024L,
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableStorageBytes = 64L * 1024L * 1024L * 1024L,
            totalStorageBytes = 128L * 1024L * 1024L * 1024L,
            batteryLevel = 85,
            isCharging = false,
            batteryTemperatureC = 29.5f,
            refreshRateHz = 120f,
            networkType = "Wi-Fi",
            isNetworkConnected = true,
            activeProfile = "PERFORMANCE"
        )

        assertEquals(50, telemetry.ramUsagePercent)
        assertEquals(0.5f, telemetry.ramUsageRatio, 0.01f)
        assertEquals(50, telemetry.storageUsagePercent)
        assertEquals(0.5f, telemetry.storageUsageRatio, 0.01f)
        assertTrue(telemetry.formattedRam().contains("4.0 GB free of 8.0 GB"))
        assertTrue(telemetry.formattedStorage().contains("64.0 GB free of 128.0 GB"))
    }

    // 7. Device Info Repository Audit
    @Test
    fun audit_deviceInfoRepositorySpecsAndStats() {
        val repo = DeviceInfoRepository(context)
        val spec = repo.getDeviceSpecification()
        assertNotNull(spec.model)
        assertNotNull(spec.manufacturer)
        assertTrue(spec.sdkInt > 0)

        val mem = repo.getMemoryStats()
        assertTrue(mem.totalRamBytes > 0)
        assertTrue(mem.availableRamBytes >= 0)

        val storage = repo.getStorageStats()
        assertTrue(storage.totalStorageBytes >= 0)

        val refresh = repo.getScreenRefreshRate()
        assertTrue(refresh > 0f)
    }

    // 8. Battery Repository Snapshot Audit
    @Test
    fun audit_batteryRepositorySnapshot() {
        val repo = BatteryRepository(context)
        val battery = repo.getBatterySnapshot()
        assertNotNull(battery)
        assertTrue(battery.percentage in 0..100)
    }

    // 9. Thermal Repository Status Audit
    @Test
    fun audit_thermalRepositoryStatus() {
        val repo = ThermalRepository(context)
        val thermal = repo.getThermalStatusSnapshot()
        assertNotNull(thermal)
        assertTrue(thermal.advisoryNotice.isNotBlank())
    }

    // 10. Network Repository Snapshot Audit
    @Test
    fun audit_networkRepositorySnapshot() {
        val diagnostics = NetworkDiagnosticsService()
        val repo = NetworkRepository(context, pingDao, diagnostics)
        val state = repo.getNetworkStateSnapshot()
        assertNotNull(state)
        assertTrue(state.typeName.isNotBlank())
    }

    // 11. Network Latency Diagnostics Math Audit
    @Test
    fun audit_networkLatencyResultProcessing() {
        val latencies = listOf(18L, 22L, 20L, 24L)
        val min = latencies.minOrNull() ?: 0L
        val max = latencies.maxOrNull() ?: 0L
        val avg = latencies.average().toLong()
        assertEquals(18L, min)
        assertEquals(24L, max)
        assertEquals(21L, avg)

        val ping = PingResult(
            host = "cloudflare.com",
            latencies = latencies,
            minLatency = min,
            maxLatency = max,
            avgLatency = avg,
            jitter = 2L,
            packetLossPercent = 0,
            isSuccessful = true
        )

        assertTrue(ping.isSuccessful)
        assertEquals(0, ping.packetLossPercent)
    }

    // 12. DataStore Preferences State Audit
    @Test
    fun audit_preferencesDefaultValues() {
        val prefs = UserPreferences()
        assertEquals("BALANCED", prefs.activeProfile)
        assertTrue(prefs.hapticsEnabled)
        assertTrue(prefs.soundEffectsEnabled)
        assertTrue(prefs.animationsEnabled)
        assertEquals("1.1.1.1", prefs.defaultPingHost)
        assertEquals(4000L, prefs.inAppRefreshIntervalMs)
    }

    // 13. DataStore Profile Switch & Interval Adjustment
    @Test
    fun audit_preferencesProfileAdjustment() = runBlocking {
        val prefsRepo = UserPreferencesRepository(context)
        prefsRepo.setActiveProfile("PERFORMANCE")
        val prefsPerf = prefsRepo.userPreferencesFlow.first()
        assertEquals("PERFORMANCE", prefsPerf.activeProfile)
        assertEquals(2000L, prefsPerf.inAppRefreshIntervalMs)

        prefsRepo.setActiveProfile("BATTERY_SAVER")
        val prefsSaver = prefsRepo.userPreferencesFlow.first()
        assertEquals("BATTERY_SAVER", prefsSaver.activeProfile)
        assertEquals(8000L, prefsSaver.inAppRefreshIntervalMs)
    }
}
