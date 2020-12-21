pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "${extra["plugin.shadow.version"]}"
        id("biz.aQute.bnd.builder") version "${extra["plugin.bnd.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("com.jfrog.bintray") version "${extra["plugin.bintray.version"]}"
        id("com.github.breadmoirai.github-release") version "${extra["plugin.github-release.version"]}"
        id("com.github.sgtsilvio.gradle.utf8") version "${extra["plugin.utf8.version"]}"
        id("com.github.sgtsilvio.gradle.metadata") version "${extra["plugin.metadata.version"]}"
        id("com.github.sgtsilvio.gradle.javadoc-links") version "${extra["plugin.javadoc-links.version"]}"
    }
}

rootProject.name = "hivemq-mqtt-client2"

listOf("websocket", "proxy", "epoll", "reactor", "examples").forEach { module ->
    include("${rootProject.name}-$module")
    project(":${rootProject.name}-$module").projectDir = file(module)
}
