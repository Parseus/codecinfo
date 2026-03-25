-processkotlinnullchecks remove

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  public static void throwUninitializedPropertyAccessException(java.lang.String);
}

-checkdiscard class kotlin.Metadata

-assumenosideeffects public class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    public java.lang.StackTraceElement getStackTraceElement() return null;
}
-checkdiscard class kotlin.coroutines.jvm.internal.DebugMetadata

-maximumremovedandroidloglevel 7

-keep public class androidx.recyclerview.widget.RecyclerView$LayoutManager {
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public <init>();
}
-keep class androidx.recyclerview.widget.LinearLayoutManager {
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public <init>();
}
-keep class com.parseus.codecinfo.ui.CustomLinearLayoutManager