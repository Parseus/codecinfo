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