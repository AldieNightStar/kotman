package haxidenti.kotman

import haxidenti.kotman.Sys.isWindows
import haxidenti.kotman.Sys.runShell
import java.io.File

object Gradle {

    fun runGradle(workingFolder: File, gradleCommands: List<String>): Boolean {
        val gradleName = if (isWindows()) {
            "gradlew.bat"
        } else {
            "./gradlew"
        }
        return runShell(workingFolder, listOf(gradleName) + gradleCommands)
    }

    fun generateProjectSettings(projectName: String) = """
        rootProject.name = "$projectName"
    """.trimIndent()

    fun currentFolderHasGradle(projectFolder: File): Boolean {
        return (projectFolder.list() ?: return false)
            .count { it.startsWith("gradlew") } > 0
    }

    fun generateProjectGradle(details: ProjectDetails): String {
        val mainClass = getMainClassName(details.packageName)
        val lines = mutableListOf<String>()
        lines.add(
            """
            val MAIN_CLASS = "$mainClass"
            val PROJECT_NAME = "${details.projectName}"
            val AUTHOR = "${details.author}"
            val VERSION = "${details.version}"
            
        """.trimIndent()
        )

        // Add plugins
        lines.add(genPlugins(details.kotlinVer))

        lines.add(
            """
            
            group = AUTHOR
            version = VERSION
            
            repositories {
                mavenCentral()
                mavenLocal()
                maven { url = uri("https://jitpack.io") }
            }
            
            """.trimIndent()
        )
        lines.add(genDependencies(details.additionalDependencies))
        lines.add(
            """
            
            application {
                mainClass.set(MAIN_CLASS)
            }
        
            tasks.test {
                useJUnitPlatform()
            }
            
            publishing {
                publications {
                    create<MavenPublication>("maven") {
                        groupId = AUTHOR
                        artifactId = PROJECT_NAME
                        version = VERSION
                        
                        from(components["java"])
                    }
                }
            }
        """.trimIndent()
        )

        return lines.joinToString("\n")
    }

    private fun genPlugins(kotlinVer: String) = """
        plugins {
            kotlin("jvm") version "$kotlinVer"
            id("com.github.johnrengelman.shadow") version "8.1.1"
            id("application")
            `maven-publish`
        }
    """.trimIndent()

    private fun genDependencies(additional: List<String>): String {
        val deps = if (additional.isNotEmpty())
            ""
        else
            "\n    " + additional.joinToString("\n    ")

        // Generate
        val string = """
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")$deps
        }
        """.trimIndent()

        // Return result
        return string
    }

    private fun getMainClassName(packageName: String) = "$packageName.MainKt"

    fun readVals(src: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (line in src.lines()) {
            val trimmedLine = line.trim()
            if (!trimmedLine.startsWith("val ") && !trimmedLine.startsWith("var ")) continue
            val name = trimmedLine.substring(4).substringBefore(" ")
            val value = trimmedLine.substringAfter("\"").substringBefore("\"")
            map[name] = value
        }
        return map
    }
}