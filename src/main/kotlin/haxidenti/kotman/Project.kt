package haxidenti.kotman

import java.io.File

const val DEFAULT_AUTHOR = "haxidenti"
const val APP_MOD_NAME = "app"
const val FILE_BUILD_GRADLE = "build.gradle.kts"
const val DIST_NAME = "release"

object Project {
    fun createProject(workdir: File, name: String) {
        val projectDir = workdir.resolve(name).also { it.mkdirs() }
        if (!runGradleInit(projectDir, name)) throw RuntimeException("Failed to initialize project")
        addGradleModule(projectDir, "core", name)
        setupDistribution(projectDir)

        // Add git keeps
        addGitKeeps(projectDir)
    }

    fun runGradleInit(dir: File, name: String): Boolean {
        return Sys.runShell(
            dir, listOf(
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
            )
        )
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

        // Add git keeps
        addGitKeeps(modDir)

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
        modDir.resolve("data").also { it.mkdirs() }

        // Add some text to build gradle
        val buildFile = modDir.resolve(FILE_BUILD_GRADLE)
        buildFile.appendText(
            """
            
            distributions {
                main {
                    contents { from("data") }
                }
            }
            
        """.trimIndent()
        )
    }

    fun addGitKeeps(root: File) {
        if (!root.isDirectory) return
        root.walkBottomUp()
            .filter { it.isDirectory }
            .filter { it.list()?.isEmpty() ?: false }
            .forEach {
                it.resolve(".gitkeep").writeText("<3")
            }
    }

    fun makeDist(projDir: File, name: String) {
        if (!runGradle(projDir, "install"))
            Err.fail("Distribution failed. `gradle install` failed to run")

        val appModDir = projDir.resolve(APP_MOD_NAME)
        if (!appModDir.isDirectory)
            Err.fail("Can't be distributed. No such directory: $appModDir")

        val buildDir = appModDir.resolve("build/install/$APP_MOD_NAME")
        if (!buildDir.isDirectory) Err.notExist(buildDir)

        // Rename files
        renameAll(
            buildDir.resolve("bin"),
            APP_MOD_NAME to name,
            "$APP_MOD_NAME.bat" to "$name.bat"
        )

        // Create directory and remove previous if there was some
        val outDir = projDir.resolve(DIST_NAME)
        if (outDir.isDirectory) outDir.deleteRecursively()
        outDir.mkdirs()

        // Copy everything to output
        if (!buildDir.copyRecursively(outDir)) Err.fail("Can't distribute into $DIST_NAME")

        // Clean build directory
        buildDir.deleteRecursively()
    }

    fun runGradle(projDir: File, task: String): Boolean {
        val sep = File.separatorChar
        return Sys.runShell(projDir, listOf(".${sep}gradlew", task))
    }

    fun hasGradle(projectDir: File): Boolean {
        if (!projectDir.isDirectory) return false
        val gradlewUnix = projectDir.resolve("gradlew")
        val gradlewWindows = projectDir.resolve("gradlew.bat")
        return gradlewUnix.isFile || gradlewWindows.isFile
    }

    fun renameAll(parent: File, vararg params: Pair<String, String>) {
        for ((input, target) in params) {
            val inputFile = parent.resolve(input)
            val targetFile = parent.resolve(target)
            if (!inputFile.renameTo(targetFile)) {
                Err.fail("Can't rename $input into $target")
            }
        }
    }
}