import java.net.URL
import java.nio.charset.StandardCharsets

val japiccDownload = tasks.register("japiccDownload") {
    group = "japicc"
    description = "Downloads the Java API Compliance Checker"

    val japiccVersion = "2.4"
    val workingDir = File(rootProject.buildDir, "japicc")
    val archive = File(workingDir, "japi-compliance-checker-$japiccVersion.zip")
    val bin by extra(File(workingDir, "japi-compliance-checker-$japiccVersion"))

    inputs.property("type", name)
    inputs.property("japiccVersion", japiccVersion)
    outputs.dir(bin)
    outputs.cacheIf { true }

    doLast {
        archive.delete()
        bin.delete()
        val url = "https://github.com/lvc/japi-compliance-checker/archive/$japiccVersion.zip"
        URL(url).openStream().copyTo(archive.outputStream())
        copy {
            from(zipTree(archive))
            into(workingDir)
        }
    }
}

allprojects {
    plugins.withId("java-library") {
        plugins.withId("maven-publish") {

            val japicc = tasks.register("japicc") {
                group = "verification"
                description = "Runs all binary and source incompatibility checks"
            }
            tasks.named("check") {
                dependsOn(japicc)
            }

            val japiccNonImpl = tasks.register("japiccNonImpl") {
                group = "japicc"
                description = "Lists interfaces that must not be implemented by library users"

                val workingDir = File(project.buildDir, "japicc")
                val nonImplFile by extra(File(workingDir, "non-impl"))
                val sourceSet = project.the<JavaPluginConvention>().sourceSets["main"].java

                inputs.property("type", name)
                inputs.files(sourceSet)
                outputs.file(nonImplFile)
                outputs.cacheIf { true }

                doLast {
                    nonImplFile.delete()
                    nonImplFile.parentFile.mkdirs()
                    nonImplFile.createNewFile()
                    val writer = nonImplFile.bufferedWriter(StandardCharsets.UTF_8)

                    sourceSet.visit {
                        if (!file.isFile) return@visit

                        val packageName = relativePath.parent.pathString.replace("/", ".").replace("\\", ".")

                        val content = file.readText(StandardCharsets.UTF_8)
                                .replace(Regex("//.*\n"), " ") // remove line comments
                                .replace("\n", " ") // remove new lines
                                .replace(Regex("/\\*.*?\\*/"), " ") // remove multi line comments
                                .replace(Regex(" +"), " ") // remove unnecessary spaces

                        var index = 0
                        val classNames = mutableListOf<String>()
                        while (true) {
                            var start = content.indexOf(" interface ", index)
                            if (start == -1) break

                            val sub = content.substring(0, start)
                            val level = sub.count { it == '{' } - sub.count { it == '}' }
                            while (level < classNames.size) {
                                classNames.removeAt(classNames.size - 1)
                            }

                            start += " interface ".length
                            val end = content.indexOf('{', start)
                            if (end == -1) break

                            val interfaceDef = content.substring(start, end)
                            val className = interfaceDef.split(Regex("[ <{]"), limit = 2)[0]
                            classNames.add(className)

                            val annotationIndex = content.indexOf("@DoNotImplement", index)
                            if (annotationIndex == -1) break

                            if (annotationIndex < start) {
                                var qualifiedName = packageName + "." + classNames.joinToString(".")

                                var rest = interfaceDef.substring(className.length).trim()
                                if (rest.startsWith("<")) {
                                    rest = rest.replace(Regex("extends [^ <,]+"), "") // remove all extends ...
                                    rest = rest.replace(Regex("@.*? "), "") // remove all annotations
                                    var generics = "<"
                                    var nesting = 0
                                    for (c in rest) {
                                        if (c == '<') {
                                            nesting++
                                        } else if (c == '>') {
                                            nesting--
                                        } else if (nesting == 1) {
                                            generics += c
                                        } else if (nesting == 0) {
                                            break
                                        }
                                    }
                                    generics += ">"
                                    generics = generics.replace(" ", "")
                                    qualifiedName += generics
                                }
                                writer.appendln(qualifiedName)
                            }
                            index = end + 1
                        }
                    }
                    writer.close()
                }
            }

            fun addArtifact(publication: MavenPublication, artifact: MavenArtifact) {

                val japiccDownloadArtifact = tasks.register("japiccDownload-${publication.artifactId}") {
                    group = "japicc"
                    description = "Downloads the previous version of ${publication.artifactId}"

                    val workingDir = File(project.buildDir, "japicc")
                    val groupId = publication.groupId
                    val artifactId = publication.artifactId
                    val version = publication.version
                    val prevVersion: String by project.extra
                    val prevJarName = "$artifactId-$prevVersion.jar"
                    val prevJar by extra(File(workingDir, prevJarName))

                    inputs.property("type", name)
                    inputs.property("prevVersion", prevVersion)
                    outputs.file(prevJar)
                    outputs.cacheIf { true }

                    onlyIf { version != prevVersion }

                    doLast {
                        prevJar.delete()
                        val path = groupId.replace(".", "/") + "/$artifactId/$prevVersion/$prevJarName"
                        URL("${repositories.mavenCentral().url}$path").openStream().copyTo(prevJar.outputStream())
                    }
                }

                val japiccArtifact = tasks.register("japicc-${publication.artifactId}") {
                    group = "japicc"
                    description = "Runs binary and source incompatibility check for ${publication.artifactId}"

                    val workingDir = File(project.buildDir, "japicc")
                    val artifactId = publication.artifactId
                    val version = publication.version
                    val prevVersion: String by project.extra
                    val prevJar: File by japiccDownloadArtifact.get().extra
                    val bin: File by japiccDownload.get().extra
                    val nonImplFile: File by japiccNonImpl.get().extra
                    val report = File(File(File(File(workingDir, "compat_reports"),
                            artifactId), "${prevVersion}_to_$version"), "compat_report.html")
                    val sourceSet = project.the<JavaPluginConvention>().sourceSets["main"].java

                    inputs.property("type", name)
                    dependsOn(artifact.buildDependencies.getDependencies(null))
                    inputs.files(sourceSet)
                    inputs.files(japiccDownload)
                    inputs.files(japiccNonImpl)
                    inputs.files(japiccDownloadArtifact)
                    outputs.file(report)
                    outputs.cacheIf { true }

                    onlyIf { prevJar.exists() }

                    doLast {
                        val command = listOf(
                                "perl", File(bin, "japi-compliance-checker.pl").path,
                                "-lib", artifactId,
                                "-skip-internal-packages", "com.hivemq.client.internal",
                                "-skip-internal-packages", "com.hivemq.shaded",
                                "-skip-internal-types", "com.hivemq.client.mqtt.mqtt(5|3).Mqtt(5|3)(Rx|Async|Blocking)Client",
                                "-non-impl", nonImplFile.path,
                                "-check-annotations", "-s",
                                prevJar.path, artifact.file.path)

                        val process = ProcessBuilder(command).directory(workingDir).start()
                        val returnCode = process.waitFor()
                        if (returnCode != 0) {
                            throw GradleException("Binary or source incompatibilities, code $returnCode")
                        }
                    }
                }
                japicc {
                    dependsOn(japiccArtifact)
                }
            }

            afterEvaluate {
                the<PublishingExtension>().publications.withType<MavenPublication>().forEach { publication ->
                    val artifact = publication.artifacts.find { it.extension == "jar" && it.classifier == null }
                    if (artifact != null) {
                        addArtifact(publication, artifact)
                    }
                }
            }
        }
    }
}
