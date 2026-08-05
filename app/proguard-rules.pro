# Add project specific ProGuard rules here.
# See https://developer.android.com/studio/build/shrinking-code for details.
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
