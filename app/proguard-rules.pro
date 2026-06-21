#-------------------------------------------------
# 1. Base Android Rules
#-------------------------------------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

#-------------------------------------------------
# 2. Kotlinx Serialization
#-------------------------------------------------
# Keep `Serializable` annotated classes
-keep @kotlinx.serialization.Serializable class * {
    *;
}
-keepnames class kotlinx.serialization.internal.**
-keepclassmembers class kotlinx.serialization.internal.** {
    *** INSTANCE;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

#-------------------------------------------------
# 3. Retrofit 2
#-------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

#-------------------------------------------------
# 4. Room Database
#-------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }

#-------------------------------------------------
# 5. Coil
#-------------------------------------------------
-dontwarn coil.**
-keep class coil.** { *; }

#-------------------------------------------------
# 6. OkHttp 3
#-------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

#-------------------------------------------------
# 7. Coroutines
#-------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

#-------------------------------------------------
# 8. WorkManager
#-------------------------------------------------
-keep class androidx.work.Worker { *; }
-keep class androidx.work.CoroutineWorker { *; }

#-------------------------------------------------
# 9. App Models / Domain
#-------------------------------------------------
-keep class ru.bitvibe.waggy.data.remote.** { *; }
-keep class ru.bitvibe.waggy.data.local.** { *; }
-keep class ru.bitvibe.waggy.domain.models.** { *; }

#-------------------------------------------------
# 10. Glance Widget
#-------------------------------------------------
-keep class androidx.glance.** { *; }
-keep public class * implements androidx.glance.appwidget.action.ActionCallback {
    <init>();
}
