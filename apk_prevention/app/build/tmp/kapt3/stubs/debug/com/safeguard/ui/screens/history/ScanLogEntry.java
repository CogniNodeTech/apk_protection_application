package com.safeguard.ui.screens.history;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.ScanRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u001d\b\u0086\b\u0018\u00002\u00020\u0001BS\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u0003\u0012\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f\u00a2\u0006\u0002\u0010\u0010J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\bH\u00c6\u0003J\t\u0010#\u001a\u00020\nH\u00c6\u0003J\t\u0010$\u001a\u00020\fH\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u00c6\u0003Ji\u0010\'\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u00032\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u00c6\u0001J\u0013\u0010(\u001a\u00020\b2\b\u0010)\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010*\u001a\u00020\fH\u00d6\u0001J\t\u0010+\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0018R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0012R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d\u00a8\u0006,"}, d2 = {"Lcom/safeguard/ui/screens/history/ScanLogEntry;", "", "id", "", "apkName", "dateGroup", "timeLabel", "isThreat", "", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "threatScore", "", "explanation", "evidenceBullets", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLcom/safeguard/core/domain/model/Verdict;ILjava/lang/String;Ljava/util/List;)V", "getApkName", "()Ljava/lang/String;", "getDateGroup", "getEvidenceBullets", "()Ljava/util/List;", "getExplanation", "getId", "()Z", "getThreatScore", "()I", "getTimeLabel", "getVerdict", "()Lcom/safeguard/core/domain/model/Verdict;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ScanLogEntry {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apkName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String dateGroup = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String timeLabel = null;
    private final boolean isThreat = false;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.model.Verdict verdict = null;
    private final int threatScore = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String explanation = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> evidenceBullets = null;
    
    public ScanLogEntry(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String dateGroup, @org.jetbrains.annotations.NotNull
    java.lang.String timeLabel, boolean isThreat, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, int threatScore, @org.jetbrains.annotations.NotNull
    java.lang.String explanation, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> evidenceBullets) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApkName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDateGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTimeLabel() {
        return null;
    }
    
    public final boolean isThreat() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict getVerdict() {
        return null;
    }
    
    public final int getThreatScore() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getExplanation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getEvidenceBullets() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.history.ScanLogEntry copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String dateGroup, @org.jetbrains.annotations.NotNull
    java.lang.String timeLabel, boolean isThreat, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, int threatScore, @org.jetbrains.annotations.NotNull
    java.lang.String explanation, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> evidenceBullets) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}