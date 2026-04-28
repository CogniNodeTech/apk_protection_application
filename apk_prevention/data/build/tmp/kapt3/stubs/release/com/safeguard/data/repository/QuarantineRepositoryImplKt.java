package com.safeguard.data.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.repository.QuarantineRecord;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.entity.DeletedApkEntity;
import com.safeguard.data.local.database.entity.QuarantineRecordEntity;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.Flow;
import java.io.File;
import java.security.MessageDigest;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\f\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a\f\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0002\u00a8\u0006\u0003"}, d2 = {"toDomain", "Lcom/safeguard/core/domain/repository/QuarantineRecord;", "Lcom/safeguard/data/local/database/entity/QuarantineRecordEntity;", "data_release"})
public final class QuarantineRepositoryImplKt {
    
    private static final com.safeguard.core.domain.repository.QuarantineRecord toDomain(com.safeguard.data.local.database.entity.QuarantineRecordEntity $this$toDomain) {
        return null;
    }
}