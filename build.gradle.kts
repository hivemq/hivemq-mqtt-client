plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
    id("biz.aQute.bnd.builder")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin")
    id("signing")
    id("com.github.hierynomus.license")
    id("pmd")
    id("io.github.sgtsilvio.gradle.defaults")
    id("io.github.sgtsilvio.gradle.metadata")
    id("io.github.sgtsilvio.gradle.javadoc-links")
}


/* ******************** metadata ******************** */

allprojects {
    group = "com.hivemq"
    description = "HiveMQ MQTT Client is an MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance " +
            "Java client library with different API flavours and backpressure support"

    plugins.apply("io.github.sgtsilvio.gradle.metadata")

    metadata {
        moduleName.set("com.hivemq.client2.mqtt")
        readableName.set("HiveMQ MQTT Client")
        organization {
            name.set("HiveMQ and the HiveMQ Community")
            url.set("https://www.hivemq.com/")
        }
        license {
            apache2()
        }
        developers {
            register("SgtSilvio") {
                fullName.set("Silvio Giebl")
                email.set("silvio.giebl@hivemq.com")
            }
        }
        github {
            org.set("hivemq")
            repo.set("hivemq-mqtt-client")
            pages()
            issues()
        }
    }
}


/* ******************** java ******************** */

allprojects {
    plugins.withId("java") {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }

        plugins.apply("io.github.sgtsilvio.gradle.defaults")
    }
}


/* ******************** dependencies ******************** */

dependencies {
    api("io.reactivex.rxjava3:rxjava:${property("rxjava.version")}")
    api("org.reactivestreams:reactive-streams:${property("reactive-streams.version")}")

    implementation("io.netty:netty-buffer:${property("netty.version")}")
    implementation("io.netty:netty-codec:${property("netty.version")}")
    implementation("io.netty:netty-common:${property("netty.version")}")
    implementation("io.netty:netty-handler:${property("netty.version")}")
    implementation("io.netty:netty-transport:${property("netty.version")}")
    implementation("org.jctools:jctools-core:${property("jctools.version")}")
    implementation("org.jetbrains:annotations:${property("annotations.version")}")
    implementation("com.google.dagger:dagger:${property("dagger.version")}")

    compileOnly("org.slf4j:slf4j-api:${property("slf4j.version")}")

    annotationProcessor("com.google.dagger:dagger-compiler:${property("dagger.version")}")
}


/* ******************** optional dependencies ******************** */

for (feature in listOf("websocket", "proxy", "epoll")) {
    java.registerFeature(feature) {
        usingSourceSet(sourceSets.main.get())
    }
}

dependencies {
    "websocketImplementation"("io.netty:netty-codec-http:${property("netty.version")}")
    "proxyImplementation"("io.netty:netty-handler-proxy:${property("netty.version")}")
    "epollImplementation"("io.netty:netty-transport-native-epoll:${property("netty.version")}:linux-x86_64")
}


/* ******************** test ******************** */

allprojects {
    plugins.withId("java") {
        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit-jupiter.version")}")
            testImplementation("org.junit.jupiter:junit-jupiter-params")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        }

        tasks.test {
            useJUnitPlatform()
            maxHeapSize = "1g"
            maxParallelForks = 1.coerceAtLeast(Runtime.getRuntime().availableProcessors() / 2)
            jvmArgs("-XX:+UseParallelGC")
        }
    }
}

dependencies {
    testImplementation("nl.jqno.equalsverifier:equalsverifier:${property("equalsverifier.version")}")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testImplementation("com.google.guava:guava:${property("guava.version")}")
    testImplementation("org.bouncycastle:bcprov-jdk15on:${property("bouncycastle.version")}")
    testImplementation("org.bouncycastle:bcpkix-jdk15on:${property("bouncycastle.version")}")
    testImplementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:${property("paho.version")}")
    testRuntimeOnly("org.slf4j:slf4j-simple:${property("slf4j.version")}")
}


/* ******************** jars ******************** */

allprojects {
    plugins.withId("java-library") {

        plugins.apply("biz.aQute.bnd.builder")

        tasks.jar {
            withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
                bnd("-consumer-policy: \${range;[==,=+)}", "-removeheaders: Private-Package")
            }
        }

        java {
            withJavadocJar()
            withSourcesJar()
        }

        plugins.apply("io.github.sgtsilvio.gradle.javadoc-links")

        tasks.javadoc {
            exclude("**/internal/**")
        }
    }
}

