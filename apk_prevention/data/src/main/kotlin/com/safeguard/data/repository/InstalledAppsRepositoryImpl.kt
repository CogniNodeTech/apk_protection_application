package com.safeguard.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.safeguard.core.domain.model.UnknownSourceApp
import com.safeguard.core.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledAppsRepository {

    override suspend fun getAppsFromUnknownSources(): List<UnknownSourceApp> {
        val pm = context.packageManager
        val myPackage = context.packageName

        return try {
            val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            installedPackages
                .asSequence()
                .filter { it.packageName != myPackage }
                .filter { (it.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0 } // Filter out system apps
                .mapNotNull { pkgInfo ->
                    val pkg = pkgInfo.packageName
                    val installer = getInstallerPackageName(pm, pkg)
                    if (installer == PLAY_STORE_INSTALLER) return@mapNotNull null

                    val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                    val label = runCatching { appInfo.loadLabel(pm).toString() }.getOrNull() ?: pkg

                    UnknownSourceApp(
                        packageName = pkg,
                        appName = label,
                        versionName = pkgInfo.versionName,
                        installerPackage = installer,
                        installTime = pkgInfo.firstInstallTime,
                        lastUpdateTime = pkgInfo.lastUpdateTime
                    )
                }
                .sortedBy { it.appName.lowercase() }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun observeUnknownSourceApps(): Flow<List<UnknownSourceApp>> = callbackFlow {
        val appContext = context.applicationContext

        suspend fun emitNow() {
            trySend(runCatching { getAppsFromUnknownSources() }.getOrDefault(emptyList()))
        }

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                CoroutineScope(Dispatchers.Default).launch {
                    emitNow()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        appContext.registerReceiver(receiver, filter)
        CoroutineScope(Dispatchers.Default).launch {
            emitNow()
        }

        awaitClose {
            runCatching { appContext.unregisterReceiver(receiver) }
        }
    }.conflate()

    override suspend fun getMissedUpdateApps(): List<UnknownSourceApp> {
        val pm = context.packageManager
        val myPackage = context.packageName
        val staleThreshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000

        return try {
            val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            installedPackages
                .asSequence()
                .filter { it.packageName != myPackage }
                .filter { (it.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0 } // exclude system apps from missed updates
                .filter { it.lastUpdateTime < staleThreshold }
                .mapNotNull { pkgInfo ->
                    val pkg = pkgInfo.packageName
                    val installer = getInstallerPackageName(pm, pkg)
                    val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                    val label = runCatching { appInfo.loadLabel(pm).toString() }.getOrNull() ?: pkg

                    UnknownSourceApp(
                        packageName = pkg,
                        appName = label,
                        versionName = pkgInfo.versionName,
                        installerPackage = installer,
                        installTime = pkgInfo.firstInstallTime,
                        lastUpdateTime = pkgInfo.lastUpdateTime
                    )
                }
                .sortedBy { it.lastUpdateTime } // oldest first
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun observeMissedUpdates(): Flow<List<UnknownSourceApp>> = callbackFlow {
        val appContext = context.applicationContext

        suspend fun emitNow() {
            trySend(runCatching { getMissedUpdateApps() }.getOrDefault(emptyList()))
        }

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                CoroutineScope(Dispatchers.Default).launch {
                    emitNow()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        appContext.registerReceiver(receiver, filter)
        CoroutineScope(Dispatchers.Default).launch {
            emitNow()
        }

        awaitClose {
            runCatching { appContext.unregisterReceiver(receiver) }
        }
    }.conflate()

    private fun getInstallerPackageName(pm: PackageManager, packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getVersionName(pm: PackageManager, packageName: String): String? {
        return try {
            val info: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            info.versionName
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val PLAY_STORE_INSTALLER = "com.android.vending"
    }
}

