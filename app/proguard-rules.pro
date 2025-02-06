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
# Keep all JNA-related classes
-keep class com.sun.jna.** { *; }
-keep class net.java.dev.jna.** { *; }
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }
# Keep all JNA-related classes
-keep class com.sun.jna.** { *; }
-keep class net.java.dev.jna.** { *; }

# Keep all Vosk-related classes
-keep class org.vosk.** { *; }

# Prevent R8 from stripping native libraries
-keep class com.sun.jna.Native { *; }
-keep class com.sun.jna.Pointer { *; }
-keep class com.sun.jna.PointerType { *; }