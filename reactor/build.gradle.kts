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
    api("io.projectreactor:reactor-core:3.3.4.RELEASE")

    implementation("io.projectreactor.addons:reactor-adapter:3.3.3.RELEASE")
    implementation("org.jetbrains:annotations:${rootProject.extra["jetbrainsAnnotationsVersion"]}")
}


/* ******************** test ******************** */

dependencies {
    testImplementation("io.projectreactor:reactor-test:3.3.4.RELEASE")
    testImplementation("com.google.guava:guava:24.1-jre")
}


/* ******************** jars ******************** */

tasks.jar {
    manifest.attributes["Export-Package"] = "com.hivemq.client.mqtt.mqtt3.reactor, com.hivemq.client.mqtt.mqtt5.reactor, com.hivemq.client.rx.reactor"
}
