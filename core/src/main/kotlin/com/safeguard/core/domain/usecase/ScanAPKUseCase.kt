package com.safeguard.core.domain.usecase

import com.safeguard.core.domain.model.ScanResult
import java.io.File

interface ScanAPKUseCase {
    /**
     * @param displayName If provided (e.g. from content URI), used as apkName in the result instead of apkFile.name.
     */
    suspend fun execute(apkFile: File, displayName: String? = null): ScanResult
}
