plugins {
    id("java-library")
}


/* ******************** metadata ******************** */

description = "Reactor API for the HiveMQ MQTT Client"

extra["moduleName"] = "com.hivemq.client.mqtt.reactor"
extra["readableName"] = "HiveMQ MQTT Client reactor module"
extra["prevVersion"] = "1.2.0"


/* ******************** dependencies ******************** */

dependencies {
    api(rootProject)
    api("io.projectreactor:reactor-core:${property("reactor.version")}")

    implementation("io.projectreactor.addons:reactor-adapter:${property("reactor-adapter.version")}")
    implementation("org.jetbrains:annotations:${property("jetbrains-annotations.version")}")
}


/* ******************** test ******************** */

dependencies {
    testImplementation("io.projectreactor:reactor-test:${property("reactor.version")}")
    testImplementation("com.google.guava:guava:${property("guava.version")}")
}


/* ******************** jars ******************** */

tasks.jar {
    manifest.attributes["Export-Package"] = "com.hivemq.client.mqtt.mqtt3.reactor, com.hivemq.client.mqtt.mqtt5.reactor, com.hivemq.client.rx.reactor"
}
