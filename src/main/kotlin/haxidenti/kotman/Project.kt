package haxidenti.kotman

import haxidenti.kotman.dto.ProjectDetails
import haxidenti.kotman.util.Gradle
import haxidenti.kotman.util.ProjectUtil.addFile
import haxidenti.kotman.util.ProjectUtil.generateScript
import haxidenti.kotman.util.ProjectUtil.gitIgnore
import haxidenti.kotman.util.ProjectUtil.gradleConfig
import haxidenti.kotman.util.ProjectUtil.mkdirMust
import haxidenti.kotman.util.createZip
import java.io.File

private val supportedExtensions = listOf("kt", "kts", "java")

object Project {
    fun generate(details: ProjectDetails) {
        val projectDir = File(".").mkdirMust(details.projectName)

        val packagePath = details.packageName.replace(".", "/")

        projectDir.addFile("build.gradle.kts", Gradle.generateProjectGradle(details))
        projectDir.addFile("settings.gradle.kts", Gradle.generateProjectSettings(details.projectName))
        projectDir.addFile("gradle/wrapper/gradle-wrapper.properties", Gradle.generateGradleWrapperConfig())

        projectDir.addFile(".gitignore", gitIgnore(details.projectName))

        val mainClassFolderPath = projectDir.mkdirMust("src/main/kotlin/$packagePath")
        projectDir.mkdirMust("src/main/resources/$packagePath")
        projectDir.mkdirMust("src/test/kotlin/$packagePath")

        // Create Java directories
        projectDir.mkdirMust("src/main/java/$packagePath")
        projectDir.mkdirMust("src/test/java/$packagePath")

        // Write Main.kt file (Main class)
        val mainKtFile = mainClassFolderPath.resolve("Main.kt")

        if (!mainKtFile.createNewFile()) {
            throw IllegalStateException("Can't create Main class. Something wrong")
        }

        mainKtFile.writeText(
            """
            package ${details.packageName};
            
            fun main() {
                println("Hello, Kotlin");
            }
        """.trimIndent()
        )

    }

    fun generateCli(projectFolder: File) {
        val values = projectFolder.gradleConfig().readValues()
        if (!Gradle.runGradle(File("."), listOf("shadowJar"))) {
            throw IllegalStateException("Can't run shadowJar. Something wrong")
        }

        // Read PROJECT_NAME from build.gradle.kts
        val projectName = values["PROJECT_NAME"]
            ?: throw IllegalStateException("Project gradle has no: val PROJECT_NAME")

        // Read MAIN_CLASS from build.gradle.kts
        val mainClasName = values["MAIN_CLASS"]
            ?: throw IllegalStateException("Project gradle has no: val MAIN_CLASS")

        // Form project Jar name
        val projectJarName = "$projectName.jar"

        // Find lib-all jars
        val libs = projectFolder.resolve("build/libs")
        val jar = libs.walk().firstOrNull { it.nameWithoutExtension.endsWith("-all") }
            ?: throw IllegalStateException("Can't find any built library for cli")

        // Generate jar for root project
        val rootJar = projectFolder.resolve(projectJarName)

        // Generate script
        val rootScript = File(projectName)

        rootScript.delete()
        rootScript.writeText(generateScript(projectJarName, mainClasName))

        rootJar.delete()
        jar.copyTo(rootJar)
    }

    fun changeVersion(projectFolder: File, newVersion: String) {
        val config = projectFolder.gradleConfig()
        val values = config.readValues()
        if (!values.containsKey("VERSION")) throw IllegalStateException("VERSION is absent to modify")
        values["VERSION"] = newVersion
        config.writeValues(values)
    }

    fun readDependencies(projectFolder: File, includeTesting: Boolean): List<String> {
        val config = projectFolder.gradleConfig()
        return config.readDependencies()
            // If include testing then no filter (all is "true") and if without testing then "!it.testing"
            .filter { if (includeTesting) true else !it.testing }
            .map { it.toString() }
    }

    fun addDependencies(projectFolder: File, deps: List<String>) {
        val config = projectFolder.gradleConfig()
        config.addDependencies(deps)
    }

    fun runGenerator(projectFolder: File) {
        val sources = projectFolder.resolve("src")
        if (!sources.isDirectory) throw IllegalStateException("Can't find src folder")
        val megabyte = 1024 * 1024
        for (file in sources.walkTopDown()) {
            if (!file.isFile) continue
            if (file.length() > megabyte) continue
            if (file.extension !in supportedExtensions) continue
            val commands = file.readLines()
                .map { it.trim() }
                .filter { it.startsWith("// generate:") }
                .map { it.substring(12).trim() }
                .filter { it.isNotBlank() }
            val parent = file.parentFile
            for (command in commands) {
                val ok = Sys.runCommand(parent, command)
                if (!ok) {
                    throw IllegalStateException("Can't run command \"$command\" :: Something is wrong. File ${file.name}")
                }
            }
        }
    }

    fun runDist(projectFolder: File) {
        val config = projectFolder.gradleConfig().readValues()
        val projectName = config["PROJECT_NAME"] ?: throw IllegalStateException("No PROJECT_NAME found in gradle")
        val projectVersion = config["VERSION"] ?: throw IllegalStateException("No VERSION found in gradle")

        val list = projectFolder.listFiles() ?: throw IllegalStateException("No files to distribute")
        val outZipName = "${projectName}_${projectVersion}.zip"

        if (list.count { it.extension == "jar" } < 1) {
            throw IllegalStateException("No jar files found in the root. Please use \"kotman cli\" to build")
        }

        val filteredList = list
            .asSequence()
            .filter { !it.name.startsWith(".") }
            .filter { it.name != "build" }
            .filter { it.name != "src" }
            .filter { it.name != "gradle" }
            .filter { !it.name.startsWith("gradle") }
            .filter { !it.name.endsWith(".gradle.kts") }
            .filter { it.name != outZipName }
            .toList()
            .also { if (it.isEmpty()) throw IllegalStateException("No files to distribute") }

        createZip(projectFolder, filteredList, File("$projectName.zip"))
    }
}