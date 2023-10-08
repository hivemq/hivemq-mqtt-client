plugins {
    `java-library`
}


/* ******************** metadata ******************** */

description = "Reactor API for the HiveMQ MQTT Client"

metadata {
    moduleName.set("com.hivemq.client2.mqtt.reactor")
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
        bnd("Export-Package: " +
                "com.hivemq.client2.mqtt.mqtt3.reactor," +
                "com.hivemq.client2.mqtt.mqtt5.reactor," +
                "com.hivemq.client2.rx.reactor")
    }
}
