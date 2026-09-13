package haxidenti.kotman

import haxidenti.kotman.util.tab
import java.io.File

const val DEFAULT_AUTHOR = "haxidenti"
const val APP_MOD_NAME = "app"
const val LIB_MOD_NAME = "lib"
const val FILE_BUILD_GRADLE = "build.gradle.kts"

object Project {
    fun createProject(workdir: File, name: String, isApp: Boolean) {
        val projectDir = workdir.resolve(name).also { it.mkdirs() }
        if (!runGradleInit(projectDir, name, isApp)) Err.fail("Can't create project")
        setupMavenLocal(projectDir, name, isApp)
    }

    fun runGradleInit(dir: File, name: String, isApp: Boolean): Boolean {
        return Sys.runShell(
            dir, listOf(
                "gradle",
                "init",
                "--type", if (isApp) "kotlin-application" else "kotlin-library",
                "--dsl", "kotlin",
                "--project-name", name,
                "--package", getPackageName(name),
                "--test-framework", "junit-jupiter",
                "--java-version", "24",
                "--no-split-project",
                "--no-incubating",
                "--no-daemon",
                "--console=plain",
                "--use-defaults",
            )
        )
    }

    fun resolveSubmoduleDir(projectDir: File, isApp: Boolean): File {
        return projectDir.resolve(if (isApp) APP_MOD_NAME else LIB_MOD_NAME)
    }

    fun getPackageName(name: String): String {
        return "$DEFAULT_AUTHOR.$name"
    }

    fun setupMavenLocal(dir: File, name: String, isApp: Boolean) {
        val modDir = resolveSubmoduleDir(dir, isApp)
        val gradleFile = modDir.resolve(FILE_BUILD_GRADLE)
            .takeIf { it.exists() }
            ?: Err.notExist(FILE_BUILD_GRADLE)

        appendGradleFile(
            gradleFile,
            getGradleSettingsPlugins(isApp),
            getGradleSettingsRepos(),
            getGradleSettingsDeps(isApp),
            getGradleSettingsGlobal(isApp),
        )
    }

    fun appendGradleFile(file: File, plugins: String, repos: String, deps: String, global: String) {
        val text = file.readLines()
        var findPlugins = true
        var findRepos = true
        var findDeps = true
        val newText = buildList {
            for (line in text) {
                add(line)
                if (findPlugins && line.startsWith("plugins {")) {
                    add(plugins.tab)
                    findPlugins = false
                }
                if (findRepos && line.startsWith("repositories {")) {
                    add(repos.tab)
                    findRepos = false
                }
                if (findDeps && line.startsWith("dependencies {")) {
                    add(deps.tab)
                    findDeps = false
                }
            }
            if (findPlugins || findRepos || findDeps) Err.fail("Gradle document is different than expected")
            add("")
            add(global)
        }
        file.writeText(newText.joinToString("\n"))
    }

    fun getGradleSettingsPlugins(isApp: Boolean) = buildString {
        if (isApp) {
            appendLine("application")
        } else {
            appendLine("`maven-publish`")
        }
    }

    fun getGradleSettingsRepos(): String {
        return """
            mavenLocal()
            maven { url = uri("https://jitpack.io") }
        """.trimIndent()
    }

    fun getGradleSettingsDeps(isApp: Boolean): String {
        return ""
    }

    fun getGradleSettingsGlobal(isApp: Boolean): String {
        return if (isApp) {
            ""
        } else {
            """
                publishing {
                    publications {
                        create<MavenPublication>("maven") {
                            from(components["java"])
                        }
                    }
                }
                
                java {
                    withSourcesJar()
                }
            """.trimIndent()
        }
    }
}