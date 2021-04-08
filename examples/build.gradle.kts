plugins {
    id("java")
}


/* ******************** metadata ******************** */

description = "Examples using the HiveMQ MQTT Client"

metadata {
    moduleName.set("com.hivemq.client.mqtt.examples")
    readableName.set("HiveMQ MQTT Client examples")
}


/* ******************** dependencies ******************** */

dependencies {
    implementation(rootProject)
}
