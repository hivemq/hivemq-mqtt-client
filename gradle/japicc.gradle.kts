import java.net.URL
import java.nio.charset.StandardCharsets

tasks.register("japicc") {
    group = "verification"

    tasks["check"].dependsOn(this)
}

if (rootProject.tasks.findByName("japiccDownload") == null) {
    rootProject.tasks.register("japiccDownload") {
        group = "japicc"
        description = "Download Java API Compliance Checker"

        val japiccVersion = "2.4"
        val workingDir = File(rootProject.buildDir, "japicc")
        val archive = File(workingDir, "japi-compliance-checker-$japiccVersion.zip")
        val executable = File(workingDir, "japi-compliance-checker-$japiccVersion/japi-compliance-checker.pl")
        extra["executable"] = executable

        outputs.files(executable)

        doLast {
            if (!archive.exists()) {
                archive.parentFile.mkdirs()
                val url = "https://github.com/lvc/japi-compliance-checker/archive/$japiccVersion.zip"
                URL(url).openStream().copyTo(archive.outputStream())
            }
            copy {
                from(zipTree(archive))
                into(workingDir)
            }
        }
    }
}

tasks.register("japiccNonImpl") {
    group = "japicc"
    description = "List non impl interfaces"

    val workingDir = File(project.buildDir, "japicc")
    val nonImplFile = File(workingDir, "non-impl")
    val sourceSet = project.the<JavaPluginConvention>().sourceSets["main"].java
    extra["nonImplFile"] = nonImplFile

    inputs.files(sourceSet)
    outputs.files(nonImplFile)

    doLast {
        nonImplFile.delete()
        nonImplFile.parentFile.mkdirs()
        nonImplFile.createNewFile()
        val writer = nonImplFile.bufferedWriter(StandardCharsets.UTF_8)

        sourceSet.visit {
            if (file.isFile) {
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
        }
        writer.close()
    }
}

fun addCheck(jarTaskProvider: TaskProvider<Jar>) {
    tasks.register("japiccCheck${jarTaskProvider.name.capitalize()}") {
        group = "japicc"
        description = "Checks for binary and source incompatibility."

        dependsOn(rootProject.tasks["japiccDownload"], tasks["japiccNonImpl"])
        tasks["japicc"].dependsOn(this)

        val jarTask = jarTaskProvider.get()

        val workingDir = File(project.buildDir, "japicc")
        val archiveName = jarTask.archiveBaseName.get() + jarTask.archiveAppendix.map { "-$it" }.getOrElse("")
        val prevVersion = project.extra["prevVersion"] as String
        val prevJarName = "$archiveName-$prevVersion.jar"
        val prevJar = File(workingDir, prevJarName)

        val report = File(File(File(File(workingDir, "compat_reports"),
                archiveName), "${prevVersion}_to_${project.version}"), "compat_report.html")

        inputs.files(jarTask)
        outputs.files(report)

        doLast {
            println("Checking if previous version is available")

            if (project.version == prevVersion) {
                throw StopExecutionException("No last semantic version available")
            }

            println("Downloading previous version")

            if (!prevJar.exists()) {
                prevJar.parentFile.mkdirs()
                val path = project.group.toString().replace(".", "/") + "/$archiveName/$prevVersion/$prevJarName"
                URL("${repositories.mavenCentral().url}$path").openStream().copyTo(prevJar.outputStream())
            }

            println("Checking binary and source compatibility with previous version")

            val executable = rootProject.tasks["japiccDownload"].extra["executable"] as File
            val nonImplFile = rootProject.tasks["japiccNonImpl"].extra["nonImplFile"] as File
            val command = listOf(
                    "perl", executable.path, "-lib", archiveName,
                    "-skip-internal-packages", "com.hivemq.client.internal",
                    "-skip-internal-packages", "com.hivemq.shaded",
                    "-skip-internal-types", "com.hivemq.client.mqtt.mqtt(5|3).Mqtt(5|3)(Rx|Async|Blocking)Client",
                    "-non-impl", nonImplFile.path,
                    "-check-annotations", "-s",
                    prevJar.path, jarTask.archiveFile.get().asFile.path)

            val process = ProcessBuilder(command).directory(workingDir).start()
            val returnCode = process.waitFor()
            if (returnCode != 0) {
                throw GradleException("Binary or source incompatibilities, code $returnCode")
            }
        }
    }
}

afterEvaluate {
    addCheck(tasks.named<Jar>("jar"))
    if (plugins.hasPlugin("com.github.johnrengelman.shadow")) {
        addCheck(tasks.named<Jar>("shadowJar"))
    }
}
