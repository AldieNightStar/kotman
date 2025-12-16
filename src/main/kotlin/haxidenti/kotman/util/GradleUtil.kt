package haxidenti.kotman.util

const val JUPITER_ENGINE_VER = "5.8.1"

object GradleUtil {
    fun genPluginsSection(kotlinVer: String) = """
        plugins {
            java
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
        val string = $$"""
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            testImplementation("org.junit.jupiter:junit-jupiter-api:$$JUPITER_ENGINE_VER")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$$JUPITER_ENGINE_VER")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$CORO_VER")$$deps
        }
        """.trimIndent()

        // Return result
        return string
    }
}