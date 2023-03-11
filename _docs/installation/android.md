---
title: Android
nav_order: 1
redirect_from: /docs/installation/android.html
---

{% comment %}disable jekyll-titles-from-headings{% endcomment %}
# Installation on Android

The HiveMQ MQTT Client library is supported on Android 4.4 (API level 19) and higher versions.
This covers almost 100% of all Android devices.

To be able to use the HiveMQ MQTT Client on Android, configure your Android project with the following 5 steps:

1. Use Android Gradle plugin 7.0 or newer.

2. Grant your app the permission to use internet communication.
   You configure this permission in the `AndroidManifest.xml` that is typically located at 
   `$PROJECT_DIR/app/src/main/AndroidManifest.xml`:

   ```xml
   <manifest>
       <uses-permission android:name="android.permission.INTERNET"/>
   </manifest>
   ```

3. Configure your app's `build.gradle(.kts)` file that is typically located at `$PROJECT_DIR/app/build.gradle(.kts)`:

   {% capture tab_content %}

   Groovy DSL
   ===

   ```groovy
   android {
       compileOptions {
           sourceCompatibility = JavaVersion.VERSION_1_8
           targetCompatibility = JavaVersion.VERSION_1_8
       }
       packagingOptions {
           resources {
               excludes += ['META-INF/INDEX.LIST', 'META-INF/io.netty.versions.properties']
           }
       }
   }
   ```

   ====

   Kotlin DSL
   ===

   ```kotlin
   android {
       compileOptions {
           sourceCompatibility = JavaVersion.VERSION_1_8
           targetCompatibility = JavaVersion.VERSION_1_8
       }
       packagingOptions {
           resources {
               excludes += listOf("META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties")
           }
       }
   }
   ```

   {% endcapture %}
   {% include tabs.html group="gradle-dsl" content=tab_content %}

4. Add the following lines to your app's proguard rules file that is typically located at 
   `$PROJECT_DIR/app/proguard-rules.pro`:

   ```
   -keepclassmembernames class io.netty.** { *; }
   -keepclassmembers class org.jctools.** { *; }
   ```
   
   Please make sure that the `proguard-rules.pro` file is referenced in your app's `build.gradle(.kts)` file:

   {% capture tab_content %}

   Groovy DSL
   ===

   ```groovy
   android {
       buildTypes {
           release {
               minifyEnabled = true
               proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
           }
       }
   }
   ```

   ====

   Kotlin DSL
   ===

   ```kotlin
   android {
       buildTypes {
           release {
               isMinifyEnabled = true
               proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
           }
       }
   }
   ```

   {% endcapture %}
   {% include tabs.html group="gradle-dsl" content=tab_content %}

5. You are done if your minimum supported Android API level is at least 24 (7.0 Nougat).
   If you want to support older Android versions, continue with [the next step](#android-api-levels-below-24).

## Android API Levels below 24

The above configuration is enough if your minimum supported Android API level is at least 24 (7.0 Nougat).
Supporting older Android versions requires additional configuration as Android still lacks support for Java 8 APIs like
`java.util.concurrent.CompletableFuture` on versions below 24.

You can use the [Android RetroFix Gradle plugin](https://github.com/SgtSilvio/android-retrofix) to backport these APIs 
automatically.
This plugin enables you to use the Java 8 APIs even if you have to support lower Android versions.

The following shows how to configure the Android RetroFix plugin.

{% capture tab_content %}

Groovy DSL
===

Ensure that the `google` and `gradlePluginPortal` plugin repositories are configured in the `settings.gradle` file:

```groovy
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}
```

In your app’s `build.gradle` file, apply the Android RetroFix plugin and add the backport dependencies
`android-retrostreams` and `android-retrofuture`:

```groovy
plugins {
    id('com.android.application')
    id('com.github.sgtsilvio.gradle.android-retrofix') version '{{ site.android_retrofix_version }}'
}

dependencies {
    implementation('com.hivemq:hivemq-mqtt-client:{{ site.version }}')

    retrofix('net.sourceforge.streamsupport:android-retrostreams:{{ site.android_retrostreams_version }}')
    retrofix('net.sourceforge.streamsupport:android-retrofuture:{{ site.android_retrostreams_version }}')
}
```

====

Kotlin DSL
===

Ensure that the `google` and `gradlePluginPortal` plugin repositories are configured in the `settings.gradle.kts` file:

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}
```

In your app’s `build.gradle.kts` file, apply the Android RetroFix plugin and add the backport dependencies
`android-retrostreams` and `android-retrofuture`:

```kotlin
plugins {
    id("com.android.application")
    id("com.github.sgtsilvio.gradle.android-retrofix") version "{{ site.android_retrofix_version }}"
}

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:{{ site.version }}")

    retrofix("net.sourceforge.streamsupport:android-retrostreams:{{ site.android_retrostreams_version }}")
    retrofix("net.sourceforge.streamsupport:android-retrofuture:{{ site.android_retrostreams_version }}")
}
```

{% endcapture %}
{% include tabs.html group="gradle-dsl" content=tab_content %}

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
