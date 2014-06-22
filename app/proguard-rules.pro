# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# OsmDroid
-dontwarn org.osmdroid.contributor.*
#-dontnote org.osmdroid.views.MapView

# ACRA
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep class org.acra.ACRA {
        *;
}
-keep class org.acra.ReportingInteractionMode {
    *;
}
-keepnames class org.acra.sender.HttpSender$** {
    *;
}
-keepnames class org.acra.ReportField {
    *;
}
-keep public class org.acra.ErrorReporter {
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}
-keep public class org.acra.ErrorReporter {
    public void handleSilentException(java.lang.Throwable);
}
