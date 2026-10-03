# MessMate R8 / Proguard Rules

-keep class com.singleminds.messmate.data.local.entity.** { *; }
-keep class com.singleminds.messmate.engine.** { *; }

# Room Keep Rules
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-dontwarn androidx.room.**
