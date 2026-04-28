package com.safeguard.data.repository;

import com.safeguard.core.domain.repository.CloudVerificationRepository;
import com.safeguard.core.domain.repository.CloudVerificationResponse;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.core.domain.repository.DeviceCloudMetadata;
import com.safeguard.core.domain.repository.LocalLayerScores;
import com.safeguard.data.remote.dto.DeviceMetadataJson;
import com.safeguard.data.remote.dto.LocalLayerScoresJson;
import com.safeguard.data.remote.dto.VerificationRequest;
import retrofit2.HttpException;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0003X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0004\u001a\u00020\u0003X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0005\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"CIRCUIT_FAIL_THRESHOLD", "", "CIRCUIT_OPEN_MS", "", "INITIAL_DELAY_MS", "MAX_ATTEMPTS", "data_release"})
public final class CloudVerificationRepositoryImplKt {
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 1000L;
    private static final int CIRCUIT_FAIL_THRESHOLD = 5;
    private static final long CIRCUIT_OPEN_MS = 60000L;
}