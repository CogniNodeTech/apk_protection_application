package com.safeguard.scan;

import com.safeguard.core.util.ApkSignatureDetector;
import java.io.File;

/**
 * Recursively walks accessible storage [root] to find every readable Android package file,
 * including inside dot-prefixed / deeply nested folders **and APKs disguised with non-`.apk`
 * filenames** (e.g. `update.zip`, `photo.dat`, no extension at all). Uses iterative DFS,
 * canonical path de-duplication (symlink loops), and caps to avoid runaway work.
 *
 * Detection tiers (cheapest first) — see [ApkSignatureDetector]:
 *  1. Extension match: `*.apk`, `*.xapk`, `*.apks`, `*.apkm` (zero IO).
 *  2. ZIP magic bytes (`PK\x03\x04`) for size-plausible files; confirmed APK by checking
 *     for an `AndroidManifest.xml` entry inside the archive.
 *
 * A `disguisedApkCount` counter is reported so the UI can surface "we caught N hidden APKs".
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\rB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u00042\b\b\u0002\u0010\f\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/safeguard/scan/DeepApkCollector;", "", "()V", "DEFAULT_MAX_DEPTH", "", "DEFAULT_MAX_DIR_VISITS", "MAX_DEEP_VERIFY_CANDIDATES", "collectApks", "Lcom/safeguard/scan/DeepApkCollector$Result;", "root", "Ljava/io/File;", "maxDepth", "maxDirVisits", "Result", "app_debug"})
public final class DeepApkCollector {
    private static final int DEFAULT_MAX_DEPTH = 50;
    private static final int DEFAULT_MAX_DIR_VISITS = 120000;
    
    /**
     * Cap on the number of size-plausible non-`.apk`-extension files we will fully ZIP-verify
     * per scan. Magic-byte filtering already prunes >99% of media; this cap protects against
     * pathological storages with thousands of large ZIP archives.
     */
    private static final int MAX_DEEP_VERIFY_CANDIDATES = 5000;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.scan.DeepApkCollector INSTANCE = null;
    
    private DeepApkCollector() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.scan.DeepApkCollector.Result collectApks(@org.jetbrains.annotations.NotNull
    java.io.File root, int maxDepth, int maxDirVisits) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0012\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\nJ\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00c6\u0003J7\u0010\u0016\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\b2\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001c"}, d2 = {"Lcom/safeguard/scan/DeepApkCollector$Result;", "", "apkFiles", "", "Ljava/io/File;", "directoriesVisited", "", "truncated", "", "disguisedApkCount", "(Ljava/util/List;IZI)V", "getApkFiles", "()Ljava/util/List;", "getDirectoriesVisited", "()I", "getDisguisedApkCount", "getTruncated", "()Z", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "toString", "", "app_debug"})
    public static final class Result {
        @org.jetbrains.annotations.NotNull
        private final java.util.List<java.io.File> apkFiles = null;
        private final int directoriesVisited = 0;
        private final boolean truncated = false;
        private final int disguisedApkCount = 0;
        
        public Result(@org.jetbrains.annotations.NotNull
        java.util.List<? extends java.io.File> apkFiles, int directoriesVisited, boolean truncated, int disguisedApkCount) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<java.io.File> getApkFiles() {
            return null;
        }
        
        public final int getDirectoriesVisited() {
            return 0;
        }
        
        public final boolean getTruncated() {
            return false;
        }
        
        public final int getDisguisedApkCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<java.io.File> component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final boolean component3() {
            return false;
        }
        
        public final int component4() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.scan.DeepApkCollector.Result copy(@org.jetbrains.annotations.NotNull
        java.util.List<? extends java.io.File> apkFiles, int directoriesVisited, boolean truncated, int disguisedApkCount) {
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
}