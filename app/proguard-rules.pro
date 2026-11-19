# ProGuard / R8 rules for ML Counters

# Keep Room entities
-keep class com.mlcounters.app.data.local.entity.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mlcounters.app.**$$serializer { *; }
-keepclassmembers class com.mlcounters.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.mlcounters.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data remote DTOs
-keep class com.mlcounters.app.data.remote.dto.** { *; }

# OpenCV
-keep class org.opencv.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
