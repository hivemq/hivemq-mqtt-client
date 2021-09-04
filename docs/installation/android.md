---
title: Android
nav_order: 1
redirect_from: /docs/installation/android.html
---

# Installation on Android

The HiveMQ MQTT Client library is supported on Android 4.4 (API level 19) and higher versions.
This covers almost 100% of all Android devices.

To be able to use the HiveMQ MQTT Client on Android, you have to grant your app the permission to use internet 
communication.
You configure this permission in the `AndroidManifest.xml` that is typically located at 
`$PROJECT_DIR/app/src/main/AndroidManifest.xml`:

```xml
<manifest>
    ...
    <uses-permission android:name="android.permission.INTERNET"/>
    ...
</manifest>
```

As the HiveMQ MQTT Client uses Java 8 language features, you also have to specify the following in the app's 
`build.gradle` file that is typically located at `$PROJECT_DIR/app/build.gradle`:

```groovy
...
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude 'META-INF/INDEX.LIST'
        exclude 'META-INF/io.netty.versions.properties'
    }
}
...
```

Additionally you have to set some proguard rules in the app's proguard rules file that is typically located at 
`$PROJECT_DIR/app/proguard-rules.pro`:

```
...
-keepclassmembernames class io.netty.** { *; }
-keepclassmembers class org.jctools.** { *; }
...
```

Please make sure that the `proguard-rules.pro` file is referenced in the app's `build.gradle` file:

```groovy
...
android {
    ...
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        ...
    }
    ...
}
...
```


## Android API levels below 24

The above configuration is enough if your minimum supported Android API level is at least 24 (7.0 Nougat).
Targeting lower Android versions requires additional configuration as Android still lacks support for Java 8 APIs like 
`java.util.Optional`, `java.util.function.*` and `java.util.concurrent.CompletableFuture` on versions below 24.

You can use the [Android RetroFix gradle plugin](https://github.com/SgtSilvio/android-retrofix) to backport these APIs 
automatically.
This plugin enables you to use the Java 8 APIs even if you have to support lower Android versions.

The following shows how to configure the Android RetroFix plugin in the app’s `build.gradle` file.
You have to add the plugin and the two backport dependencies `android-retrostreams` and `android-retrofuture`:

```groovy
buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath 'gradle.plugin.com.github.sgtsilvio.gradle:android-retrofix:{{ site.android_retrofix_version }}'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.sgtsilvio.gradle.android-retrofix'

...

dependencies {
    implementation 'com.hivemq:hivemq-mqtt-client:{{ site.version }}'
    
    implementation 'net.sourceforge.streamsupport:android-retrostreams:{{ site.android_retrostreams_version }}'
    implementation 'net.sourceforge.streamsupport:android-retrofuture:{{ site.android_retrostreams_version }}'
    ...
}
```

When you increase the Android API level to 24+ in the future, you will only need to remove the plugin and the backport 
dependencies.
You do not have to change your code.

{% include admonition.html type="note" title="Note" content='

Android Studio will still display an error "Call requires API level 24 (current min is 21)".
This error is actually just a warning.
Android Studio does not know that we backport the API, so it still thinks that the API can not be used with the 
minSdkVersion.
You can build and run your app without any problems.
If you want to get rid of the warning, just add `@SuppressLint("NewApi")` to the method or class where you use the API.

'%}
