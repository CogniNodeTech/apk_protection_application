package com.safeguard.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.QuarantineRecord
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.data.local.database.dao.DeletedApkDao
import com.safeguard.data.local.database.dao.QuarantineDao
import com.safeguard.data.local.database.entity.DeletedApkEntity
import com.safeguard.data.local.database.entity.QuarantineRecordEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class QuarantineRepositoryImpl @Inject constructor(
    private val quarantineDao: QuarantineDao,
    private val deletedApkDao: DeletedApkDao,
    @Named("quarantine_dir") private val quarantineDir: File,
    @ApplicationContext private val appContext: Context
) : QuarantineRepository {

    override suspend fun quarantine(apkPath: String, result: ScanResult): QuarantineRecord {
        val id = UUID.randomUUID().toString()
        val sourceFile = File(apkPath)
        val packageNameFromApk = try {
            appContext.packageManager.getPackageArchiveInfo(apkPath, 0)?.packageName
        } catch (_: Exception) {
            null
        }
        if (!quarantineDir.exists()) quarantineDir.mkdirs()
        val destFile = File(quarantineDir, "$id.apk")
        sourceFile.copyTo(destFile, overwrite = true)
        val removed = try {
            sourceFile.delete()
        } catch (_: Exception) {
            false
        }
        // Installed apps live under app-private paths; delete often fails without system perms. Offer uninstall so the threat is removed.
        if ((!removed || sourceFile.exists()) &&
            packageNameFromApk != null &&
            packageNameFromApk != appContext.packageName
        ) {
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageNameFromApk")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start uninstall for $packageNameFromApk", e)
            }
        }
        val entity = QuarantineRecordEntity(
            id = id,
            originalPath = apkPath,
            quarantinePath = destFile.absolutePath,
            apkHash = extractOrComputeSha256(apkPath, result) ?: "",
            threatName = result.threatInfo?.threatName ?: result.finalVerdict.name,
            apkName = result.apkName,
            riskScore = result.overallRiskScore,
            quarantineTimestamp = System.currentTimeMillis(),
            autoDeleteAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            sizeBytes = result.apkSizeBytes
        )
        quarantineDao.insert(entity)
        return QuarantineRecord(
            id = entity.id,
            originalPath = entity.originalPath,
            quarantinePath = entity.quarantinePath,
            apkHash = entity.apkHash,
            threatName = entity.threatName,
            apkName = entity.apkName,
            riskScore = entity.riskScore,
            quarantinedAt = entity.quarantineTimestamp,
            autoDeleteAt = entity.autoDeleteAt,
            sizeBytes = entity.sizeBytes
        )
    }

    override suspend fun deleteFromQuarantine(id: String) {
        val entity = quarantineDao.getById(id)
        entity?.let { File(it.quarantinePath).delete() }
        quarantineDao.deleteById(id)
    }

    override suspend fun permanentlyDelete(id: String) {
        val entity = quarantineDao.getById(id)
        if (entity != null) {
            // Record the APK as permanently blocked before deleting
            deletedApkDao.insert(
                DeletedApkEntity(
                    apkName = entity.apkName ?: File(entity.originalPath).name,
                    apkSha256 = extractOrComputeSha256(entity.originalPath, null),
                    originalPath = entity.originalPath,
                    threatName = entity.threatName,
                    riskScore = entity.riskScore,
                    deletedAt = System.currentTimeMillis()
                )
            )
            // Delete the quarantined file from device
            File(entity.quarantinePath).delete()
            // Also delete the original file if it still exists
            val originalFile = File(entity.originalPath)
            if (originalFile.exists()) originalFile.delete()
        }
        quarantineDao.deleteById(id)
    }

    override suspend fun deleteAndBlockApk(apkPath: String, result: ScanResult) {
        deletedApkDao.insert(
            DeletedApkEntity(
                apkName = result.apkName,
                apkSha256 = extractOrComputeSha256(apkPath, result),
                originalPath = apkPath,
                threatName = result.threatInfo?.threatName ?: result.finalVerdict.name,
                riskScore = result.overallRiskScore,
                deletedAt = System.currentTimeMillis()
            )
        )
        val finalApkName = result.apkName ?: File(apkPath).name
        quarantineDao.deleteByApkName(finalApkName)
        
        val file = File(apkPath)
        val packageNameFromApk = try {
            appContext.packageManager.getPackageArchiveInfo(apkPath, 0)?.packageName
        } catch (_: Exception) {
            null
        }
        val removed = try {
            file.delete()
        } catch (_: Exception) {
            false
        }
        if ((!removed || file.exists()) &&
            packageNameFromApk != null &&
            packageNameFromApk != appContext.packageName
        ) {
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageNameFromApk")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start uninstall for $packageNameFromApk", e)
            }
        }
    }

    override suspend fun isApkBlocked(apkName: String, apkSha256: String?): Boolean {
        if (!apkSha256.isNullOrBlank() && deletedApkDao.isApkHashBlocked(apkSha256)) {
            return true
        }
        return deletedApkDao.isApkBlocked(apkName)
    }

    override suspend fun restoreFromQuarantine(id: String): String? {
        val entity = quarantineDao.getById(id) ?: return null
        val file = File(entity.quarantinePath)
        if (!file.exists()) return null
        val restoreDir = File(File(entity.originalPath).parent ?: "/sdcard/Download")
        if (!restoreDir.exists()) restoreDir.mkdirs()
        val restoreFile = File(restoreDir, file.name)
        file.copyTo(restoreFile, overwrite = true)
        file.delete()
        quarantineDao.deleteById(id)
        return restoreFile.absolutePath
    }

    override fun getQuarantineList(): kotlinx.coroutines.flow.Flow<List<QuarantineRecord>> =
        quarantineDao.getAllFlow().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getQuarantineCount(): Int = quarantineDao.getCount()

    override suspend fun getAutoDeleteCountdown(): Int {
        val in30Days = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
        return quarantineDao.getAutoDeleteCount(in30Days)
    }

    override suspend fun clearAllQuarantine() {
        // Delete all files in the quarantine directory
        quarantineDir.listFiles()?.forEach { it.delete() }
        // Clear database records
        quarantineDao.deleteAll()
        deletedApkDao.deleteAll()
    }

    companion object {
        private const val TAG = "QuarantineRepo"

        private fun parseSha256FromEvidence(result: ScanResult?): String? {
            val marker = "apk_sha256="
            return result?.aggregatedEvidence
                ?.firstOrNull { it.startsWith(marker) }
                ?.substringAfter(marker)
                ?.takeIf { it.length == 64 }
        }

        private fun sha256(file: File): String? {
            return try {
                if (!file.exists() || !file.isFile) return null
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } > 0) {
                        digest.update(buffer, 0, read)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            } catch (_: Exception) {
                null
            }
        }

        private fun extractOrComputeSha256(apkPath: String, result: ScanResult?): String? {
            return parseSha256FromEvidence(result) ?: sha256(File(apkPath))
        }
    }
}

private fun QuarantineRecordEntity.toDomain() = QuarantineRecord(
    id = id,
    originalPath = originalPath,
    quarantinePath = quarantinePath,
    apkHash = apkHash,
    threatName = threatName,
    apkName = apkName,
    riskScore = riskScore,
    quarantinedAt = quarantineTimestamp,
    autoDeleteAt = autoDeleteAt,
    sizeBytes = sizeBytes
)
