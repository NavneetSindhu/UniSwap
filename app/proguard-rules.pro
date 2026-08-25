# ==============================================================================
# UniSwap Production ProGuard / R8 Optimization & Obfuscation Rules
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Stack Traces, Source Maps & Crashlytics De-obfuscation
# ------------------------------------------------------------------------------
# Retain line number table and source file attributes so Firebase Crashlytics
# can accurately map stack traces to original Kotlin source lines.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve essential annotations and JVM generic signatures for reflection/deserialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ------------------------------------------------------------------------------
# 2. General Kotlin & Enum Preservation
# ------------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ------------------------------------------------------------------------------
# 3. UniSwap Data & Domain Models (Firestore, Room & Serialization)
# ------------------------------------------------------------------------------
# Preserve all data classes and properties under UniSwap models to prevent
# Firebase Firestore reflection deserialization crashes.
-keep class com.minimize.uniswap.data.model.** { *; }
-keepclassmembers class com.minimize.uniswap.data.model.** { *; }

# Keep any classes annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keep @androidx.annotation.Keep interface * { *; }

# ------------------------------------------------------------------------------
# 4. AndroidX Room Database
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 5. Dagger Hilt Dependency Injection
# ------------------------------------------------------------------------------
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# ------------------------------------------------------------------------------
# 6. Firebase & Google Identity / Credentials
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# ------------------------------------------------------------------------------
# 7. KotlinX Coroutines & Flow
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# 8. Retrofit & Gson Serialization
# ------------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.Expose <fields>;
}
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ------------------------------------------------------------------------------
# 9. Coil & Lottie Media Libraries
# ------------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**
-keep class com.airbnb.lottie.** { *; }

# ------------------------------------------------------------------------------
# 10. Production Log Stripping (Timber & Android Log)
# ------------------------------------------------------------------------------
# Strip debug, verbose, and informational logs in release binaries for security & size
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}