tasks.jar {
    withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
        bnd("Export-Package: " +
                "com.hivemq.client2.annotations.*," +
                "com.hivemq.client2.mqtt.*," +
                "com.hivemq.client2.rx.*," +
                "com.hivemq.client2.util.*")
    }
}

tasks.shadowJar {
    archiveAppendix.set("shaded")
    archiveClassifier.set("")

    configurations = listOf(project.configurations.create("shaded") {
        extendsFrom(project.configurations["runtimeClasspath"])
        for (apiDependency in project.configurations["apiElements"].allDependencies) {
            exclude(apiDependency.group, apiDependency.name)
        }
    })

    val shadePrefix = "com.hivemq.client2.internal.shaded."
    val shadeFilePrefix = shadePrefix.replace(".", "_")
    relocate("io.netty", "${shadePrefix}io.netty")
    relocate("META-INF/native/libnetty", "META-INF/native/lib${shadeFilePrefix}netty")
    exclude("META-INF/io.netty.versions.properties")
    relocate("org.jctools", "${shadePrefix}org.jctools")
    relocate("org.jetbrains", "${shadePrefix}org.jetbrains")
    relocate("dagger", "${shadePrefix}dagger")
    relocate("javax.inject", "${shadePrefix}javax.inject")

    minimize()
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) {
    skip()
}


/* ******************** publishing ******************** */

allprojects {
    plugins.withId("java-library") {

        plugins.apply("maven-publish")

        publishing.publications.register<MavenPublication>("base") {
            from(components["java"])
            suppressAllPomMetadataWarnings()
        }
    }

    plugins.withId("java-platform") {

        plugins.apply("maven-publish")

        publishing.publications.register<MavenPublication>("base") {
            from(components["javaPlatform"])
            suppressAllPomMetadataWarnings()
        }
    }
}

publishing.publications.register<MavenPublication>("shaded") {
    artifactId = "${project.name}-shaded"
    artifact(tasks.shadowJar)
    artifact(tasks.named("javadocJar"))
    artifact(tasks.named("sourcesJar"))
    pom.withXml {
        asNode().appendNode("dependencies").apply {
            for (apiDependency in configurations["apiElements"].allDependencies) {
                appendNode("dependency").apply {
                    appendNode("groupId", apiDependency.group)
                    appendNode("artifactId", apiDependency.name)
                    appendNode("version", apiDependency.version)
                    appendNode("scope", "compile")
                }
            }
        }
    }
}

allprojects {
    plugins.withId("maven-publish") {
        publishing.publications.withType<MavenPublication>().configureEach {
            pom.withXml {
                val dependencies = (asNode()["dependencies"] as groovy.util.NodeList)[0] as groovy.util.Node
                for (dependency in dependencies.children()) {
                    dependency as groovy.util.Node
                    val optional = dependency["optional"] as groovy.util.NodeList
                    if (!optional.isEmpty() && (optional[0] as groovy.util.Node).text() == "true") {
                        ((dependency["scope"] as groovy.util.NodeList)[0] as groovy.util.Node).setValue("runtime")
                    }
                }
            }
        }
    }
}

allprojects {
    plugins.withId("maven-publish") {

        plugins.apply("signing")

        signing {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
            publishing.publications.configureEach {
                sign(this)
            }
        }
    }
}

nexusPublishing {
    this.repositories {
        sonatype()
    }
}

/* ******************** checks ******************** */

allprojects {
    plugins.apply("com.github.hierynomus.license")

    license {
        header = rootDir.resolve("HEADER")
        mapping("java", "SLASHSTAR_STYLE")
    }
}

allprojects {
    plugins.withId("java") {

        plugins.apply("pmd")

        pmd {
            toolVersion = "5.7.0"
            incrementalAnalysis.set(false)
        }
    }
}

apply("$rootDir/gradle/japicc.gradle.kts")


/* ******************** build cache ******************** */

allprojects {
    normalization {
        runtimeClasspath {
            ignore("META-INF/MANIFEST.MF")
        }
    }
}
