plugins {
    id("java-library")
    id("io.github.sgtsilvio.gradle.maven-central-publishing")
}

/* ******************** metadata ******************** */

description = "Reactor API for the HiveMQ MQTT Client"

metadata {
    moduleName.set("com.hivemq.client.mqtt.reactor")
    readableName.set("HiveMQ MQTT Client reactor module")
}

/* ******************** dependencies ******************** */

dependencies {
    api(rootProject)
    api(libs.reactor.core)

    implementation(libs.reactor.adapter)
    implementation(libs.jetbrains.annotations)
}

/* ******************** test ******************** */

dependencies {
    testImplementation(libs.reactor.test)
    testImplementation(libs.guava)
}

/* ******************** jars ******************** */

tasks.jar {
    bundle {
        bnd("Export-Package: " +
                "com.hivemq.client.mqtt.mqtt3.reactor," +
                "com.hivemq.client.mqtt.mqtt5.reactor," +
                "com.hivemq.client.rx.reactor")
    }
}
