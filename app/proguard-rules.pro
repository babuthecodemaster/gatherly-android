# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep model classes for Gson serialization
-keep class com.cosmic.gatherly.data.model.** { *; }
-keep class com.cosmic.gatherly.data.request.** { *; }
-keep class com.cosmic.gatherly.data.response.** { *; }

# Keep Retrofit service interface
-keep interface com.cosmic.gatherly.data.api.** { *; }

# Retrofit specific rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn retrofit2.Platform$Java8

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Application classes
-keep class com.cosmic.gatherly.** { *; }

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep custom views
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# SLF4J rules
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-keepclassmembers class org.slf4j.** { *; }