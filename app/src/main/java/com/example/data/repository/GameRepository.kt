package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.data.local.GameDao
import com.example.data.local.GameEntity
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isGameCategory: Boolean
)

class GameRepository(
    private val context: Context,
    private val gameDao: GameDao,
    private val preferencesRepository: UserPreferencesRepository? = null
) {
    private val packageManager: PackageManager = context.packageManager

    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val favoriteGames: Flow<List<GameEntity>> = gameDao.getFavoriteGames()

    suspend fun addGame(packageName: String, appName: String, isFavorite: Boolean = false) {
        gameDao.insertGame(
            GameEntity(
                packageName = packageName,
                appName = appName,
                isFavorite = isFavorite,
                isSystemDetected = false
            )
        )
        if (isFavorite) {
            preferencesRepository?.toggleFavoritePackage(packageName, true)
        }
    }

    suspend fun toggleFavorite(packageName: String, isFavorite: Boolean) {
        gameDao.updateFavorite(packageName, isFavorite)
        preferencesRepository?.toggleFavoritePackage(packageName, isFavorite)
    }

    suspend fun removeGame(packageName: String) {
        gameDao.deleteGame(packageName)
        preferencesRepository?.toggleFavoritePackage(packageName, false)
    }

    suspend fun clearAllGames() {
        gameDao.clearAll()
    }

    suspend fun scanAndAutoAddGames(): Int = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val detectedGames = mutableListOf<GameEntity>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue

            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(pkg, 0)
                }
            } catch (_: Exception) {
                null
            } ?: continue

            val label = packageManager.getApplicationLabel(appInfo).toString()
            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val lowerName = label.lowercase()
            val lowerPkg = pkg.lowercase()
            val looksLikeGame = isGame ||
                    lowerName.contains("game") ||
                    lowerPkg.contains("game") ||
                    lowerPkg.contains("unity") ||
                    lowerPkg.contains("unreal") ||
                    lowerPkg.contains("mihoyo") ||
                    lowerPkg.contains("roblox") ||
                    lowerPkg.contains("pubg") ||
                    lowerPkg.contains("freefire") ||
                    lowerPkg.contains("supercell")

            if (looksLikeGame) {
                detectedGames.add(
                    GameEntity(
                        packageName = pkg,
                        appName = label,
                        isFavorite = false,
                        isSystemDetected = true
                    )
                )
            }
        }

        if (detectedGames.isNotEmpty()) {
            gameDao.insertGames(detectedGames)
        }

        return@withContext detectedGames.size
    }

    suspend fun getInstalledLaunchableApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) return@mapNotNull null

            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(pkg, 0)
                }
            } catch (_: Exception) {
                null
            } ?: return@mapNotNull null

            val label = packageManager.getApplicationLabel(appInfo).toString()
            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            InstalledAppInfo(
                packageName = pkg,
                appName = label,
                isGameCategory = isGame
            )
        }.sortedBy { it.appName.lowercase() }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun launchGame(packageName: String): Result<Unit> {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                preferencesRepository?.addRecentlyLaunched(packageName)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot launch application: no active launcher component found for package $packageName."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
