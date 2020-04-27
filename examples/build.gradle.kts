plugins {
    id("java")
}


/* ******************** metadata ******************** */

description = "Examples using the HiveMQ MQTT Client"

extra["moduleName"] = "com.hivemq.client.mqtt.examples"
extra["readableName"] = "HiveMQ MQTT Client examples"


/* ******************** dependencies ******************** */

dependencies {
    implementation(rootProject)
}
