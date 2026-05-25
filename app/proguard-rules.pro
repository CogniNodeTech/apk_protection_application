# SafeGuard - ProGuard rules
# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.gpu.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.crypto.tink.util.KeysDownloader
-dontwarn com.google.api.client.http.**
-dontwarn javax.lang.model.element.Modifier
-dontwarn org.joda.time.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Moshi (Kotlin reflect)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# APK Parser / dexlib2
-keep class net.dongliu.apkparser.** { *; }
-dontwarn org.jf.dexlib2.**

# SQLCipher
-keep class net.zetetic.database.** { *; }

# SafeGuard Security Hardening (Obfuscate everything else)
-keepnames class com.safeguard.core.domain.model.** { *; }
-keepclassmembers class com.safeguard.core.domain.model.** { *; }

# Reduce log-based info disclosure in release (retain w/e for serious diagnostics)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Specifically obfuscate RASP and ML implementation details
-repackageclasses 'com.safeguard.security.internal'
-allowaccessmodification
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# Keep Hilt/Dagger
-keep class * { @dagger.Provides *; }
-keep class * { @dagger.Module *; }
