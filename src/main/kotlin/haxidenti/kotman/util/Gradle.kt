package haxidenti.kotman.util

import haxidenti.kotman.Sys.isWindows
import haxidenti.kotman.Sys.runShell
import haxidenti.kotman.dto.ProjectDetails
import haxidenti.kotman.util.GradleUtil.genDependenciesSection
import haxidenti.kotman.util.GradleUtil.genPluginsSection
import java.io.File
import java.nio.charset.Charset

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
            val CORO_VER = "${details.kotlinCoroutineVer}"
            
            val AUTHOR = "${details.author}"
            val PROJECT_NAME = "${details.projectName}"
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
                mavenLocal()
                mavenCentral()
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
            
            """.trimIndent()
        )

        // Add task for CLI
        lines.add(cliTask())

        lines.add(
            """
            
            java {
                withSourcesJar()
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

    private fun cliTask(): String {
        val loader = this.javaClass.classLoader
        return loader.getResourceAsStream("cli_task.kts")!!.readAllBytes().toString(Charset.defaultCharset())
    }
}