---
layout: default
title: Android
parent: Installation
nav_order: 1
---

# Installation on Android

To use the HiveMQ MQTT Client on Android, you have to enable the permission to use internet communication in the 
`AndroidManifest.xml` that is typically located at `$PROJECT_DIR/app/src/main/AndroidManifest.xml`:

```xml
<manifest>
    ...
    <uses-permission android:name="android.permission.INTERNET"/>
    ...
</manifest>
```

As the HiveMQ MQTT Client uses Java 8 language features, you also have to specify the following in the app's 
`build.grade` file that is typically located at `$PROJECT_DIR/app/build.gradle`:

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
-keepclassmembernames class org.jctools.** { *; }
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


## Android API levels < 24

The HiveMQ MQTT Client uses Java 8 APIs such as `java.util.Optional`, `java.util.function.*` and 
`java.util.concurrent.CompletableFuture`.
Unfortunately, Android still lacks support of theses APIs on Android versions lower than 7.0 Nougat (API level 24).
If you are targeting API levels smaller than 24, you have to use the 
[Android RetroFix gradle plugin](https://github.com/SgtSilvio/android-retrofix) to backport these APIs automatically.
The minimum supported API level is 19. This covers almost 100% of all Android devices.

Add the following to the app's `build.gradle` file:

```groovy
buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath 'gradle.plugin.com.github.sgtsilvio.gradle:android-retrofix:0.2.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.sgtsilvio.gradle.android-retrofix'

...

dependencies {
    implementation 'com.hivemq:hivemq-mqtt-client:1.1.1'
    
    implementation 'net.sourceforge.streamsupport:android-retrostreams:1.7.1'
    implementation 'net.sourceforge.streamsupport:android-retrofuture:1.7.1'
    ...
}
```

The Android RetroFix plugin enables you to use the Java 8 APIs even if you have to support lower Android versions.
When you increase the Android API level to 24+ in the future, you only need to remove the plugin and the backport 
dependencies. You do not have to change your code.
