package com.safeguard.security.quarantine

import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.QuarantineRecord
import com.safeguard.core.domain.repository.QuarantineRepository
import java.io.File
import javax.inject.Inject

/**
 * Delegates to QuarantineRepository; provides a single place for quarantine operations.
 */
class QuarantineManager @Inject constructor(
    private val repository: QuarantineRepository
) {
    suspend fun quarantine(apkPath: String, result: ScanResult): QuarantineRecord =
        repository.quarantine(apkPath, result)

    suspend fun delete(id: String) = repository.deleteFromQuarantine(id)
    suspend fun restore(id: String): String? = repository.restoreFromQuarantine(id)
    fun getQuarantineList() = repository.getQuarantineList()
    suspend fun getCount() = repository.getQuarantineCount()
    suspend fun getAutoDeleteCountdown() = repository.getAutoDeleteCountdown()
}
