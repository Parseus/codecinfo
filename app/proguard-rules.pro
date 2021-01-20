-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepattributes SourceFile, LineNumberTable
-allowaccessmodification
-repackageclasses

-dontwarn android.hardware.scontext.**
-dontwarn com.samsung.**
#noinspection ShrinkerUnresolvedReference
-keep class com.samsung.** { *; }

# TODO: Remove after LeakCanary 2.7 is released
# Enum values are referenced reflectively in EnumSet initialization
-keepclassmembers,allowoptimization enum leakcanary.AndroidLeakFixes {
    public static **[] values();
}