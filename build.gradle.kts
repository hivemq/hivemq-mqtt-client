plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.bnd)
    `maven-publish`
    signing
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.defaults)
    alias(libs.plugins.metadata)
    alias(libs.plugins.javadocLinks)
    alias(libs.plugins.license)
    pmd
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
    api(libs.rxjava)
    api(libs.reactiveStreams)
    compileOnlyApi(libs.jetbrains.annotations)
    implementation(libs.netty.buffer)
    implementation(libs.netty.codec)
    implementation(libs.netty.common)
    implementation(libs.netty.handler)
    implementation(libs.netty.transport)
    implementation(libs.jctools)
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    compileOnly(libs.slf4j.api)
}


/* ******************** optional dependencies ******************** */

for (feature in listOf("websocket", "proxy", "epoll")) {
    java.registerFeature(feature) {
        usingSourceSet(sourceSets.main.get())
    }
}

dependencies {
    "websocketImplementation"(libs.netty.codec.http)
    "proxyImplementation"(libs.netty.handler.proxy)
    "epollImplementation"(variantOf(libs.netty.transport.native.epoll) { classifier("linux-x86_64") })
}


/* ******************** test ******************** */

allprojects {
    plugins.withId("java") {
        dependencies {
            testImplementation(libs.junit.jupiter)
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
    testImplementation(libs.mockito)
    testImplementation(libs.equalsVerifier)
    testImplementation(libs.jetbrains.annotations)
    testImplementation(libs.guava)
    testImplementation(libs.paho.mqttClient)
    testRuntimeOnly(libs.slf4j.simple)
}


/* ******************** jars ******************** */

allprojects {
    plugins.withId("java-library") {

        plugins.apply("biz.aQute.bnd.builder")

        tasks.jar {
            configure<aQute.bnd.gradle.BundleTaskExtension> {
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
    configure<aQute.bnd.gradle.BundleTaskExtension> {
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
