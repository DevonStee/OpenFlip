# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/Cellar/android-sdk/24.4.1_1/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep Widget classes from obfuscation
-keep class com.bokehforu.openflip.widget.** { *; }

# Keep UI/settings classes from obfuscation (as requested)
-keep class com.bokehforu.openflip.feature.clock.ui.** { *; }
-keep class com.bokehforu.openflip.feature.clock.view.** { *; }
-keep class com.bokehforu.openflip.feature.settings.ui.** { *; }
-keep class com.bokehforu.openflip.feature.settings.viewmodel.** { *; }
-keep class com.bokehforu.openflip.core.settings.** { *; }
