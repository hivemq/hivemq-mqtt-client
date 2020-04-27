import java.util.*

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
    id("biz.aQute.bnd.builder")
    id("com.github.hierynomus.license")
    id("maven-publish")
    id("com.jfrog.bintray")
    id("com.github.breadmoirai.github-release")
    id("pmd")
}


/* ******************** metadata ******************** */

allprojects {
    group = "com.hivemq"
    version = "1.2.0"

    description = "HiveMQ MQTT Client is a MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance Java " +
            "client library with different API flavours and backpressure support"

    extra["moduleName"] = "com.hivemq.client.mqtt"
    extra["readableName"] = "HiveMQ MQTT Client"
    extra["vendor"] = "HiveMQ and the HiveMQ Community"
    extra["githubOrg"] = "hivemq"
    extra["githubRepo"] = "hivemq-mqtt-client"
    extra["githubUrl"] = "https://github.com/${extra["githubOrg"]}/${extra["githubRepo"]}"
    extra["scmConnection"] = "scm:git:git://github.com/${extra["githubOrg"]}/${extra["githubRepo"]}.git"
    extra["scmDeveloperConnection"] = "scm:git:ssh://git@github.com/${extra["githubOrg"]}/${extra["githubRepo"]}.git"
    extra["issuesUrl"] = "${extra["githubUrl"]}/issues"
    extra["docUrl"] = "https://${extra["githubOrg"]}.github.io/${extra["githubRepo"]}/"
    extra["licenseShortName"] = "Apache-2.0"
    extra["licenseReadableName"] = "The Apache License, Version 2.0"
    extra["licenseUrl"] = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    extra["prevVersion"] = "1.1.4"
}


/* ******************** java ******************** */

