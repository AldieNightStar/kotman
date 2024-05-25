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