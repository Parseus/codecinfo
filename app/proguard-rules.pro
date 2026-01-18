-processkotlinnullchecks remove

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  public static void throwUninitializedPropertyAccessException(java.lang.String);
}

-checkdiscard class kotlin.Metadata

-assumenosideeffects public class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    public java.lang.StackTraceElement getStackTraceElement() return null;
}
-checkdiscard class kotlin.coroutines.jvm.internal.DebugMetadata

-keepattributes SourceFile, LineNumberTable
-allowaccessmodification
-repackageclasses