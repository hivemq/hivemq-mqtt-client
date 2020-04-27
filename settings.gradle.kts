pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "${settings.extra["plugin.shadow.version"]}"
        id("biz.aQute.bnd.builder") version "${settings.extra["plugin.bnd.version"]}"
        id("com.github.hierynomus.license") version "${settings.extra["plugin.license.version"]}"
        id("com.jfrog.bintray") version "${settings.extra["plugin.bintray.version"]}"
        id("com.github.breadmoirai.github-release") version "${settings.extra["plugin.github-release.version"]}"
    }
}

rootProject.name = "hivemq-mqtt-client"

include("examples")
project(":examples").name = "hivemq-mqtt-client-examples"

include("reactor")
project(":reactor").name = "hivemq-mqtt-client-reactor"
