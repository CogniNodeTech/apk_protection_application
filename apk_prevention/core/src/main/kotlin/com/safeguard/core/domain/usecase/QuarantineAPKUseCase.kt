package com.safeguard.core.domain.usecase

import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.QuarantineRecord

interface QuarantineAPKUseCase {
    suspend fun execute(apkPath: String, result: ScanResult): QuarantineRecord
}
