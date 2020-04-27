pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "4.0.4"
        id("biz.aQute.bnd.builder") version "5.0.0"
        id("com.github.hierynomus.license") version "0.14.0"
        id("com.jfrog.bintray") version "1.8.4"
        id("com.github.breadmoirai.github-release") version "2.2.9"
    }
}

rootProject.name = "hivemq-mqtt-client"

include("examples")
project(":examples").name = "hivemq-mqtt-client-examples"

include("reactor")
project(":reactor").name = "hivemq-mqtt-client-reactor"
