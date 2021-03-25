rootProject.name = "hivemq-mqtt-client"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "${extra["plugin.shadow.version"]}"
        id("biz.aQute.bnd.builder") version "${extra["plugin.bnd.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("com.github.breadmoirai.github-release") version "${extra["plugin.github-release.version"]}"
        id("com.github.sgtsilvio.gradle.utf8") version "${extra["plugin.utf8.version"]}"
        id("com.github.sgtsilvio.gradle.metadata") version "${extra["plugin.metadata.version"]}"
        id("com.github.sgtsilvio.gradle.javadoc-links") version "${extra["plugin.javadoc-links.version"]}"
        id("io.github.gradle-nexus.publish-plugin") version "${extra["plugin.nexus.publish.version"]}"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

listOf("websocket", "proxy", "epoll", "reactor", "examples").forEach { module ->
    include("${rootProject.name}-$module")
    project(":${rootProject.name}-$module").projectDir = file(module)
}