allprojects {
    plugins.withType<JavaPlugin> {
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        tasks.withType<Javadoc> {
            options.encoding = "UTF-8"
            exclude("**/internal/**")
        }
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

    implementation("io.netty:netty-buffer:${property("netty.version")}")
    implementation("io.netty:netty-codec:${property("netty.version")}")
    implementation("io.netty:netty-common:${property("netty.version")}")
    implementation("io.netty:netty-handler:${property("netty.version")}")
    implementation("io.netty:netty-transport:${property("netty.version")}")
    implementation("org.jctools:jctools-core:${property("jctools.version")}")
    implementation("org.jetbrains:annotations:${property("jetbrains-annotations.version")}")
    implementation("com.google.dagger:dagger:${property("dagger.version")}")

    compileOnly("org.slf4j:slf4j-api:${property("slf4j.version")}")

    annotationProcessor("com.google.dagger:dagger-compiler:${property("dagger.version")}")
}


/* ******************** optional dependencies ******************** */

val features = listOf("websocket", "proxy", "epoll")
features.forEach { feature ->
    java.registerFeature(feature) {
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
    plugins.withType<JavaPlugin> {
        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit.jupiter.version")}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junit.jupiter.version")}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit.jupiter.version")}")
        }

        tasks.test {
            useJUnitPlatform()
            maxHeapSize = "4096m"
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
    plugins.withType<JavaLibraryPlugin> {

        project.apply(plugin = "biz.aQute.bnd.builder")

        afterEvaluate {
            tasks.jar {
                manifest {
                    attributes["Automatic-Module-Name"] = project.extra["moduleName"]
                    attributes["Bundle-Name"] = project.name
                    attributes["Bundle-SymbolicName"] = project.extra["moduleName"]
                    attributes["Bundle-Description"] = project.description
                    attributes["Bundle-Vendor"] = project.extra["vendor"]
                    attributes["Bundle-License"] = "${project.extra["licenseShortName"]}" +
                            ";description=\"${project.extra["licenseReadableName"]}\"" +
                            ";link=\"${project.extra["licenseUrl"]}\""
                    attributes["Bundle-DocURL"] = project.extra["docUrl"]
                    attributes["Bundle-SCM"] = "url=\"${project.extra["githubUrl"]}\"" +
                            ";connection=\"${project.extra["scmConnection"]}\"" +
                            ";developerConnection=\"${project.extra["scmDeveloperConnection"]}\""
                    attributes["-consumer-policy"] = "\${range;[==,=+)}"
                    attributes["-removeheaders"] = "Private-Package"
                }
            }
        }

        java {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

tasks.jar {
    manifest.attributes["Export-Package"] = "com.hivemq.client.annotations.*, com.hivemq.client.mqtt.*, com.hivemq.client.rx.*, com.hivemq.client.util.*"
}

tasks.shadowJar {
    archiveAppendix.set("shaded")
    archiveClassifier.set("")

    // api: not shaded and relocated, added as dependencies in pom
    dependencies {
        exclude(dependency("io.reactivex.rxjava2:rxjava"))
        exclude(dependency("org.reactivestreams:reactive-streams"))
        exclude(dependency("org.slf4j:slf4j-api"))
    }

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

apply(from = "${project.rootDir}/gradle/publishing.gradle")

allprojects {
    plugins.withType<JavaLibraryPlugin> {

        project.apply(plugin = "maven-publish")

        publishing.publications.create<MavenPublication>("base") {
            from(components["java"])
            suppressAllPomMetadataWarnings()
        }

        afterEvaluate {
            publishing.publications.withType<MavenPublication> {
                pom {
                    name.set("${project.extra["readableName"]}")
                    description.set(project.description)
                    url.set("${project.extra["githubUrl"]}")
                    licenses {
                        license {
                            name.set("${project.extra["licenseReadableName"]}")
                            url.set("${project.extra["licenseUrl"]}")
                        }
                    }
                    developers {
                        developer {
                            id.set("SG")
                            name.set("Silvio Giebl")
                            email.set("silvio.giebl@hivemq.com")
                        }
                    }
                    scm {
                        connection.set("${project.extra["scmConnection"]}")
                        developerConnection.set("${project.extra["scmDeveloperConnection"]}")
                        url.set("${project.extra["githubUrl"]}")
                    }
                    issueManagement {
                        system.set("github")
                        url.set("${project.extra["issuesUrl"]}")
                    }
                    withXml {
                        (asNode().get("dependencies") as groovy.util.NodeList).forEach { dependencies ->
                            (dependencies as groovy.util.Node).children().forEach { dependency ->
                                val dep = dependency as groovy.util.Node
                                val optional = dep.get("optional") as groovy.util.NodeList
                                val scope = dep.get("scope") as groovy.util.NodeList
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
}

features.forEach { feature ->
    publishing.publications.create<MavenPublication>(feature) {
        artifactId = project.name + "-" + feature
        pom.withXml {
            asNode().appendNode("dependencies").apply {
                appendNode("dependency").apply {
                    appendNode("groupId", project.group)
                    appendNode("artifactId", project.name)
                    appendNode("version", project.version)
                    appendNode("scope", "compile")
                }
                configurations[feature + "RuntimeElements"].allDependencies.forEach {
                    appendNode("dependency").apply {
                        appendNode("groupId", it.group)
                        appendNode("artifactId", it.name)
                        appendNode("version", it.version)
                        appendNode("scope", "runtime")
                    }
                }
            }
        }
    }
}

publishing.publications.create<MavenPublication>("shaded") {
    artifactId = project.name + "-shaded"
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
    plugins.withType<JavaLibraryPlugin> {

        project.apply(plugin = "com.jfrog.bintray")

        bintray {
            user = "${rootProject.extra["bintray_username"]}"
            key = "${rootProject.extra["bintray_apiKey"]}"
            publish = true
            with(pkg) {
                userOrg = "hivemq"
                repo = "HiveMQ"
                name = "hivemq-mqtt-client"
                desc = project.description
                websiteUrl = "${project.extra["githubUrl"]}"
                issueTrackerUrl = "${project.extra["issuesUrl"]}"
                vcsUrl = "${project.extra["githubUrl"]}.git"
                setLicenses("${project.extra["licenseShortName"]}")
                setLabels("mqtt", "mqtt-client", "iot", "internet-of-things", "rxjava2", "reactive-streams", "backpressure")
                with(version) {
                    released = Date().toString()
                    vcsTag = "v${project.version}"
                    with(gpg) {
                        sign = true
                    }
                }
            }
        }
        afterEvaluate {
            bintray.setPublications(*publishing.publications.withType<MavenPublication>().names.toTypedArray())
        }

        // workaround for publishing gradle metadata https://github.com/bintray/gradle-bintray-plugin/issues/229
        tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
            doFirst {
                publishing.publications.withType<MavenPublication>().forEach { publication ->
                    val moduleFile = File(File(File(project.buildDir, "publications"), publication.name), "module.json")
                    if (moduleFile.exists()) {
                        publication.artifact(moduleFile).extension = "module"
                    }
                }
            }
        }
    }
}

githubRelease {
    token("${rootProject.extra["github_token"]}")
    owner.set("${project.extra["githubOrg"]}")
    repo.set("${project.extra["githubRepo"]}")
    targetCommitish.set("master")
    tagName.set("v${project.version}")
    releaseName.set("${project.version}")
}


/* ******************** checks ******************** */

allprojects {
    apply(plugin = "com.github.hierynomus.license")

    license {
        header = File(project.rootDir, "HEADER")
        mapping("java", "SLASHSTAR_STYLE")
    }
}

allprojects {
    plugins.withType<JavaPlugin> {
        apply(plugin = "pmd")

        pmd {
            toolVersion = "5.7.0"
        }
    }
}

allprojects {
    plugins.withType<JavaLibraryPlugin> {
        project.apply(from = "${project.rootDir}/gradle/japicc.gradle")
    }
}
