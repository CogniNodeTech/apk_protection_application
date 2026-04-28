# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.apkprevention.auth.data.model.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-dontwarn dagger.hilt.**

# Facebook SDK
-keep class com.facebook.** { *; }
-keepattributes Signature

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
