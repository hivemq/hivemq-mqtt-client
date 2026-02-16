plugins {
    `java-library`
    `maven-publish`
    signing
    pmd
    alias(libs.plugins.bnd)
    alias(libs.plugins.javadocLinks)
    alias(libs.plugins.license)
    alias(libs.plugins.mavenCentralPublishing)
    alias(libs.plugins.metadata)
    alias(libs.plugins.oci)
    alias(libs.plugins.shadow)
    alias(libs.plugins.utf8)
}

/* ******************** metadata ******************** */

allprojects {
    group = "com.hivemq"
    description = "HiveMQ MQTT Client is an MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance " +
            "Java client library with different API flavours and backpressure support"

    plugins.apply("io.github.sgtsilvio.gradle.metadata")
    metadata {
        moduleName.set("com.hivemq.client.mqtt")
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
                languageVersion = JavaLanguageVersion.of(21)
            }
        }
        tasks.compileJava {
            javaCompiler = javaToolchains.compilerFor {
                languageVersion = JavaLanguageVersion.of(8)
            }
        }
        plugins.apply("com.github.sgtsilvio.gradle.utf8")
    }
}

/* ******************** dependencies ******************** */

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    api(libs.rxjava)
    api(libs.reactiveStreams)

    implementation(libs.netty.buffer)
    implementation(libs.netty.codec)
    implementation(libs.netty.common)
    implementation(libs.netty.handler)
    implementation(libs.netty.transport)
    implementation(libs.jctools)
    implementation(libs.jetbrains.annotations)
    implementation(libs.dagger)

    compileOnly(libs.slf4j.api)

    annotationProcessor(libs.dagger.compiler)
}

/* ******************** optional dependencies ******************** */

for (feature in listOf("websocket", "proxy", "epoll")) {
    java.registerFeature(feature) {
        usingSourceSet(sourceSets["main"])
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
            testImplementation(platform(libs.junit.bom))
            testImplementation(libs.junit.jupiter)
            testRuntimeOnly(libs.junit.platform.launcher)
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
    testImplementation(libs.equalsverifier)
    testImplementation(libs.mockito)
    testImplementation(libs.guava)
    testImplementation(libs.bouncycastle.pkix)
    testImplementation(libs.bouncycastle.prov)
    testImplementation(libs.paho.client)
    testRuntimeOnly(libs.slf4j.simple)
}

/* ******************** integration Tests ******************** */

oci {
    registries {
        dockerHub {
            optionalCredentials()
        }
    }
}

sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    integrationTestImplementation(platform(libs.junit.bom))
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestRuntimeOnly(libs.junit.platform.launcher)
    integrationTestImplementation(libs.gradleOci.junitJupiter)
    integrationTestImplementation(libs.testcontainers)
    integrationTestImplementation(libs.testcontainers.hivemq)
    integrationTestImplementation(libs.testcontainers.junitJupiter)
    integrationTestImplementation(libs.hivemq.extensionSdk)
    integrationTestImplementation(libs.awaitility)
}

val integrationTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs integration tests."
    useJUnitPlatform()
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
}

oci.of(integrationTest) {
    imageDependencies {
        runtime("hivemq:hivemq-ce:latest") { isChanging = true }
    }
    val linuxAmd64 = platformSelector(platform("linux", "amd64"))
    val linuxArm64v8 = platformSelector(platform("linux", "arm64", "v8"))
    platformSelector = if (System.getenv("CI_RUN") != null) linuxAmd64 else linuxAmd64.and(linuxArm64v8)
}

tasks.check { dependsOn(integrationTest) }

/* ******************** jars ******************** */

allprojects {
    plugins.withId("java-library") {
        plugins.apply("biz.aQute.bnd.builder")
        tasks.jar {
            bundle {
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
    bundle {
        bnd(
            "Export-Package: " +
                    "com.hivemq.client.annotations.*," +
                    "com.hivemq.client.mqtt.*," +
                    "com.hivemq.client.rx.*," +
                    "com.hivemq.client.util.*"
        )
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

    val shadePrefix = "com.hivemq.client.internal.shaded."
    val shadeFilePrefix = shadePrefix.replace(".", "_")
    relocate("io.netty", "${shadePrefix}io.netty")
    relocate("META-INF/native/libnetty", "META-INF/native/lib${shadeFilePrefix}netty")
    exclude("META-INF/io.netty.versions.properties")
    relocate("org.jctools", "${shadePrefix}org.jctools")
    relocate("org.jetbrains", "${shadePrefix}org.jetbrains")
    relocate("dagger", "${shadePrefix}dagger")
    exclude("META-INF/com.google.dagger_dagger.version")
    relocate("javax.inject", "${shadePrefix}javax.inject")

    minimize()
}

shadow {
    addShadowVariantIntoJavaComponent = false
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
    artifact(tasks["shadowJar"])
    artifact(tasks["javadocJar"])
    artifact(tasks["sourcesJar"])
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

tasks.named("publishBasePublicationToMavenCentralStagingRepository") {
    mustRunAfter(tasks.named("signShadedPublication"))
}
tasks.named("publishShadedPublicationToMavenCentralStagingRepository") {
    mustRunAfter(tasks.named("signBasePublication"))
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
            toolVersion = libs.versions.pmd.get()
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
