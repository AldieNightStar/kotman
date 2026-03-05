package haxidenti.kotman

import java.io.File

const val DEFAULT_AUTHOR = "haxidenti"

object Project {
    fun createProject(workdir: File, name: String) {
        val dir = workdir.resolve(name).also { it.mkdirs() }
        if (!runGradleInit(dir, name)) throw RuntimeException("Failed to initialize project")
        addGradleModule(dir, "core", name)
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
        modDir.resolve("build.gradle.kts").writeText(generateBuildFile(name))

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

            val REFERENCE = "$DEFAULT_AUTHOR:$name:1.0.0"

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
                        groupId = REFERENCE.split(":")[0]
                        artifactId = REFERENCE.split(":")[1]
                        version = REFERENCE.split(":")[2]
                    }
                }
            }

            java {
                withSourcesJar()
            }

            group = REFERENCE.split(":")[0]
            version = REFERENCE.split(":")[2]
        """.trimIndent()
    }
}