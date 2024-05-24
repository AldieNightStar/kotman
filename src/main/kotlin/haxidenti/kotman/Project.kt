package haxidenti.kotman

import java.io.File
import java.nio.file.Files

object Project {
    fun generate(details: ProjectDetails) {
        val projectDir = File(".").mkdirMust(details.projectName)

        val packagePath = details.packageName.replace(".", "/")

        projectDir.addFile("build.gradle.kts", Gradle.generateProjectGradle(details))
        projectDir.addFile("settings.gradle.kts", Gradle.generateProjectSettings(details.projectName))

        projectDir.addFile(".gitignore", gitIgnore())


        val mainClassFolderPath = projectDir.mkdirMust("src/main/kotlin/$packagePath")
        projectDir.mkdirMust("src/test/kotlin/$packagePath")

        // Write Main.kt file (Main class)
        val mainKtFile = mainClassFolderPath.resolve("Main.kt")

        if (!mainKtFile.createNewFile()) {
            throw IllegalStateException("Can't create Main class. Something wrong")
        }

        mainKtFile.writeText("""
            package ${details.packageName};
            
            fun main() {
                println("Hello, Kotlin");
            }
        """.trimIndent())

    }

    fun generateCli(projectFolder: File) {
        val values = readProjectValues(projectFolder)
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

        // Generate jar for root projecrt
        val rootJar = projectFolder.resolve(projectJarName)

        // Generate script
        val rootScript = File(projectName)

        rootScript.delete()
        rootScript.writeText(generateScript(projectJarName, mainClasName))

        rootJar.delete()
        jar.copyTo(rootJar)
    }

    private fun readProjectValues(projectFolder: File): Map<String, String> {
        if (!Gradle.currentFolderHasGradle(projectFolder)) {
            throw IllegalStateException("This is not a Gradle project or gradle wrapper is not initialized yet")
        }
        return Gradle.readVals(projectFolder.gradleFileSrc())
    }

    private fun generateScript(jarName: String, mainClassName: String) = """ 
            #!/bin/bash
            SCRIPTPATH="${'$'}( cd -- "${'$'}(dirname "${'$'}0")" >/dev/null 2>&1 ; pwd -P )"
            java -cp "${'$'}SCRIPTPATH/$jarName" $mainClassName "${'$'}@" 
        """.trimIndent()

    private fun File.addFile(name: String, content: String) {
        val path = File(this, name).toPath()
        val bytes = content.toByteArray()
        try {
            Files.write(path, bytes)
        } catch (e: Exception) {
            throw IllegalStateException("Can't create file $name", e)
        }
    }

    private fun File.mkdirMust(path: String): File {
        val dir = File(this, path)
        if (!dir.mkdirs()) {
            throw IllegalStateException("Can't create folder $path")
        }
        return dir.canonicalFile
    }

    private fun File.gradleFileSrc(): String {
        val gradle = File(this, "build.gradle.kts")
        if (!gradle.isFile) throw IllegalStateException("$this has no build.gradle.kts file")
        return gradle.readText()
    }

    private fun gitIgnore() = """
        /.idea/
        /.gradle/
    """.trimIndent()
}