package haxidenti.kotman.util

import haxidenti.kotman.Sys.isWindows
import haxidenti.kotman.Sys.runShell
import haxidenti.kotman.dto.ProjectDetails
import haxidenti.kotman.util.GradleUtil.genDependenciesSection
import haxidenti.kotman.util.GradleUtil.genPluginsSection
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
        lines.add(genPluginsSection(details.kotlinVer))

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
        lines.add(genDependenciesSection(details.additionalDependencies))
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

    private fun getMainClassName(packageName: String) = "$packageName.MainKt"
}