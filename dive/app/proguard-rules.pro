# Add project specific ProGuard rules here.
-keepattributes SourceFile,LineNumberTable
-dontobfuscate
-keepclassmembers class com.google.firebase.database.GenericTypeIndicator{*;}
-keep class * extends com.google.firebase.database.GenericTypeIndicator{*;}
-keep class com.google.firebase.database.GenericTypeIndicator{*;}
-keepclassmembers class me.vavra.dive.common.** {
  *;
}