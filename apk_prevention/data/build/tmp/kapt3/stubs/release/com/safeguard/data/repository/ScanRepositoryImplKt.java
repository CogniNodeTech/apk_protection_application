package com.safeguard.data.repository;

import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.LayerResult;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.ThreatInfo;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.cache.LayerResultDto;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.entity.AuditLogEntity;
import com.safeguard.data.local.database.entity.ScanRecordEntity;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001a\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\u001a\"\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u00022\u0012\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004H\u0002\u00a8\u0006\u0007"}, d2 = {"toDomain", "Lcom/safeguard/core/domain/model/ScanResult;", "Lcom/safeguard/data/local/database/entity/ScanRecordEntity;", "adapter", "Lcom/squareup/moshi/JsonAdapter;", "", "Lcom/safeguard/data/local/cache/LayerResultDto;", "data_release"})
public final class ScanRepositoryImplKt {
    
    private static final com.safeguard.core.domain.model.ScanResult toDomain(com.safeguard.data.local.database.entity.ScanRecordEntity $this$toDomain, com.squareup.moshi.JsonAdapter<java.util.List<com.safeguard.data.local.cache.LayerResultDto>> adapter) {
        return null;
    }
}