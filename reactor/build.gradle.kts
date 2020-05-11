plugins {
    id("java-library")
}


/* ******************** metadata ******************** */

description = "Reactor API for the HiveMQ MQTT Client"

metadata {
    moduleName = "com.hivemq.client.mqtt.reactor"
    readableName = "HiveMQ MQTT Client reactor module"
}


/* ******************** dependencies ******************** */

dependencies {
    api(rootProject)
    api("io.projectreactor:reactor-core:${property("reactor.version")}")

    implementation("io.projectreactor.addons:reactor-adapter:${property("reactor-adapter.version")}")
    implementation("org.jetbrains:annotations:${property("annotations.version")}")
}


/* ******************** test ******************** */

dependencies {
    testImplementation("io.projectreactor:reactor-test:${property("reactor.version")}")
    testImplementation("com.google.guava:guava:${property("guava.version")}")
}


/* ******************** jars ******************** */

tasks.jar {
    withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
        bnd("Export-Package: " +
                "com.hivemq.client.mqtt.mqtt3.reactor," +
                "com.hivemq.client.mqtt.mqtt5.reactor," +
                "com.hivemq.client.rx.reactor")
    }
}
