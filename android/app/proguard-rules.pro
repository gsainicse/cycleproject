# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.cycleproject.b2b.models.** { *; }
-keep class com.cycleproject.b2b.api.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
