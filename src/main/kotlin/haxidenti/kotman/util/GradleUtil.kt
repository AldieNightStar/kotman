package haxidenti.kotman.util

object GradleUtil {
    fun genPluginsSection(kotlinVer: String) = """
        plugins {
            kotlin("jvm") version "$kotlinVer"
            id("com.github.johnrengelman.shadow") version "8.1.1"
            id("application")
            `maven-publish`
        }
    """.trimIndent()

    fun genDependenciesSection(additional: List<String>): String {
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
}