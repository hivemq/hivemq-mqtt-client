import java.util.*

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
    id("biz.aQute.bnd.builder")
    id("maven-publish")
    id("com.jfrog.bintray")
    id("com.github.breadmoirai.github-release")
    id("com.github.hierynomus.license")
    id("pmd")
    id("com.github.sgtsilvio.gradle.utf8")
    id("com.github.sgtsilvio.gradle.metadata")
    id("com.github.sgtsilvio.gradle.javadoc-links")
}


/* ******************** metadata ******************** */

allprojects {
    group = "com.hivemq"
    description = "HiveMQ MQTT Client is an MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance " +
            "Java client library with different API flavours and backpressure support"

    plugins.apply("com.github.sgtsilvio.gradle.metadata")

    metadata {
        moduleName = "com.hivemq.client.mqtt"
        readableName = "HiveMQ MQTT Client"
        organization {
            name = "HiveMQ and the HiveMQ Community"
            url = "https://www.hivemq.com/"
        }
        license {
            apache2()
        }
        developers {
            developer {
                id = "SgtSilvio"
                name = "Silvio Giebl"
                email = "silvio.giebl@hivemq.com"
            }
        }
        github {
            org = "hivemq"
            repo = "hivemq-mqtt-client"
            pages()
            issues()
        }
    }
}


/* ******************** java ******************** */

allprojects {
    plugins.withId("java") {
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
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
    api("io.reactivex.rxjava2:rxjava:${property("rxjava.version")}")
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

listOf("websocket", "proxy", "epoll").forEach {
    java.registerFeature(it) {
        usingSourceSet(sourceSets["main"])
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
            testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junit-jupiter.version")}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit-jupiter.version")}")
        }

        tasks.test {
            useJUnitPlatform()
            maxHeapSize = "1g"
            maxParallelForks = Runtime.getRuntime().availableProcessors()
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

        plugins.apply("com.github.sgtsilvio.gradle.javadoc-links")

        tasks.javadoc {
            exclude("**/internal/**")
        }
    }
}

tasks.jar {
    withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
        bnd("Export-Package: " +
                "com.hivemq.client.annotations.*," +
                "com.hivemq.client.mqtt.*," +
                "com.hivemq.client.rx.*," +
                "com.hivemq.client.util.*")
    }
}

tasks.shadowJar {
    archiveAppendix.set("shaded")
    archiveClassifier.set("")

    configurations = listOf(project.run {
        configurations.create("shaded") {
            extendsFrom(configurations["runtimeClasspath"])
            configurations["apiElements"].allDependencies.forEach {
                exclude(it.group, it.name)
            }
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
    relocate("javax.inject", "${shadePrefix}javax.inject")

    minimize()
}


/* ******************** publishing ******************** */

apply("${rootDir}/gradle/publishing.gradle.kts")

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
            configurations["apiElements"].allDependencies.forEach {
                appendNode("dependency").apply {
                    appendNode("groupId", it.group)
                    appendNode("artifactId", it.name)
                    appendNode("version", it.version)
                    appendNode("scope", "compile")
                }
            }
        }
    }
}

allprojects {
    plugins.withId("maven-publish") {
        afterEvaluate {
            publishing.publications.withType<MavenPublication>().configureEach {
                pom.withXml {
                    (asNode()["dependencies"] as groovy.util.NodeList).forEach { dependencies ->
                        (dependencies as groovy.util.Node).children().forEach { dependency ->
                            val dep = dependency as groovy.util.Node
                            val optional = dep["optional"] as groovy.util.NodeList
                            val scope = dep["scope"] as groovy.util.NodeList
                            if (!optional.isEmpty() && (optional[0] as groovy.util.Node).text() == "true") {
                                (scope[0] as groovy.util.Node).setValue("runtime")
                            }
                        }
                    }
                }
            }
        }
    }
}

allprojects {
    plugins.withId("maven-publish") {

        plugins.apply("com.jfrog.bintray")

        bintray {
            user = "${rootProject.extra["bintray_username"]}"
            key = "${rootProject.extra["bintray_apiKey"]}"
            publish = true
            pkg.apply {
                userOrg = "hivemq"
                repo = "HiveMQ"
                name = "hivemq-mqtt-client"
                desc = project.description
                websiteUrl = metadata.url
                issueTrackerUrl = metadata.issueManagement.url
                vcsUrl = metadata.scm.url
                setLicenses(metadata.license.shortName)
                setLabels("mqtt", "mqtt-client", "iot", "internet-of-things", "rxjava2", "reactive-streams", "backpressure")
                version.apply {
                    released = Date().toString()
                    vcsTag = "v${project.version}"
                    gpg.apply {
                        sign = true
                    }
                }
            }
        }
        afterEvaluate {
            bintray.setPublications(*publishing.publications.withType<MavenPublication>().names.toTypedArray())
        }

        // workaround for publishing gradle module metadata https://github.com/bintray/gradle-bintray-plugin/issues/229
        tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
            doFirst {
                publishing.publications.withType<MavenPublication> {
                    val moduleFile = buildDir.resolve("publications/$name/module.json")
                    if (moduleFile.exists()) {
                        artifact(moduleFile).extension = "module"
                    }
                }
            }
        }
    }
}

githubRelease {
    token("${rootProject.extra["github_token"]}")
    owner.set(metadata.github.org)
    repo.set(metadata.github.repo)
    targetCommitish.set("master")
    tagName.set("v${project.version}")
    releaseName.set("${project.version}")
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
        }
    }
}

apply("${rootDir}/gradle/japicc.gradle.kts")


/* ******************** build cache ******************** */

allprojects {
    normalization {
        runtimeClasspath {
            ignore("META-INF/MANIFEST.MF")
        }
    }
}
