package com.safeguard.core.domain.repository

import com.safeguard.core.domain.model.UnknownSourceApp
import kotlinx.coroutines.flow.Flow

interface InstalledAppsRepository {
    suspend fun getAppsFromUnknownSources(): List<UnknownSourceApp>
    fun observeUnknownSourceApps(): Flow<List<UnknownSourceApp>>
    
    suspend fun getMissedUpdateApps(): List<UnknownSourceApp>
    fun observeMissedUpdates(): Flow<List<UnknownSourceApp>>
}

