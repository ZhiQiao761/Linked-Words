-optimizationpasses 5
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,Exceptions
-keepattributes InnerClasses,LineNumberTable
#-keepattributes Signature,LineNumberTable,SourceFile

-keep class **.R$* { public static final int *; }
-keep class **$Properties

-keepclassmembers,allowshrinking class awais.backworddictionary.custom.WordItem { private *; }

-keep,allowshrinking public class android.webkit.*
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep,allowshrinking public class * extends androidx.multidex.MultiDexApplication

-keep,allowshrinking class * implements java.lang.annotation.Annotation
-keep public class * implements android.os.IInterface
-keep public class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembernames,allowshrinking public class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembernames public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembernames,allowshrinking public class * extends android.widget.NumberPicker {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers,allowshrinking class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class java.nio.* { *; }
-keep public class * implements androidx.versionedparcelable.VersionedParcelable {
    <init>();
}

-dontwarn rx.*
-dontwarn okio.**
-dontwarn org.apache.**
-dontwarn java.lang.invoke.*
-dontwarn android.webkit.WebView

#noinspection ShrinkerUnresolvedReference
################## GOOGLE ##################
#-keep class com.google.gson.** { *; }
#-keep class com.google.** { *; }
#-keep class com.google.gson.stream.** { *; }
#-keep class com.google.gson.examples.android.model.** { *; }
#
#-keep class com.android.volley.** { *; }
#-keep class com.android.volley.toolbox.** { *; }
#-keep class com.android.volley.Response$* { *; }
#-keep class com.android.volley.Request$* { *; }
#-keep class com.android.volley.RequestQueue$* { *; }
#-keep class com.android.volley.toolbox.HurlStack$* { *; }
#-keep class com.android.volley.toolbox.ImageLoader$* { *; }
