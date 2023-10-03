rootProject.name = "hivemq-mqtt-client2"

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "${extra["plugin.shadow.version"]}"
        id("biz.aQute.bnd.builder") version "${extra["plugin.bnd.version"]}"
        id("io.github.gradle-nexus.publish-plugin") version "${extra["plugin.nexus-publish.version"]}"
        id("io.github.sgtsilvio.gradle.defaults") version "${extra["plugin.defaults.version"]}"
        id("io.github.sgtsilvio.gradle.metadata") version "${extra["plugin.metadata.version"]}"
        id("io.github.sgtsilvio.gradle.javadoc-links") version "${extra["plugin.javadoc-links.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

for (module in listOf("websocket", "proxy", "epoll", "reactor", "examples")) {
    include("${rootProject.name}-$module")
    project(":${rootProject.name}-$module").projectDir = file(module)
}
