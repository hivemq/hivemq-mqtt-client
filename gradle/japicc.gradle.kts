import java.net.URL
import java.nio.charset.StandardCharsets

val japiccDownload = tasks.register("japiccDownload") {
    group = "japicc"
    description = "Downloads the Java API Compliance Checker"

    val japiccVersion = "2.4"
    val workingDir = buildDir.resolve("japicc")
    val archive = workingDir.resolve("japi-compliance-checker-$japiccVersion.zip")
    val bin by extra(workingDir.resolve("japi-compliance-checker-$japiccVersion"))

    inputs.property("type", name)
    inputs.property("japiccVersion", japiccVersion)
    outputs.dir(bin)
    outputs.cacheIf { true }

    doLast {
        archive.delete()
        bin.delete()
        val url = "https://github.com/lvc/japi-compliance-checker/archive/$japiccVersion.zip"
        URL(url).openStream().use { input -> archive.outputStream().use { output -> input.copyTo(output) } }
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

                val workingDir = buildDir.resolve("japicc")
                val nonImplFile by extra(workingDir.resolve("non-impl"))
                val sourceSet by extra(project.the<JavaPluginConvention>().sourceSets["main"].java.filterNot {
                    it.path.matches(Regex(".*/internal/.*"))
                })

                inputs.property("type", name)
                inputs.files(sourceSet)
                outputs.file(nonImplFile)
                outputs.cacheIf { true }

                doLast {
                    nonImplFile.delete()
                    nonImplFile.parentFile.mkdirs()
                    nonImplFile.createNewFile()
                    val writer = nonImplFile.bufferedWriter(StandardCharsets.UTF_8)

                    sourceSet.forEach {
                        if (!it.isFile) return@forEach

                        val content = it.readText(StandardCharsets.UTF_8)
                                .replace(Regex("//.*\n"), " ") // remove line comments
                                .replace("\n", " ") // remove new lines
                                .replace(Regex("/\\*.*?\\*/"), " ") // remove multi line comments
                                .replace(Regex(" +"), " ") // remove unnecessary spaces

                        val packageStart = content.indexOf("package ") + "package ".length
                        val packageEnd = content.indexOf(";", packageStart)
                        val packageName = content.substring(packageStart, packageEnd)

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

                            val annotationIndex = content.indexOf("@ApiStatus.NonExtendable", index)
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

                    val workingDir = buildDir.resolve("japicc")
                    val groupId = publication.groupId
                    val artifactId = publication.artifactId
                    val version = publication.version
                    val prevVersion: String by project.extra
                    val prevJarName = "$artifactId-$prevVersion.jar"
                    val prevJar by extra(workingDir.resolve(prevJarName))

                    inputs.property("type", name)
                    inputs.property("prevVersion", prevVersion)
                    outputs.file(prevJar)
                    outputs.cacheIf { true }

                    onlyIf { version != prevVersion }

                    doLast {
                        prevJar.delete()
                        val path = groupId.replace(".", "/") + "/$artifactId/$prevVersion/$prevJarName"
                        URL("${repositories.mavenCentral().url}$path").openStream()
                                .use { input -> prevJar.outputStream().use { output -> input.copyTo(output) } }
                    }
                }

                val japiccArtifact = tasks.register("japicc-${publication.artifactId}") {
                    group = "japicc"
                    description = "Runs binary and source incompatibility check for ${publication.artifactId}"

                    val workingDir = buildDir.resolve("japicc")
                    val artifactId = publication.artifactId
                    val version = publication.version
                    val prevVersion: String by project.extra
                    val jar = artifact.file
                    val prevJar: File by japiccDownloadArtifact.get().extra
                    val sourceSet: List<File> by japiccNonImpl.get().extra
                    val nonImplFile: File by japiccNonImpl.get().extra
                    val bin: File by japiccDownload.get().extra
                    val report = workingDir.resolve(
                            "compat_reports/$artifactId/${prevVersion}_to_$version/compat_report.html")

                    inputs.property("type", name)
                    dependsOn(artifact.buildDependencies.getDependencies(null))
                    inputs.files(sourceSet)
                    inputs.files(japiccNonImpl)
                    inputs.files(japiccDownload)
                    inputs.files(japiccDownloadArtifact)
                    outputs.file(report)
                    outputs.cacheIf { true }

                    onlyIf { prevJar.exists() }

                    doLast {
                        val command = listOf(
                                "perl", bin.resolve("japi-compliance-checker.pl").path,
                                "-lib", artifactId,
                                "-skip-internal-packages", "com.hivemq.client2.internal",
                                "-non-impl", nonImplFile.path,
                                "-check-annotations", "-s",
                                prevJar.path, jar.path)

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
                the<PublishingExtension>().publications.withType<MavenPublication>().configureEach {
                    val artifact = artifacts.find { (it.extension == "jar") && (it.classifier == null) }
                    if (artifact != null) {
                        addArtifact(this, artifact)
                    }
                }
            }
        }
    }
}
