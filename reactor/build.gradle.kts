plugins {
    `java-library`
    alias(libs.plugins.mavenCentralPublishing)
}


/* ******************** metadata ******************** */

description = "Reactor API for the HiveMQ MQTT Client"

metadata {
    moduleName.set("com.hivemq.mqtt.client2.reactor")
    readableName.set("HiveMQ MQTT Client reactor module")
}


/* ******************** dependencies ******************** */

dependencies {
    api(rootProject)
    api(libs.reactor)
    compileOnlyApi(libs.jetbrains.annotations)
    implementation(libs.reactor.adapter)
}


/* ******************** test ******************** */

dependencies {
    testImplementation(libs.reactor.test)
    testImplementation(libs.guava)
}


/* ******************** jars ******************** */

tasks.jar {
    configure<aQute.bnd.gradle.BundleTaskExtension> {
        bnd("-exportcontents: !*.internal.*, com.hivemq.mqtt.client2.*")
    }
}
