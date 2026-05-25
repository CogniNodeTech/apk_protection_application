package com.safeguard.domain

import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.QuarantineRecord
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import javax.inject.Inject

class QuarantineAPKUseCaseImpl @Inject constructor(
    private val repository: QuarantineRepository
) : QuarantineAPKUseCase {

    override suspend fun execute(apkPath: String, result: ScanResult): QuarantineRecord =
        repository.quarantine(apkPath, result)
}
