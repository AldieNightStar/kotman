package haxidenti.kotman

import java.io.File

const val DEFAULT_AUTHOR = "haxidenti"
const val APP_MOD_NAME = "app"
const val FILE_BUILD_GRADLE = "build.gradle.kts"

object Project {
    fun createProject(workdir: File, name: String) {
        val projectDir = workdir.resolve(name).also { it.mkdirs() }
        if (!runGradleInit(projectDir, name)) throw RuntimeException("Failed to initialize project")
        addGradleModule(projectDir, "core", name)
        setupDistribution(projectDir)
    }

    fun runGradleInit(dir: File, name: String): Boolean {
        return Sys.runShell(dir,listOf(
            "gradle",
            "init",
            "--type", "kotlin-application",
            "--dsl", "kotlin",
            "--project-name", name,
            "--package", "$DEFAULT_AUTHOR.$name",
            "--test-framework", "junit-jupiter",
            "--java-version", "24",
            "--no-split-project",
            "--no-incubating",
            "--no-daemon",
            "--console=plain",
        ))
    }

    fun addGradleModule(dir: File, name: String, projectName: String): Boolean {
        val modDir = dir.resolve(name).also { it.mkdirs() }
        val settings = dir.resolve("settings.gradle.kts")

        // Validate it's correct
        if (!settings.isFile) return false

        // Basic dirs
        val packagePath = "$DEFAULT_AUTHOR/$projectName"
        modDir.resolve("src/main/kotlin/$packagePath").also { it.mkdirs() }
        modDir.resolve("src/main/java/$packagePath").also { it.mkdirs() }
        modDir.resolve("src/main/resources/$packagePath").also { it.mkdirs() }

        // Test dirs
        modDir.resolve("src/test/kotlin/$packagePath").also { it.mkdirs() }
        modDir.resolve("src/test/java/$packagePath").also { it.mkdirs() }

        // Build file
        modDir.resolve(FILE_BUILD_GRADLE).writeText(generateBuildFile(name))

        // Append include
        settings.appendText("include(\"$name\")\n")

        return true
    }

    fun generateBuildFile(name: String): String {
        return """
            plugins {
                alias(libs.plugins.kotlin.jvm)
                `maven-publish`
                java
            }


            // Reference string
            val REFERENCE = "$DEFAULT_AUTHOR:$name:1.0.0".split(":")
            
            // Parsing the reference
            val GROUP = REFERENCE[0]
            val ARTIFACT = REFERENCE[1]
            val VERSION = REFERENCE[2]

            repositories {
                mavenLocal()
                mavenCentral()
            }

            dependencies {
                testImplementation(kotlin("test"))
            }

            kotlin {
                jvmToolchain(24)
            }

            tasks.test {
                useJUnitPlatform()
            }

            publishing {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                        groupId = GROUP
                        artifactId = ARTIFACT
                        version = VERSION
                    }
                }
            }

            java {
                withSourcesJar()
            }

            group = GROUP
            version = VERSION
        """.trimIndent()
    }

    fun setupDistribution(dir: File) {
        val modDir = dir.resolve(APP_MOD_NAME)
        if (!modDir.isDirectory) throw RuntimeException("$APP_MOD_NAME is not a directory")

        // Create data directory
        val dataDir = modDir.resolve("data").also { it.mkdirs() }

        // Some file to keep the structure
        dataDir.resolve(".gitkeep").writeText("<3")

        // Add some text to build gradle
        val buildFile = modDir.resolve(FILE_BUILD_GRADLE)
        buildFile.appendText("""
            
            distributions {
                main {
                    contents { from("data") }
                }
            }
            
        """.trimIndent())
    }
}