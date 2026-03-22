# ProGuard rules for Shijian AI Trading App

# Keep source file names and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep all public and protected methods for the app
-keep public class com.shijian.aitrading.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
